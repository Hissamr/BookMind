package com.bookmind.utility;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.access-token-secret}")
    private String accessTokenSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-secret}")
    private String refreshTokenSecret;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Get signing key for access token
     */
    private SecretKey getAccessTokenSigningKey() {
        return Keys.hmacShaKeyFor(accessTokenSecret.getBytes());
    }

    /**
     * Get signing key for refresh token
     */
    private SecretKey getRefreshTokenSigningKey() {
        return Keys.hmacShaKeyFor(refreshTokenSecret.getBytes());
    }

    /**
     * Generate access token (short-lived, 15 minutes)
     */
    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "access")
                .signWith(getAccessTokenSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token (long-lived, 30 days)
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "refresh")
                .signWith(getRefreshTokenSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate tokens from Authentication
     */
    public String generateTokenFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return generateAccessToken(username);
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getAccessTokenSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Could not get username from token", e);
            return null;
        }
    }

    /**
     * Extract username from refresh token
     */
    public String getUsernameFromRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getRefreshTokenSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Could not get username from refresh token", e);
            return null;
        }
    }

    /**
     * Validate access token
     */
    public boolean validateAccessToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getAccessTokenSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Access token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Access token is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getRefreshTokenSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Refresh token is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get expiration time from token
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getAccessTokenSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Could not get expiration date from token", e);
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * Get remaining expiration time in milliseconds
     */
    public long getExpirationTimeRemaining(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return 0;
        }
        return expiration.getTime() - System.currentTimeMillis();
    }
}
