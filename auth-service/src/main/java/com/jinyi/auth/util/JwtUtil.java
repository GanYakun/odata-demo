package com.jinyi.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Utility Class
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    
    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Generate access token
     */
    public String generateAccessToken(String username, Long userId, Map<String, Object> claims) {
        Map<String, Object> tokenClaims = new HashMap<>();
        if (claims != null) {
            tokenClaims.putAll(claims);
        }
        tokenClaims.put("userId", userId);
        tokenClaims.put("type", "access");
        
        return Jwts.builder()
                .setClaims(tokenClaims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to get username from token", e);
            return null;
        }
    }
    
    /**
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            log.error("Failed to get user ID from token", e);
            return null;
        }
    }
    
    /**
     * Get claims from token
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Validate token
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration", e);
            return true;
        }
    }
    
    /**
     * Get expiration date from token
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * Check if it is a refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            log.error("Failed to check token type", e);
            return false;
        }
    }
    
    /**
     * Check if it is an access token
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String type = claims.get("type", String.class);
            return "access".equals(type);
        } catch (Exception e) {
            log.error("Failed to check token type", e);
            return false;
        }
    }
    
    /**
     * Simple token validation (without username check)
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
}