package com.example.greenhouse.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import com.auth0.jwt.JWT;

@Component
public class JwtUtil {
    @Value("${spring.security.jwt.secret}")
    private String secret;

    public String generateToken(long telegramId){
        Date expirationDate = Date.from(ZonedDateTime.now().plusYears(1).toInstant());
        return JWT.create()
                .withSubject(String.valueOf(telegramId))
                .withIssuedAt(new Date())
                .withIssuer("greenhouse")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public long validateTokenAndRetrieveSubject(String token){
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("greenhouse")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        return Long.valueOf(jwt.getSubject());
    }
}
