package com.myblogbackend.blog.config.security;

import com.myblogbackend.blog.enums.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

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
        } catch (ExpiredJwtException | BadCredentialsException ex) {
            request.setAttribute("exception", ex);
            throw ex;
        }
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
