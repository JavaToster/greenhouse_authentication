package com.example.greenhouse.security.jwt;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.greenhouse.exceptions.auth.InvalidTokenTypeException;
import com.example.greenhouse.security.DevicePrincipal;
import com.example.greenhouse.security.UserPrincipal;
import com.example.greenhouse.util.enums.Role;
import com.example.greenhouse.util.enums.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationProviderTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private DecodedJWT decodedJWT;

    @Mock
    private Claim claim;

    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @BeforeEach
    void setUp() {
        jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtUtil);
    }

    @Test
    void shouldDelegateTokenGenerationToJwtUtil() {
        when(jwtUtil.generateToken(123L, Role.ROLE_ADMIN)).thenReturn("generated-token");

        String result = jwtAuthenticationProvider.generate(123L, Role.ROLE_ADMIN);

        assertEquals("generated-token", result);
        verify(jwtUtil, times(1)).generateToken(123L, Role.ROLE_ADMIN);
    }

    @Test
    void shouldAuthenticateUserPrincipalWhenTokenTypeIsUser() {
        when(jwtUtil.verify("user-token")).thenReturn(decodedJWT);
        when(jwtUtil.getTokenType(decodedJWT)).thenReturn(TokenType.USER);
        when(decodedJWT.getSubject()).thenReturn("123");
        when(jwtUtil.getRole(decodedJWT)).thenReturn(Role.ROLE_OWNER);

        Authentication authentication = jwtAuthenticationProvider.authenticate("user-token");

        assertInstanceOf(UserPrincipal.class, authentication.getPrincipal());
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals(123L, principal.telegramId());
        assertEquals(Role.ROLE_OWNER, principal.role());
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_OWNER")));
    }

    @Test
    void shouldAuthenticateDevicePrincipalWhenTokenTypeIsDevice() {
        UUID deviceId = UUID.randomUUID();
        UUID clusterId = UUID.randomUUID();

        when(jwtUtil.verify("device-token")).thenReturn(decodedJWT);
        when(jwtUtil.getTokenType(decodedJWT)).thenReturn(TokenType.DEVICE);
        when(decodedJWT.getSubject()).thenReturn(deviceId.toString());
        when(decodedJWT.getClaim("cluster_id")).thenReturn(claim);
        when(claim.asString()).thenReturn(clusterId.toString());

        Authentication authentication = jwtAuthenticationProvider.authenticate("device-token");

        assertInstanceOf(DevicePrincipal.class, authentication.getPrincipal());
        DevicePrincipal principal = (DevicePrincipal) authentication.getPrincipal();
        assertEquals(deviceId, principal.deviceId());
        assertEquals(clusterId, principal.clusterId());
        assertTrue(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_DEVICE")));
    }

    @Test
    void shouldThrowExceptionWhenDeviceTokenHasNoClusterIdClaim() {
        UUID deviceId = UUID.randomUUID();

        when(jwtUtil.verify("device-token")).thenReturn(decodedJWT);
        when(jwtUtil.getTokenType(decodedJWT)).thenReturn(TokenType.DEVICE);
        when(decodedJWT.getSubject()).thenReturn(deviceId.toString());
        when(decodedJWT.getClaim("cluster_id")).thenReturn(claim);
        when(claim.asString()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> jwtAuthenticationProvider.authenticate("device-token"));
    }

    @Test
    void shouldWrapExceptionWhenTokenTypeClaimIsInvalid() {
        when(jwtUtil.verify("bad-token")).thenReturn(decodedJWT);
        when(jwtUtil.getTokenType(decodedJWT)).thenThrow(new IllegalArgumentException("Missing token_type claim"));

        assertThrows(InvalidTokenTypeException.class, () -> jwtAuthenticationProvider.authenticate("bad-token"));
    }
}