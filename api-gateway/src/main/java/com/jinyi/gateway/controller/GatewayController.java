package com.jinyi.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Controller for health checks and info
 */
@RestController
@RequestMapping("/gateway")
@Slf4j
public class GatewayController {
    
    /**
     * Gateway health check
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API Gateway is running");
        response.put("service", "api-gateway");
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(response);
    }
    
    /**
     * Gateway info
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("service", "api-gateway");
        response.put("version", "1.0.0");
        response.put("description", "OData Cloud Platform API Gateway");
        response.put("features", new String[]{
            "JWT Authentication",
            "Permission-based Authorization", 
            "Service Discovery",
            "Load Balancing",
            "CORS Support"
        });
        response.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(response);
    }
}