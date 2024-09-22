package com.skndan.rdp.service.keycloak;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.model.AuthRequest;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.model.UserRecord;
import com.skndan.rdp.service.auth.AuthService;

import io.quarkus.oidc.AccessTokenCredential;
import io.quarkus.oidc.common.OidcEndpoint;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class KeycloakServiceImpl implements KeycloakService {

  @Value("${keycloak.realm}")
  private String realm;

  @Value("${keycloak.client}")
  private String client;

  @Value("${keycloak.clientSecret}")
  private String clientSecret;

  @Value("${keycloak.urls.auth}")
  private String authUrl;

  @Value("${keycloak.urls.token}")
  private String tokenUrl;

  private final Keycloak keycloak;

 
  
  @Inject
  AuthService authService;

  public KeycloakServiceImpl(Keycloak keycloak) {
    this.keycloak = keycloak;
  }

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public String createUser(UserRecord userRecord) {

    UserRepresentation user = getUserRepresentation(userRecord);

    UsersResource usersResource = getUsersResource();

    Response response = usersResource.create(user);

    System.out.println("Status " + response.getStatus());
    if (Objects.equals(201, response.getStatus())) {

      // Extract the Location header to get the user ID
      String location = response.getHeaderString("Location");
      if (location != null) {
        // The user ID is the last part of the Location URL
        String userId = location.substring(location.lastIndexOf("/") + 1);
        System.out.println("User ID: " + userId);
        return userId;
      }
    }

    return null;
  }

  @Override
  public UserRecord updateUser(String userId, UserRecord userRecord) {
    UsersResource usersResource = getUsersResource();
    UserRepresentation user = getUserRepresentation(userRecord);

    usersResource.get(userId).update(user);

    UserRepresentation updatedUser = usersResource.get(userId).toRepresentation();

    // Check if the updated fields match what we set
    if (updatedUser.getId().equals(userRecord.id()) &&
        updatedUser.getUsername().equals(userRecord.username()) &&
        updatedUser.getEmail().equals(userRecord.email()) &&
        updatedUser.getFirstName().equals(userRecord.firstName()) &&
        updatedUser.getLastName().equals(userRecord.lastName())) {
      System.out.println("Update User Status: Success");
      return userRecord; // Return updated record
    } else {
      System.err.println("Update User Status: Failed");
      return null; // Indicate failure
    }
  }

  @Override
  public void assignRolesToUser(String userId, List<RoleRepresentation> roles, String clientId) {
    UsersResource usersResource = getUsersResource();

    // Get the client UUID for the specified client ID
    String clientUUID = getClientUUID(clientId);

    if (clientUUID != null) {
      usersResource.get(userId).roles().clientLevel(clientUUID).add(roles);

    } else {
      System.err.println("Client UUID not found for client ID: " + clientId);
    }
  }

  @Override
  public List<RoleRepresentation> getClientRoles(String clientId) {
    ClientsResource clientsResource = keycloak.realm(realm).clients();
    List<ClientRepresentation> clients = clientsResource.findByClientId(clientId);

    if (!clients.isEmpty()) {
      String clientUUID = clients.get(0).getId(); // Get the first matching client's UUID
      RolesResource rolesResource = clientsResource.get(clientUUID).roles(); // Access the RolesResource
      return rolesResource.list(); // Retrieve all roles for this client
    }

    return Collections.emptyList(); // Return empty list if client not found
  }

  private String getClientUUID(String clientId) {
    ClientsResource clientsResource = keycloak.realm(realm).clients();
    List<ClientRepresentation> clients = clientsResource.findByClientId(clientId);

    if (!clients.isEmpty()) {
      return clients.get(0).getId(); // Return the first matching client's UUID
    }

    return null; // Client ID not found
  }

  @Override
  public UserRecord getUser(String userId) {
    UsersResource usersResource = getUsersResource();
    UserRepresentation userRepresentation = usersResource.get(userId).toRepresentation();

    if (userRepresentation != null) {
      UserRecord userRecord = new UserRecord(
          userRepresentation.getId(),
          userRepresentation.getUsername(),
          userRepresentation.getEmail(),
          userRepresentation.getFirstName(),
          userRepresentation.getLastName(),
          "");
      return userRecord; // Return the user found
    }

    return null; // No user found with the given ID
  }

  // TODO
  @Override
  public void emailVerification(String userId) {
    // UsersResource usersResource = getUsersResource();
    // usersResource.get(userId).sendVerifyEmail();
  }

  private static UserRepresentation getUserRepresentation(UserRecord userRecord) {
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(userRecord.username());
    user.setEmail(userRecord.email());
    user.setFirstName(userRecord.firstName());
    user.setLastName(userRecord.lastName());

    // TODO: Need to change to false after implementing email verification
    user.setEmailVerified(true);

    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setValue(userRecord.password());
    credentialRepresentation.setTemporary(false);
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

    List<CredentialRepresentation> list = new ArrayList<>();
    list.add(credentialRepresentation);
    user.setCredentials(list);
    return user;
  }

  private UsersResource getUsersResource() {
    RealmResource realm1 = keycloak.realm(realm);
    return realm1.users();
  }

  @Override
  public Response getAccessToken(AuthRequest authRequest) {
    try {

      Keycloak kBuilder = KeycloakBuilder.builder()
          .serverUrl(authUrl)
          .realm(realm)
          .clientId(client)
          .clientSecret(clientSecret)
          .username(authRequest.getEmail())
          .password(authRequest.getPassword())
          .grantType(OAuth2Constants.PASSWORD)
          .build();

      AccessTokenResponse tokenResponse = kBuilder.tokenManager().grantToken();
      return Response.ok(tokenResponse).build();
    } catch (Exception e) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Failed to obtain access token: " + e.getMessage())
          .build();
    }
  }

  @Override
  public Response getRefreshToken(AuthRequest authRequest) {
    try {
      Keycloak keycloa = KeycloakBuilder.builder()
          .serverUrl(authUrl)
          .realm(realm)
          .clientId(client)
          .clientSecret(clientSecret)
          .grantType(OAuth2Constants.REFRESH_TOKEN)
          .build();

      AccessTokenResponse tokenResponse = keycloa.tokenManager().grantToken();
      return Response.ok(tokenResponse).build();
    } catch (Exception e) {
      return Response.status(Response.Status.UNAUTHORIZED)
          .entity("Failed to obtain access token: " + e.getMessage())
          .build();
    }
  }

  @Override
  public String getUrl(AuthRequest authRequest) {
    return UriBuilder.fromUri(authUrl)
        .path("/realms/rdp-realm/protocol/openid-connect/auth")
        .queryParam("client_id", client)
        .queryParam("client_secret", clientSecret)
        .queryParam("redirect_uri", authRequest.getRedirectUrl())
        .queryParam("response_type", "code")
        .queryParam("scope", "openid")
        .queryParam("kc_idp_hint", authRequest.getIdpHint())
        .build(realm)
        .toString();
  }

  @Override
  public Response getSocialToken(AuthRequest authRequest) {
  
    // Create an HTTP client
    Client clientBuilder = ClientBuilder.newClient();

    // Create the form with required parameters for token exchange
    Form form = new Form();
    form.param("grant_type", "authorization_code");
    form.param("code", authRequest.getAuthorizationCode());
    form.param("client_id", client);
    form.param("client_secret", clientSecret);
    form.param("redirect_uri", "http://localhost:3000/callback");

    // Send the POST request to Keycloak's token endpoint
    Response response = clientBuilder
        .target("https://auth.skndan.com:8443/realms/rdp-realm/protocol/openid-connect/token")
        .request(MediaType.APPLICATION_FORM_URLENCODED)
        .post(Entity.form(form));

    String jsonResponse = response.readEntity(String.class);

    // Convert JSON string to AccessTokenResponse using ObjectMapper
    AccessTokenResponse accessTokenResponse;
    try {
      accessTokenResponse = mapper.readValue(jsonResponse, AccessTokenResponse.class);
      Profile profile = parseAccessToken(accessTokenResponse);
      return Response.ok(accessTokenResponse).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST)
          .build();
    }
  }

  private Profile parseAccessToken(AccessTokenResponse accessToken) {
        // Decode the JWT token to extract claims (email, first name, last name)
        String[] parts = accessToken.getToken().split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        // Decode the payload part of the JWT token
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        // Convert the payload JSON string to a JSON object
        JsonObject jsonObject = Json.createReader(new StringReader(payload)).readObject();

        // Extract user information from claims
        String email = jsonObject.getString("preferred_username", null);
        String firstName = jsonObject.getString("given_name", null);
        String lastName = jsonObject.getString("family_name", null);
        String userId = jsonObject.getString("sub", null); 

        SignUpRequest dept = new SignUpRequest();
        dept.setEmail(email);
        dept.setFirstName(firstName);
        dept.setLastName(lastName);
        dept.setUserId(userId);
        dept.setSocial(true);

        Profile profile = authService.createProfile(dept);
        // Create and return a UserProfile object or any other structure you want
        return profile;
    }
}
