package com.myblogbackend.blog.config.security;

import com.myblogbackend.blog.enums.TokenType;
import com.myblogbackend.blog.exception.InvalidDataException;
import com.myblogbackend.blog.exception.InvalidTokenRequestException;
import com.myblogbackend.blog.exception.JwtTokenExpiredException;
import com.myblogbackend.blog.models.RoleEntity;
import com.myblogbackend.blog.models.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
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

    private final RedisTemplate<String, String> redisTemplate;

    public JwtProvider(final RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String generateJwtToken(final UserEntity userEntity, final String deviceId) {
        logger.info("Generating JWT access token for user: {}", userEntity.getEmail());
        return generateToken(userEntity, expiryMinutes, TokenType.ACCESS_TOKEN, deviceId);
    }

    public String generateTokenFromUser(final UserEntity userEntity, final String deviceId) {
        logger.info("Generating JWT refresh token for user: {}", userEntity.getEmail());
        return generateToken(userEntity, expiryDay, TokenType.REFRESH_TOKEN, deviceId);
    }

    private String generateToken(final UserEntity userEntity, final long expiryDuration, final TokenType tokenType, final String deviceId) {
        logger.debug("Generating {} token for user: {} with expiry duration: {} minutes", tokenType, userEntity.getEmail(), expiryDuration);
        var claims = new java.util.HashMap<>(getClaimsFromUser(userEntity));

        // Add deviceId to claims
        claims.put("deviceId", deviceId);

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
            if (Boolean.TRUE.equals(redisTemplate.hasKey(authToken))) {
                throw new InvalidTokenRequestException("JWT", authToken, "Token has been blacklisted");
            }

            Claims claims = parseClaims(authToken, type);
            String userEmail = claims.getSubject();
            String deviceId = claims.get("deviceId", String.class);

            // Fetch the stored device ID
            String storedDeviceId = redisTemplate.opsForValue().get(userEmail + ":deviceId");

            // Check if the stored deviceId exists, if not, refresh it for valid tokens
            if (storedDeviceId == null) {
                // Option 1: Regenerate the deviceId if valid session, or extend TTL
                redisTemplate.opsForValue().set(userEmail + ":deviceId", deviceId, Duration.ofMinutes(30));
            } else if (!storedDeviceId.equals(deviceId)) {
                throw new InvalidTokenRequestException("JWT", authToken, "User logged in from another device");
            }

            // Validate token expiry
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new JwtTokenExpiredException("Expired JWT token");
            }

            return true;
        } catch (JwtTokenExpiredException ex) {
            logger.warn("Expired JWT token: {}", ex.getMessage());
            throw ex; // Re-throw the exception
        } catch (Exception ex) {
            logger.error("Token validation failed: {}", ex.getMessage());
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        }
    }
}