package com.skndan.rdp.service.keycloak;

import java.io.StringReader;
import java.util.Base64;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.model.AuthRequest;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.model.UserRecord;
import com.skndan.rdp.service.auth.AuthService;

import jakarta.enterprise.context.ApplicationScoped;
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

@ApplicationScoped
public class KeycloakService {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "keycloak.urls.auth")
    private String authUrl;

    @ConfigProperty(name = "keycloak.realm")
    private String realm;

    @ConfigProperty(name = "keycloak.client")
    String clientId;

    @ConfigProperty(name = "keycloak.clientSecret")
    private String clientSecret;

    @ConfigProperty(name = "keycloak.apiClientId")
    String apiClientId;

    @ConfigProperty(name = "keycloak.apiClientSecret")
    private String apiClientSecret;

    @Inject
    AuthService authService;

    ObjectMapper mapper = new ObjectMapper();

    public String createUser(UserRecord userRecord) {

        UserRepresentation userExists = getUserByEmail(userRecord.email());

        if (userExists != null) {
            System.out.println("User found: " + userExists.getEmail());
            return userExists.getId();
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRecord.username());
        user.setEmail(userRecord.email());
        user.setFirstName(userRecord.firstName());
        user.setLastName(userRecord.lastName());
        user.setEmailVerified(true);
        user.setEnabled(true);

        // Create the user
        var usersResource = keycloak.realm(realm).users();
        var response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user: " + response.getStatusInfo());
        }

        // Retrieve user ID
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userRecord.password());
        credential.setTemporary(false);

        usersResource.get(userId).resetPassword(credential);

        return userId;
    }

    public List<RoleRepresentation> getAllRoles() {
        // Retrieve the client resource using the client ID
        ClientRepresentation clientResource = keycloak.realm(realm).clients().findByClientId(clientId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client not found: " + "horizon-ui"));

        // Fetch and return roles for the client
        return keycloak.realm(realm).clients().get(clientResource.getId()).roles().list();
    }

    public void updateUser(String userId, String email, String firstName, String lastName) {
        var usersResource = keycloak.realm(realm).users();
        UserResource userResource = usersResource.get(userId);

        // Fetch the existing user representation
        UserRepresentation user = userResource.toRepresentation();

        // Update the fields
        if (email != null)
            user.setEmail(email);
        if (firstName != null)
            user.setFirstName(firstName);
        if (lastName != null)
            user.setLastName(lastName);

        // Update the user
        userResource.update(user);
    }

    public void resetPassword(String userId, String newPassword) {
        var usersResource = keycloak.realm(realm).users();
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(newPassword);
        credential.setTemporary(false);

        usersResource.get(userId).resetPassword(credential);
    }

    public void assignClientRoles(String userId, List<RoleRepresentation> roles) {
        var clientResource = keycloak.realm(realm).clients().findByClientId(clientId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        var clientIdKey = clientResource.getId();
        keycloak.realm(realm).users()
                .get(userId)
                .roles()
                .clientLevel(clientIdKey)
                .add(roles);
    }

    public void unassignClientRoles(String userId, List<RoleRepresentation> roles) {
        var clientResource = keycloak.realm(realm).clients().findByClientId(clientId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        var clientIdKey = clientResource.getId();
        keycloak.realm(realm).users()
                .get(userId)
                .roles()
                .clientLevel(clientIdKey)
                .remove(roles);
    }

    public UserRepresentation getUserById(String userId) {
        var usersResource = keycloak.realm(realm).users();
        return usersResource.get(userId).toRepresentation();
    }

    public UserRepresentation getUserByEmail(String email) {
        UsersResource usersResource = keycloak.realm(realm).users();

        // Search for users by email
        List<UserRepresentation> users = usersResource.search(null, null, null, email, null, null);

        // Check if user exists
        if (users != null && !users.isEmpty()) {
            // Returning the first matched user (Keycloak ensures emails are unique per
            // realm)
            return users.get(0);
        }

        // Return null if no user is found
        return null;
    }

    public List<UserRepresentation> getAllUsers() {
        return keycloak.realm(realm).users().list();
    }

    public List<RoleRepresentation> getAssignedClientRoles(String userId) {
        // Fetch the client using the clientId
        var clientResource = keycloak.realm(realm).clients().findByClientId(clientId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        // Fetch roles assigned to the user for the specified client
        return keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .clientLevel(clientResource.getId())
                .listEffective(); // Use listEffective to include inherited roles
    }

    public Response getSocialToken(AuthRequest authRequest) {

        // Create an HTTP client
        Client clientBuilder = ClientBuilder.newClient();

        // Create the form with required parameters for token exchange
        Form form = new Form();
        form.param("grant_type", "authorization_code");
        form.param("code", authRequest.getAuthorizationCode());
        form.param("client_id", apiClientId);
        form.param("client_secret", apiClientSecret);
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

    public String getUrl(AuthRequest authRequest) {
        return UriBuilder.fromUri(authUrl)
                .path("/realms/rdp-realm/protocol/openid-connect/auth")
                .queryParam("client_id", apiClientId)
                .queryParam("client_secret", apiClientSecret)
                .queryParam("redirect_uri", authRequest.getRedirectUrl())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid")
                .queryParam("kc_idp_hint", authRequest.getIdpHint())
                .build(realm)
                .toString();
    }

}