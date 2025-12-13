package com.jinyi.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway配置类
 * 提供路由配置和全局过滤器
 */
@Configuration
@Slf4j
public class GatewayConfig {

    /**
     * 编程式路由配置（可选，也可以完全使用application.yml配置）
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // OData服务路由
                .route("odata-service", r -> r
                        .path("/odata/**")
                        .filters(f -> f
                                .stripPrefix(1)  // 移除/odata前缀
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                        )
                        .uri("http://localhost:8080")
                )
                // 平台配置服务路由
                .route("platform-config", r -> r
                        .path("/platform/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                        )
                        .uri("http://localhost:8081")
                )
                .build();
    }

    /**
     * 全局请求日志过滤器
     */
    @Bean
    public GlobalFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    /**
     * 请求日志过滤器实现
     */
    @Slf4j
    public static class RequestLoggingFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            
            log.info("Gateway Request: {} {} from {}",
                    request.getMethod(),
                    request.getURI(),
                    request.getRemoteAddress()
            );

            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        log.info("Gateway Response: {} for {}",
                                exchange.getResponse().getStatusCode(),
                                request.getURI()
                        );
                    })
            );
        }

        @Override
        public int getOrder() {
            return -1; // 高优先级，最先执行
        }
    }
}