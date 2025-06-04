package org.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, Object>> handleNotFoundException(NotFoundException ex, ServerWebExchange exchange) {
        logger.error("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        logger.error("Response status exception: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildErrorResponse(exchange, status, "Request failed", ex.getReason())
                .map(response -> ResponseEntity.status(status).body(response));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<Map<String, Object>> handleAuthenticationException(AuthenticationException ex, ServerWebExchange exchange) {
        logger.error("Authentication error: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, ServerWebExchange exchange) {
        logger.error("Access denied: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, "Access denied", ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleFeignException(FeignException ex, ServerWebExchange exchange) {
        logger.error("Feign client error: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.status());

        Map<String, Object> errorDetails = extractFeignResponse(ex);
        if (errorDetails == null) {
            return buildErrorResponse(exchange, status, "Service communication error", ex.getMessage())
                    .map(response -> ResponseEntity.status(status).body(response));
        }

        return Mono.just(ResponseEntity.status(status).body(errorDetails));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> handleCircuitBreakerException(CallNotPermittedException ex, ServerWebExchange exchange) {
        logger.error("Circuit breaker open: {}", ex.getMessage());
        return buildErrorResponse(exchange, HttpStatus.SERVICE_UNAVAILABLE,
                "Service temporarily unavailable", "The service is currently unavailable, please try again later");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, Object>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        logger.error("Unexpected error occurred", ex);
        return buildErrorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error", "An unexpected error occurred");
    }

    private Mono<Map<String, Object>> buildErrorResponse(ServerWebExchange exchange,
                                                         HttpStatus status,
                                                         String error,
                                                         String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("requestId", exchange.getRequest().getId());

        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(status);

        return Mono.just(errorResponse);
    }

    private Map<String, Object> extractFeignResponse(FeignException ex) {
        try {
            // Attempt to extract the JSON response from the Feign exception
            String responseBody = ex.contentUTF8();
            if (responseBody != null && !responseBody.isEmpty()) {
                return objectMapper.readValue(responseBody, Map.class);
            }
        } catch (JsonProcessingException e) {
            logger.warn("Could not parse Feign client error response: {}", e.getMessage());
        }
        return null;
    }
}