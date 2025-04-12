package org.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class RBACFilter extends AbstractGatewayFilterFactory<RBACFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RBACFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    public RBACFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if the request has an Authorization header
            if (!request.getHeaders().containsKey(AUTHORIZATION_HEADER)) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // Extract the token from the Authorization header
            String authHeader = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                // Validate the token and extract claims
                Claims claims = extractClaims(token);

                // Extract roles from the token
                List<String> userRoles = extractRoles(claims);

                // Check if the user has the required roles
                if (!hasRequiredRoles(userRoles, config.getRequiredRoles())) {
                    return onError(exchange, "Access denied: Insufficient privileges", HttpStatus.FORBIDDEN);
                }

                // Add user roles to the request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Roles", String.join(",", userRoles))
                        .header("X-User-Id", claims.getSubject())
                        .build();

                // Log the access
                logger.info("User {} with roles {} accessed {}",
                        claims.getSubject(),
                        userRoles,
                        request.getPath());

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                logger.error("Error validating JWT token", e);
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Claims claims) {
        // Extract roles from JWT claims
        // The roles might be stored in different formats depending on your JWT structure
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List) {
            return (List<String>) rolesObj;
        } else if (rolesObj instanceof String) {
            return Arrays.asList(((String) rolesObj).split(","));
        }

        return Collections.emptyList();
    }

    private boolean hasRequiredRoles(List<String> userRoles, List<String> requiredRoles) {
        // If no specific roles are required, allow access
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }

        // Check if the user has any of the required roles
        return userRoles.stream().anyMatch(requiredRoles::contains);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorJson = String.format("{\"error\": \"%s\", \"status\": %d}",
                message, status.value());

        DataBuffer buffer = response.bufferFactory()
                .wrap(errorJson.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Setter
    @Getter
    public static class Config {
        private List<String> requiredRoles = new ArrayList<>();

    }
}
