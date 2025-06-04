package org.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.apigateway.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_KEY = "role";
    private static final String USERNAME_KEY = "username";
    private static final String USER_ID_KEY = "userId";

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final JwtProperties jwtProperties;

    @Autowired
    public AuthFilter(JwtProperties jwtProperties) {
        super(Config.class);
        this.jwtProperties = jwtProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Check if the path is excluded from authentication
            if (isPathExcluded(path)) {
                return chain.filter(exchange);
            }

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange.getResponse(), "Missing authorization header");
            }

            // Extract and validate the token
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return onError(exchange.getResponse(), "Invalid authentication format");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                Claims claims = validateToken(token);

                // Add user information to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Auth-User-ID", claims.get(USER_ID_KEY, String.class))
                        .header("X-Auth-Username", claims.get(USERNAME_KEY, String.class))
                        .header("X-Auth-Role", claims.get(ROLE_KEY, String.class))
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (ExpiredJwtException e) {
                return onError(exchange.getResponse(), "Token has expired");
            } catch (SignatureException | MalformedJwtException | UnsupportedJwtException |
                     IllegalArgumentException e) {
                logger.error("JWT validation error", e);
                return onError(exchange.getResponse(), "Invalid token");
            }
        };
    }

    public Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isPathExcluded(String path) {
        List<String> excludedPaths = jwtProperties.getPaths();
        if (excludedPaths == null || excludedPaths.isEmpty()) {
            logger.warn("No excluded paths configured for JWT authentication");
            return false;
        }

        return excludedPaths.stream()
                .anyMatch(pattern -> {
                    if (pattern.endsWith("/**")) {
                        String prefix = pattern.substring(0, pattern.length() - 3);
                        return path.startsWith(prefix);
                    }
                    return path.equals(pattern);
                });
    }

    private Mono<Void> onError(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date().getTime());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.put("message", message);

        byte[] bytes = writeErrorResponse(errorResponse).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String writeErrorResponse(Map<String, Object> errorResponse) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : errorResponse.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    sb.append("\"").append(entry.getValue()).append("\"");
                } else {
                    sb.append(entry.getValue());
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return "{\"error\":\"Error processing authentication\"}";
        }
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
