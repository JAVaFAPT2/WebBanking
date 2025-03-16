package org.user.service.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

public class KeycloakAdminClient {
    public static Keycloak getKeycloakInstance(String serverUrl, String realm, String accessToken) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl) // e.g., "http://localhost:8571"
                .realm(realm)         // e.g., "banking-service"
                .authorization(accessToken) // Your access token
                .build();
    }
}