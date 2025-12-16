package com.jinyi.auth.service;

import com.jinyi.auth.dto.LoginRequest;
import com.jinyi.auth.dto.LoginResponse;
import com.jinyi.auth.dto.RefreshTokenRequest;
import com.jinyi.auth.entity.Permission;
import com.jinyi.auth.entity.Role;
import com.jinyi.auth.entity.User;
import com.jinyi.auth.mapper.UserMapper;
import com.jinyi.auth.util.JwtUtil;
import com.jinyi.auth.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Authentication Service
 */
@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    /**
     * User login
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsername());
        
        // Find user
        User user = userMapper.findByUsername(loginRequest.getUsername());
        if (user == null) {
            log.warn("User not found: {}", loginRequest.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
        
        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("User account is not active: {}", loginRequest.getUsername());
            throw new RuntimeException("Account has been disabled");
        }
        
        // Check if account is locked
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("User account is locked: {}", loginRequest.getUsername());
            throw new RuntimeException("Account is locked, please try again later");
        }
        
        // Verify password
        if (!passwordUtil.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", loginRequest.getUsername());
            
            // Increase failure count
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            // Lock account for 1 hour if failure count exceeds 5
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLockedUntil(LocalDateTime.now().plusHours(1));
                log.warn("User account locked due to too many failed attempts: {}", loginRequest.getUsername());
            }
            
            userMapper.updateById(user);
            throw new RuntimeException("Invalid username or password");
        }
        
        // Get user roles and permissions
        List<Role> roles = userMapper.findRolesByUserId(user.getId());
        List<Permission> permissions = userMapper.findPermissionsByUserId(user.getId());
        
        // Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        claims.put("permissions", permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList()));
        
        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        
        // Update login information
        user.setLastLoginTime(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userMapper.updateById(user);
        
        log.info("User login successful: {}", loginRequest.getUsername());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24 hours
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .roles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()))
                .permissions(permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList()))
                .build();
    }
    
    /**
     * Refresh token
     */
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        log.info("Refresh token attempt");
        
        // Validate refresh token
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        if (username == null || userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // Find user
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getId().equals(userId)) {
            throw new RuntimeException("User does not exist");
        }
        
        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new RuntimeException("Account has been disabled");
        }
        
        // Get user roles and permissions
        List<Role> roles = userMapper.findRolesByUserId(user.getId());
        List<Permission> permissions = userMapper.findPermissionsByUserId(user.getId());
        
        // Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        claims.put("permissions", permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList()));
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getId(), claims);
        
        log.info("Token refresh successful for user: {}", username);
        
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep refresh token unchanged
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .roles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()))
                .permissions(permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList()))
                .build();
    }
    
    /**
     * User logout
     */
    public void logout(String username) {
        log.info("User logout: {}", username);
        // Here you can implement token blacklist mechanism
        // Since JWT is stateless, simple implementation can delete token on client side
    }
}