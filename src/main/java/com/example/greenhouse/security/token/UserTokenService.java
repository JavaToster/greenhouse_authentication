package com.example.greenhouse.security.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserTokenService {
    private static final Duration USER_TOKEN_TTL = Duration.ofDays(365);

    private final JwtUtil jwtUtil;

    public String generate(long telegramId) {
        return jwtUtil.generateToken(
                String.valueOf(telegramId),
                TokenType.USER,
                Collections.emptyMap(),
                USER_TOKEN_TTL
        );
    }

    public long validateAndGetTelegramId(String token) {
        DecodedJWT jwt = jwtUtil.verify(token);
        TokenType tokenType = jwtUtil.getTokenType(jwt);
        if (tokenType != TokenType.USER) {
            throw new IllegalArgumentException("Token is not a user token");
        }
        return Long.parseLong(jwt.getSubject());
    }
}
