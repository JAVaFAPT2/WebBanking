package org.api.gateway.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                http
                        .authorizeExchange(exchanges -> exchanges
                                //ALLOWING REGISTER API FOR DIRECT ACCESS
                                .pathMatchers("/api/users/register").permitAll()
                                //ALL OTHER APIS ARE AUTHENTICATED
                                .anyExchange().authenticated()
                        )
                        .csrf(ServerHttpSecurity.CsrfSpec::disable)
                        .oauth2Login(Customizer.withDefaults())
                        .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(Customizer.withDefaults())
                        );
                return http.build();
        }
}