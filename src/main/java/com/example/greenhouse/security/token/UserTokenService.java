package com.example.greenhouse.security.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.greenhouse.util.enums.Role;

@Service
@RequiredArgsConstructor
public class UserTokenService {
    private final JwtUtil jwtUtil;

    public String generate(long telegramId, Role role) {
        return jwtUtil.generateToken(
                telegramId, role
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
