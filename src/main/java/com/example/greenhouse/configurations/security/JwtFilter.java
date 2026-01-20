package com.example.greenhouse.configurations.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.greenhouse.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
                // Если заголовок отсутствует - пропускаем запрос дальше
                filterChain.doFilter(request, response);
                return;
            }

            String jwt = authHeader.substring(7);

            if (jwt.isBlank()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token in Bearer Header");
                return;
            }

            long telegramId = jwtUtil.validateTokenAndRetrieveSubject(jwt);
            UserDetails userDetails = new User(String.valueOf(telegramId), "", Collections.emptyList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);

        } catch (JWTVerificationException ex) {
            logger.error("JWT verification failed", ex);
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JWT Token");

        } catch (EntityNotFoundException ex) {
            logger.error("User not found", ex);
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");

        } catch (Exception ex) {
            logger.error("Authentication error", ex);
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
