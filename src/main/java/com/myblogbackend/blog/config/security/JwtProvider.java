package com.myblogbackend.blog.config.security;

import com.myblogbackend.blog.cache.LoggedOutJwtTokenCache;
import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.event.OnUserLogoutSuccessEvent;
import com.myblogbackend.blog.exception.InvalidDataException;
import com.myblogbackend.blog.exception.InvalidTokenRequestException;
import com.myblogbackend.blog.models.RoleEntity;
import com.myblogbackend.blog.models.UserEntity;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


@Component
public class JwtProvider {

    private static final Logger logger = LogManager.getLogger(JwtProvider.class);

    @Value("${jwt.expiryMinutes}")
    private long expiryMinutes;

    @Value("${jwt.expiryDay}")
    private long expiryDay;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    private final LoggedOutJwtTokenCache loggedOutJwtTokenCache;

    public JwtProvider(@Lazy final LoggedOutJwtTokenCache loggedOutJwtTokenCache) {
        this.loggedOutJwtTokenCache = loggedOutJwtTokenCache;
    }

    public String generateJwtToken(final UserEntity userEntity) {
        logger.info("Generating JWT access token for user: {}", userEntity.getEmail());
        return generateToken(userEntity, expiryMinutes, TokenType.ACCESS_TOKEN);
    }

    public String generateTokenFromUser(final UserEntity userEntity) {
        logger.info("Generating JWT refresh token for user: {}", userEntity.getEmail());
        return generateToken(userEntity, expiryDay, TokenType.REFRESH_TOKEN);
    }

    private String generateToken(final UserEntity userEntity, final long expiryDuration, final TokenType tokenType) {
        logger.debug("Generating {} token for user: {} with expiry duration: {} minutes",
                tokenType, userEntity.getEmail(), expiryDuration);
        var claims = getClaimsFromUser(userEntity);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userEntity.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * expiryDuration))
                .signWith(getKey(tokenType), SignatureAlgorithm.HS256)
                .compact();
    }

    private Map<String, Object> getClaimsFromUser(final UserEntity userEntity) {
        logger.debug("Extracting claims for user: {}", userEntity.getEmail());
        return Map.of(
                "userId", userEntity.getId(),
                "roles", userEntity.getRoles().stream().map(RoleEntity::getName).toList()
        );
    }

    public String generateRefreshTokenToken(final Instant expiryDate) {
        logger.info("Generating refresh token with custom expiry date.");
        return Jwts.builder()
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
                .signWith(getKey(TokenType.REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey(final TokenType type) {
        logger.info("---------- getKey ----------");
        return switch (type) {
            case ACCESS_TOKEN -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            case REFRESH_TOKEN -> Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            default -> throw new InvalidDataException("Invalid token type");
        };
    }

    public String getUserNameFromJwtToken(final String token, final TokenType type) {
        logger.debug("Extracting username from JWT token of type: {}", type);
        return parseClaims(token, type).getSubject();
    }

    public Date getTokenExpiryFromJWT(final String token, final TokenType type) {
        logger.debug("Extracting token expiry from JWT token of type: {}", type);
        return parseClaims(token, type).getExpiration();
    }

    private Claims parseClaims(final String token, final TokenType type) {
        logger.debug("Parsing claims from JWT token of type: {}", type);
        return Jwts.parserBuilder()
                .setSigningKey(getKey(type))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateJwtToken(final String authToken, final TokenType type, final HttpServletRequest request) {
        try {
            logger.info("Validating JWT token of type: {}", type);
            parseClaims(authToken, type);
            validateTokenIsNotForALoggedOutDevice(authToken);
            logger.info("JWT token validated successfully.");
            return true;
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            logger.warn("Expired JWT token: {}", ex.getMessage());
            request.setAttribute("expired", ex.getMessage());
            throw new ExpiredJwtException(ex.getHeader(), ex.getClaims(), "Expired JWT token");
        }
    }

    private void validateTokenIsNotForALoggedOutDevice(final String authToken) {
        logger.debug("Checking if token is associated with a logged-out user.");
        OnUserLogoutSuccessEvent loggedOutEvent = loggedOutJwtTokenCache.getLogoutEventForToken(authToken);
        if (loggedOutEvent != null) {
            String errorMessage = String.format("Token corresponds to an already logged out user [%s] at [%s]. Please login again",
                    loggedOutEvent.getUserEmail(), loggedOutEvent.getEventTime());
            logger.warn(errorMessage);
            throw new InvalidTokenRequestException("JWT", authToken, errorMessage);
        }
    }
}