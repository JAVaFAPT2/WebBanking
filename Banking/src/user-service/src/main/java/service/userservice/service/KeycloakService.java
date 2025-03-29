package service.userservice.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakService {
    private final Keycloak keycloak;

    public KeycloakService(
            @Value("${app.config.keycloak.server-url}") String serverUrl,
            @Value("${app.config.keycloak.realm}") String realm,
            @Value("${app.config.keycloak.client-id}") String clientId,
            @Value("${app.config.keycloak.client-secret}") String clientSecret) {

        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials") // Using client credentials for server-to-server communication
                .build();
    }

    public void createUser(UserRepresentation user) {
        keycloak.realm("banking-service").users().create(user);
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm("banking-service").users().list();
    }

    public void deleteUser(String userId) {
        keycloak.realm("banking-service").users().delete(userId);
    }

    public UserRepresentation getUserById(String userId) {
        return keycloak.realm("banking-service").users().get(userId).toRepresentation();
    }

    public void updateUser(String userId, UserRepresentation user) {
        keycloak.realm("banking-service").users().get(userId).update(user);
    }
}
