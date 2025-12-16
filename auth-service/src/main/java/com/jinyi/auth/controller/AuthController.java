package com.jinyi.auth.controller;

import com.jinyi.auth.dto.LoginRequest;
import com.jinyi.auth.dto.LoginResponse;
import com.jinyi.auth.dto.RefreshTokenRequest;
import com.jinyi.auth.service.AuthService;
import com.jinyi.auth.util.JwtUtil;
import com.jinyi.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Login request for user: {}", loginRequest.getUsername());
            
            LoginResponse loginResponse = authService.login(loginRequest);
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }
    
    /**
     * Refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            log.info("Token refresh request");
            
            LoginResponse loginResponse = authService.refreshToken(refreshTokenRequest);
            
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", loginResponse));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("令牌刷新失败: " + e.getMessage()));
        }
    }
    
    /**
     * User logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                authService.logout(username);
                log.info("User logged out: {}", username);
            }
            
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("登出失败: " + e.getMessage()));
        }
    }
    
    /**
     * Validate token
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Object>> validateToken(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("令牌不存在"));
            }
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                return ResponseEntity.ok(ApiResponse.success("令牌有效", 
                        new Object() {
                            public String getUsername() { return username; }
                            public Long getUserId() { return userId; }
                            public boolean isValid() { return true; }
                        }));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("令牌无效"));
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("令牌验证失败: " + e.getMessage()));
        }
    }
    
    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Object>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户未认证"));
            }
            
            String username = authentication.getName();
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", 
                    new Object() {
                        public String getUsername() { return username; }
                        public Object getAuthorities() { return authentication.getAuthorities(); }
                        public boolean isAuthenticated() { return authentication.isAuthenticated(); }
                    }));
        } catch (Exception e) {
            log.error("Get current user failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("认证服务运行正常", "OK"));
    }
    
    /**
     * Extract JWT token from request
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}