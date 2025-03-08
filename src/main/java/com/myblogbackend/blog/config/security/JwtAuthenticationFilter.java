package com.myblogbackend.blog.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.exception.InvalidTokenRequestException;
import com.myblogbackend.blog.exception.JwtTokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/favicon.ico", "/static/", "/images/", "/css/", "/js/",
            "/v3/api-docs", "/swagger-ui", "/api/v1/auth/", "/api/v1/public/", "/api/v2/public/"
    );

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(@NotNull final HttpServletRequest request,
                                    @NotNull final HttpServletResponse response,
                                    @NotNull final FilterChain filterChain) throws ServletException, IOException {
        try {
            getJwt(request).ifPresent(jwt -> {
                if (tokenProvider.validateJwtToken(jwt, TokenType.ACCESS_TOKEN, request)) {
                    String username = tokenProvider.getUserNameFromJwtToken(jwt, TokenType.ACCESS_TOKEN);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    setAuthentication(userDetails, request);
                }
            });
            filterChain.doFilter(request, response);
        } catch (InvalidTokenRequestException | JwtTokenExpiredException | BadCredentialsException ex) {
            logger.warn("Authentication error: {}", ex.getMessage());
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, ex.getMessage());
            return;
        }
    }

    private void setErrorResponse(final HttpStatus status, final HttpServletResponse response, final String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", status.value());
        responseBody.put("success", false);
        responseBody.put("message", message);
        responseBody.put("error", List.of(Map.of("message", message)));
        responseBody.put("timestamp", new Timestamp(System.currentTimeMillis()).toInstant().toString());
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(responseBody));
    }

    private Optional<String> getJwt(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.replace("Bearer ", ""));
    }

    private void setAuthentication(final UserDetails userDetails, final HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
