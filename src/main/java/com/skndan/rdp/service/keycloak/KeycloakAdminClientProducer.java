package com.skndan.rdp.service.keycloak;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
public class KeycloakAdminClientProducer {
  @ConfigProperty(name = "keycloak.urls.auth")
  String authUrl;

  @ConfigProperty(name = "keycloak.realm")
  String realm;

  @ConfigProperty(name = "keycloak.adminClientId")
  String adminClientId;

  @ConfigProperty(name = "keycloak.adminClientSecret")
  String adminClientSecret;

  @Produces
  @Singleton
  public Keycloak createKeycloakAdminClient() {
    return KeycloakBuilder.builder()
        .serverUrl(authUrl) // Keycloak server URL
        .realm(realm) // Target realm
        .clientId(adminClientId) // Admin client ID
        .clientSecret(adminClientSecret) // Admin client secret
        .grantType("client_credentials") // Client credentials grant
        .build();
  }
}
