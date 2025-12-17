package com.jinyi.gateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * JWT Utility Class for API Gateway
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret:odata-auth-secret-key-2024-very-long-and-secure-key-for-jwt-token-generation}")
    private String secret;
    
    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to get username from token: {}", e.getMessage());
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
            log.error("Failed to get user ID from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user roles from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("roles", List.class);
        } catch (Exception e) {
            log.error("Failed to get roles from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user permissions from token
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("permissions", List.class);
        } catch (Exception e) {
            log.error("Failed to get permissions from token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
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
            log.error("Failed to check token type: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get claims from token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}