package com.myblogbackend.blog.cache;

import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.event.OnUserLogoutSuccessEvent;
import com.myblogbackend.blog.config.security.JwtProvider;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class LoggedOutJwtTokenCache {
    private static final Logger logger = LogManager.getLogger(LoggedOutJwtTokenCache.class);
    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final JwtProvider tokenProvider;

    @Autowired
    public LoggedOutJwtTokenCache(final JwtProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(1000)
                .build();
    }

    public void markLogoutEventForToken(final OnUserLogoutSuccessEvent event) {
        String token = event.getToken();
        if (tokenEventMap.containsKey(token)) {
            logger.info(String.format("Log out token for user [%s] is already present in the cache", event.getUserEmail()));

        } else {
            var tokenExpiryDate = tokenProvider.getTokenExpiryFromJWT(token, TokenType.ACCESS_TOKEN);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            logger.info(String.format("Logout token cache set for [%s] with a TTL of [%s] seconds. Token is due expiry at [%s]",
                    event.getUserEmail(), ttlForToken, tokenExpiryDate));
            tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
        }
    }

    public OnUserLogoutSuccessEvent getLogoutEventForToken(final String token) {
        return tokenEventMap.get(token);
    }

    private long getTTLForToken(final Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}