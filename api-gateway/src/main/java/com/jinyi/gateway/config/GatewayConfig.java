package com.jinyi.gateway.config;

import com.jinyi.gateway.filter.AuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Gateway Configuration
 */
@Configuration
@Slf4j
public class GatewayConfig {
    
    @Autowired
    private AuthenticationFilter authenticationFilter;
    
    /**
     * Configure routes with authentication
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service Route (Public)
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))
                
                // Platform Config Service Route (Protected)
                .route("platform-config-service", r -> r
                        .path("/platform/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://platform-config-service"))
                
                // OData Gateway Service Route (Protected)
                .route("odata-gateway-service", r -> r
                        .path("/odata/**")
                        .filters(f -> f
                                .stripPrefix(1)  // Remove /odata prefix
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://odata-gateway"))
                
                // Health Check Route (Public)
                .route("health-check", r -> r
                        .path("/actuator/**")
                        .uri("lb://platform-config-service"))
                
                // Admin Routes (Protected with strict permissions)
                .route("admin-routes", r -> r
                        .path("/admin/**")
                        .filters(f -> {
                            AuthenticationFilter.Config config = new AuthenticationFilter.Config();
                            config.setRequirePermission(true);
                            return f.filter(authenticationFilter.apply(config));
                        })
                        .uri("lb://platform-config-service"))
                
                .build();
    }
    
    /**
     * CORS Configuration
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOriginPattern("*");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("X-User-Id");
        corsConfig.addExposedHeader("X-Username");
        corsConfig.addExposedHeader("X-User-Roles");
        corsConfig.addExposedHeader("X-User-Permissions");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}