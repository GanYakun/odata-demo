package com.jinyi.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinyi.gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter for API Gateway
 */
@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AuthenticationFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            String path = request.getURI().getPath();
            log.debug("Processing request: {} {}", request.getMethod(), path);
            
            // Skip authentication for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint, skipping authentication: {}", path);
                return chain.filter(exchange);
            }
            
            // Extract JWT token from Authorization header
            String token = extractToken(request);
            
            if (!StringUtils.hasText(token)) {
                log.warn("Missing JWT token for protected endpoint: {}", path);
                return handleUnauthorized(response, "Missing authentication token");
            }
            
            // Validate JWT token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for endpoint: {}", path);
                return handleUnauthorized(response, "Invalid authentication token");
            }
            
            // Check if token is expired
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Expired JWT token for endpoint: {}", path);
                return handleUnauthorized(response, "Authentication token expired");
            }
            
            // Check if it's an access token
            if (!jwtUtil.isAccessToken(token)) {
                log.warn("Invalid token type for endpoint: {}", path);
                return handleUnauthorized(response, "Invalid token type");
            }
            
            // Extract user information from token
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);
            List<String> permissions = jwtUtil.getPermissionsFromToken(token);
            
            if (username == null || userId == null) {
                log.warn("Invalid token payload for endpoint: {}", path);
                return handleUnauthorized(response, "Invalid token payload");
            }
            
            // Check permissions for specific endpoints
            if (config.isRequirePermission() && !hasRequiredPermission(path, permissions)) {
                log.warn("Insufficient permissions for user {} to access: {}", username, path);
                return handleForbidden(response, "Insufficient permissions");
            }
            
            // Add user information to request headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId.toString())
                    .header("X-Username", username)
                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                    .header("X-User-Permissions", permissions != null ? String.join(",", permissions) : "")
                    .build();
            
            log.debug("Authentication successful for user: {} accessing: {}", username, path);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
    
    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String path) {
        // Public endpoints that don't require authentication
        String[] publicPaths = {
            "/auth/login",
            "/auth/refresh",
            "/auth/health",
            "/actuator/health",
            "/actuator/info",
            "/favicon.ico"
        };
        
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if user has required permission for the endpoint
     */
    private boolean hasRequiredPermission(String path, List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        // Define permission requirements for different endpoints
        Map<String, String> pathPermissions = new HashMap<>();
        pathPermissions.put("/platform/applications", "application:read");
        pathPermissions.put("/platform/entity-definitions", "entity:read");
        pathPermissions.put("/platform/entity-data", "odata:query");
        pathPermissions.put("/odata/", "odata:query");
        
        // Check if path requires specific permission
        for (Map.Entry<String, String> entry : pathPermissions.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                String requiredPermission = entry.getValue();
                boolean hasPermission = permissions.contains(requiredPermission);
                log.debug("Checking permission {} for path {}: {}", requiredPermission, path, hasPermission);
                return hasPermission;
            }
        }
        
        // If no specific permission required, allow access
        return true;
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * Handle unauthorized access (401)
     */
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        return handleErrorResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }
    
    /**
     * Handle forbidden access (403)
     */
    private Mono<Void> handleForbidden(ServerHttpResponse response, String message) {
        return handleErrorResponse(response, HttpStatus.FORBIDDEN, "Forbidden", message);
    }
    
    /**
     * Handle error response
     */
    private Mono<Void> handleErrorResponse(ServerHttpResponse response, HttpStatus status, String error, String message) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON response", e);
            return response.setComplete();
        }
    }
    
    /**
     * Configuration class for the filter
     */
    public static class Config {
        private boolean requirePermission = true;
        
        public boolean isRequirePermission() {
            return requirePermission;
        }
        
        public void setRequirePermission(boolean requirePermission) {
            this.requirePermission = requirePermission;
        }
    }
}