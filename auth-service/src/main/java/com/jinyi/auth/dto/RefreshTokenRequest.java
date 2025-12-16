package com.jinyi.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO
 */
@Data
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token cannot be empty")
    private String refreshToken;
}