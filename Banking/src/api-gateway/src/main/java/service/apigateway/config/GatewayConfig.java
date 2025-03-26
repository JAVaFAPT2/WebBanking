package service.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/users/authenticate", "/users/register").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/users/**")
                        .uri("lb://USER-SERVICE"))
                .route("account-service", r -> r.path("/accounts/**")
                        .uri("lb://ACCOUNT-SERVICE"))
                .route("fund-transfer-service", r -> r.path("/transfers/**")
                        .uri("lb://FUND-TRANSFER-SERVICE"))
                .route("transaction-service", r -> r.path("/transactions/**")
                        .uri("lb://TRANSACTION-SERVICE"))
                .route("notification-service", r -> r.path("/notifications/**")
                        .uri("lb://NOTIFICATION-SERVICE"))
                .build();
    }
}
