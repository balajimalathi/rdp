package com.skndan.rdp.client;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

// import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.skndan.rdp.model.guacamole.TokenResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class GuacamoleAuthService {

  @Inject
  GuacamoleConfig guacamoleConfig;

  private TokenResponse currentToken;
  private Instant tokenExpiry;

  public synchronized String getAuthToken() {
    if (isTokenExpired()) {
      authenticate();
    }
    return currentToken.getAuthToken();
  }

  private void authenticate() {
    String authUrl = guacamoleConfig.apiUrl + "/api/tokens";
    Map<String, String> payload = Map.of(
        "username", guacamoleConfig.username,
        "password", guacamoleConfig.password);

    try {
      // Response response = RestClientBuilder.newBuilder()
      //     .baseUri(URI.create(authUrl))
      //     .build()
      //     .request("/api/tokens")
      //     .post(Entity.form(new MultivaluedHashMap<>(payload)));

      // if (response.getStatus() == 200) {
      //   currentToken = response.readEntity(TokenResponse.class);
      //   tokenExpiry = Instant.now()
      //       .plusSeconds(currentToken.getExpiresIn() - guacamoleConfig.reauthBufferSeconds);
      // } else {
      //   throw new RuntimeException("Authentication failed: " + response.getStatus());
      // }
    } catch (Exception e) {
      throw new RuntimeException("Failed to authenticate with Guacamole API", e);
    }
  }

  private boolean isTokenExpired() {
    return currentToken == null || tokenExpiry == null || Instant.now().isAfter(tokenExpiry);
  }
}
