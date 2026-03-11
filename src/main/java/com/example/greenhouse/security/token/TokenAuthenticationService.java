package com.example.greenhouse.security.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.greenhouse.exceptions.auth.InvalidTokenTypeException;
import com.example.greenhouse.security.JwtUtil;
import com.example.greenhouse.security.jwt.TokenType;
import com.example.greenhouse.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenAuthenticationService {
    private static final String CLUSTER_ID_CLAIM = "cluster_id";

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public Authentication authenticate(String token) {
        DecodedJWT jwt = jwtUtil.verify(token);
        TokenType tokenType;
        try {
            tokenType = jwtUtil.getTokenType(jwt);
        } catch (RuntimeException ex) {
            throw new InvalidTokenTypeException("Invalid token_type claim", ex);
        }

        return switch (tokenType) {
            case USER -> authenticateUser(jwt);
            case DEVICE -> authenticateDevice(jwt);
        };
    }

    private Authentication authenticateUser(DecodedJWT jwt) {
        long telegramId = Long.parseLong(jwt.getSubject());
        UserDetails userDetails = new com.example.greenhouse.security.UserDetails(
                userService.findUserByTelegramId(telegramId)
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private Authentication authenticateDevice(DecodedJWT jwt) {
        UUID deviceId = UUID.fromString(jwt.getSubject());
        String clusterIdRaw = jwt.getClaim(CLUSTER_ID_CLAIM).asString();
        if (clusterIdRaw == null || clusterIdRaw.isBlank()) {
            throw new IllegalArgumentException("Device token has no cluster_id claim");
        }

        DevicePrincipal principal = new DevicePrincipal(deviceId, UUID.fromString(clusterIdRaw));
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))
        );
    }
}
