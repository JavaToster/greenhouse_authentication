package com.example.greenhouse.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.greenhouse.exceptions.auth.InvalidTokenTypeException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtAuthenticationProvider authenticationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws IOException {
        jwtFilter = new JwtFilter(authenticationService);
        responseBody = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        lenient().when(request.getRequestURI()).thenReturn("/auth/sign-in");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipAuthenticationWhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldSkipAuthenticationWhenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldReturnUnauthorizedWhenBearerTokenIsBlank() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("Invalid JWT Token in Bearer Header"));
    }

    @Test
    void shouldSetSecurityContextWhenTokenIsValid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        Authentication authentication = new UsernamePasswordAuthenticationToken("user", "valid-token");
        when(authenticationService.authenticate("valid-token")).thenReturn(authentication);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == authentication);
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtVerificationFails() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(authenticationService.authenticate("invalid-token"))
                .thenThrow(new JWTVerificationException("bad signature"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("Invalid JWT Token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenTypeIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(authenticationService.authenticate("some-token"))
                .thenThrow(new InvalidTokenTypeException("wrong type", new RuntimeException()));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("Token type is not allowed"));
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenPayloadIsMalformed() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(authenticationService.authenticate("some-token"))
                .thenThrow(new IllegalArgumentException("bad payload"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("Malformed token payload"));
    }

    @Test
    void shouldReturnNotFoundWhenEntityNotFound() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(authenticationService.authenticate("some-token"))
                .thenThrow(new EntityNotFoundException("no such user"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("User not found"));
    }

    @Test
    void shouldReturnInternalServerErrorOnUnexpectedException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(authenticationService.authenticate("some-token"))
                .thenThrow(new RuntimeException("boom"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(responseBody.toString().contains("Internal server error"));
    }
}