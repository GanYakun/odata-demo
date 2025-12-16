package com.jinyi.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Login Response DTO
 */
@Data
@Builder
public class LoginResponse {
    
    private String accessToken;
    
    private String refreshToken;
    
    private String tokenType;
    
    private Long expiresIn;
    
    private Long userId;
    
    private String username;
    
    private String realName;
    
    private String email;
    
    private List<String> roles;
    
    private List<String> permissions;
}