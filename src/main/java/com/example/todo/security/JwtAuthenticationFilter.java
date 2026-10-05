package com.example.todo.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_SCHEME = "Bearer";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            String username = jwtService.extractUsername(token);
            if (!jwtService.isValid(token, username)) {
                log.debug("توکن نامعتبر برای کاربر '{}' در {}", username, request.getRequestURI());
                return;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException ex) {
            log.debug("توکن منقضی‌شده در {}", request.getRequestURI());
            SecurityContextHolder.clearContext();
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            log.debug("توکن نامعتبر ({}) در {}: {}", ex.getClass().getSimpleName(), request.getRequestURI(),
                    ex.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Per RFC 6750 only the Bearer scheme is honoured, and the scheme is case-insensitive
     * (RFC 7235). Clients such as Swagger UI also copy the raw value including the scheme into
     * the Authorize dialog, producing "Bearer Bearer <token>", so a repeated prefix is stripped
     * as well. A JWT itself never contains whitespace.
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header == null) {
            return null;
        }

        String token = header.trim();
        if (!hasBearerScheme(token)) {
            return null;
        }

        while (hasBearerScheme(token)) {
            token = token.substring(BEARER_SCHEME.length()).trim();
        }

        return token.isEmpty() ? null : token;
    }

    private boolean hasBearerScheme(String value) {
        return value.length() > BEARER_SCHEME.length()
                && value.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())
                && Character.isWhitespace(value.charAt(BEARER_SCHEME.length()));
    }
}