package com.jinyi.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Database Configuration
 */
@Configuration
@Order(1)
public class DatabaseConfig {
    
    // This configuration ensures proper initialization order
    // Tables will be created by JPA before data initialization
}