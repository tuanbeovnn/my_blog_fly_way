package com.myblogbackend.blog.config.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter implements Filter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int MAX_FAVORITE_REQUESTS_PER_MINUTE = 4;
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Apply rate limiting only to authentication endpoints
        if (isAuthEndpoint(path)) {
            String clientIp = getClientIp(httpRequest);
            String key = "rate_limit:" + clientIp + ":" + path;

            String currentCount = redisTemplate.opsForValue().get(key);
            if (currentCount == null) {
                redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
            } else {
                int count = Integer.parseInt(currentCount);
                if (count >= MAX_REQUESTS_PER_MINUTE) {
                    httpResponse.setStatus(429);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                    return;
                } else {
                    redisTemplate.opsForValue().increment(key);
                }
            }
        }
        if (isFavoritesEndpoint(path)) {
            String clientIp = getClientIp(httpRequest);
            String key = "rate_limit:auth:" + clientIp + ":" + path;
            String current = redisTemplate.opsForValue().get(key);
            if (current == null) {
                redisTemplate.opsForValue().set(key, "1", 1, TimeUnit.MINUTES);
            } else {
                int count = Integer.parseInt(current);
                if (count >= MAX_FAVORITE_REQUESTS_PER_MINUTE) {
                    httpResponse.setStatus(429);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                    return;
                } else {
                    redisTemplate.opsForValue().increment(key);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(final String path) {
        return path.contains("/auth/signin") ||
                path.contains("/auth/signup") ||
                path.contains("/auth/forgot") ||
                path.contains("/auth/reset-password");
    }
    private boolean isFavoritesEndpoint(final String path) {
        return path.contains("/favorites") || path.startsWith("/favorites");
    }

    private String getClientIp(final HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
