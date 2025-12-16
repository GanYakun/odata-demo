package com.jinyi.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Password Utility Class
 */
@Component
public class PasswordUtil {
    
    private final PasswordEncoder passwordEncoder;
    
    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Encode password
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Verify password
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * Generate random password
     */
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}