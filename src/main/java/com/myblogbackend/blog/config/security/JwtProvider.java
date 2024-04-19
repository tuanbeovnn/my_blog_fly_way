package com.myblogbackend.blog.config.security;

import com.myblogbackend.blog.cache.LoggedOutJwtTokenCache;
import com.myblogbackend.blog.event.OnUserLogoutSuccessEvent;
import com.myblogbackend.blog.exception.InvalidTokenRequestException;
import com.myblogbackend.blog.models.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;


@Component
public class JwtProvider {

    private static final Logger logger = LogManager.getLogger(JwtProvider.class);
    public static final String ISSUER_GENERATE_TOKEN = "StackAbuse";
    public static final String ISSUER_GENERATE_REFRESH_TOKEN = "Therapex";
    public static final String SIGNING_KEY = "HelloWorld";

    private final LoggedOutJwtTokenCache loggedOutJwtTokenCache;

    public JwtProvider(final @Lazy LoggedOutJwtTokenCache loggedOutJwtTokenCache) {
        this.loggedOutJwtTokenCache = loggedOutJwtTokenCache;
    }

    public String generateJwtToken(final Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuer(ISSUER_GENERATE_TOKEN)
                .setId(String.valueOf(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    public String generateTokenFromUser(final UserEntity userEntity) {
        Instant expiryDate = Instant.now().plusMillis(3600000);
        return Jwts.builder()
                .setSubject(userEntity.getEmail())
                .setIssuer(ISSUER_GENERATE_REFRESH_TOKEN)
                .setId(String.valueOf(userEntity.getId()))
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(expiryDate))
                .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
                .compact();
    }

    public String getUserNameFromJwtToken(final String token) {
        return Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public Date getTokenExpiryFromJWT(final String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SIGNING_KEY)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean validateJwtToken(final String authToken, final HttpServletRequest request) {
        try {
            Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(authToken);
            validateTokenIsNotForALoggedOutDevice(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            request.setAttribute("expired", ex.getMessage());
            throw new ExpiredJwtException(ex.getHeader(), ex.getClaims(), "Expired JWT token");
        }
    }

    private void validateTokenIsNotForALoggedOutDevice(final String authToken) {
        OnUserLogoutSuccessEvent previouslyLoggedOutEvent = loggedOutJwtTokenCache.getLogoutEventForToken(authToken);
        if (previouslyLoggedOutEvent != null) {
            String userEmail = previouslyLoggedOutEvent.getUserEmail();
            Date logoutEventDate = previouslyLoggedOutEvent.getEventTime();
            String errorMessage = String.format("Token corresponds to an already logged out user [%s] at [%s]. Please login again",
                    userEmail, logoutEventDate);
            throw new InvalidTokenRequestException("JWT", authToken, errorMessage);
        }
    }

    public long getExpiryDuration() {
        return 3600000;
    }
}