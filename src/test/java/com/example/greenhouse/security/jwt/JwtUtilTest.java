package com.example.greenhouse.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.greenhouse.util.enums.Role;
import com.example.greenhouse.util.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-for-unit-tests-only";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET);
    }

    @Test
    void shouldGenerateAndVerifyUserTokenSuccessfully() {
        String token = jwtUtil.generateToken(123L, Role.ROLE_OWNER);

        assertNotNull(token);
        DecodedJWT decoded = jwtUtil.verify(token);

        assertEquals("123", decoded.getSubject());
        assertEquals(TokenType.USER, jwtUtil.getTokenType(decoded));
        assertEquals(Role.ROLE_OWNER, jwtUtil.getRole(decoded));
    }

    @Test
    void shouldGenerateTokenWithCustomClaimsAndTtl() {
        String token = jwtUtil.generateToken(
                "device-42",
                TokenType.DEVICE,
                Map.of("cluster_id", "cluster-1"),
                Duration.ofMinutes(5)
        );

        DecodedJWT decoded = jwtUtil.verify(token);

        assertEquals("device-42", decoded.getSubject());
        assertEquals(TokenType.DEVICE, jwtUtil.getTokenType(decoded));
        assertEquals("cluster-1", decoded.getClaim("cluster_id").asString());
    }

    @Test
    void shouldThrowExceptionWhenTokenTypeClaimIsMissing() {
        // Build a raw JWT (signed with the same secret) that intentionally omits the token_type claim,
        // bypassing JwtUtil.generateToken which always sets it.
        String tokenWithoutTypeClaim = JWT.create()
                .withSubject("123")
                .withIssuer("greenhouse")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(java.time.ZonedDateTime.now().plusMinutes(5).toInstant()))
                .sign(Algorithm.HMAC256(SECRET));

        DecodedJWT decoded = jwtUtil.verify(tokenWithoutTypeClaim);

        assertThrows(IllegalArgumentException.class, () -> jwtUtil.getTokenType(decoded));
    }

    @Test
    void shouldThrowExceptionWhenTokenTypeClaimIsUnknownValue() {
        String tokenWithInvalidType = JWT.create()
                .withSubject("123")
                .withIssuer("greenhouse")
                .withClaim("token_type", "NOT_A_REAL_TYPE")
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(java.time.ZonedDateTime.now().plusMinutes(5).toInstant()))
                .sign(Algorithm.HMAC256(SECRET));

        DecodedJWT decoded = jwtUtil.verify(tokenWithInvalidType);

        assertThrows(IllegalArgumentException.class, () -> jwtUtil.getTokenType(decoded));
    }

    @Test
    void shouldReturnUnknownRoleWhenRoleClaimIsAbsent() {
        String token = jwtUtil.generateToken(
                "device-1",
                TokenType.DEVICE,
                Map.of(),
                Duration.ofMinutes(1)
        );
        DecodedJWT decoded = jwtUtil.verify(token);

        assertEquals(Role.ROLE_UNKNOWN, jwtUtil.getRole(decoded));
    }

    @Test
    void shouldThrowExceptionWhenVerifyingTokenSignedWithDifferentSecret() {
        JwtUtil otherJwtUtil = new JwtUtil("a-completely-different-secret-key");
        String token = otherJwtUtil.generateToken(123L, Role.ROLE_OWNER);

        assertThrows(JWTVerificationException.class, () -> jwtUtil.verify(token));
    }

    @Test
    void shouldThrowExceptionWhenTokenIsMalformed() {
        assertThrows(JWTVerificationException.class, () -> jwtUtil.verify("not-a-valid-jwt-token"));
    }

    @Test
    void shouldThrowExceptionWhenClaimTypeIsUnsupported() {
        Map<String, Object> unsupportedClaims = Map.of("weird", new Object());

        assertThrows(IllegalArgumentException.class, () ->
                jwtUtil.generateToken("subject", TokenType.USER, unsupportedClaims, Duration.ofMinutes(1)));
    }
}