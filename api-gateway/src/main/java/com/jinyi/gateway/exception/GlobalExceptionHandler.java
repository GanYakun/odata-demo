package com.jinyi.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for API Gateway
 */
@Component
@Order(-1)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String error = "Internal Server Error";
        String message = ex.getMessage();
        
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = rse.getStatus();
            error = status.getReasonPhrase();
            message = rse.getReason();
        }
        
        log.error("Gateway error: {} - {}", status, message, ex);
        
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", error);
        errorResponse.put("message", message != null ? message : "An error occurred");
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("path", exchange.getRequest().getURI().getPath());
        
        try {
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating JSON error response", e);
            return response.setComplete();
        }
    }
}