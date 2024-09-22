package com.skndan.rdp.resource.auth;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.model.AuthRequest;
import com.skndan.rdp.model.AuthResponse;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.model.UserRecord;
import com.skndan.rdp.service.auth.AuthService;
import com.skndan.rdp.service.keycloak.KeycloakService;

import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.vertx.mutiny.ext.web.client.WebClient;

@Path("/api/v1/auth")
@PermitAll
public class AuthResource {

  @Inject
  private KeycloakService keycloakService;

  @Inject
  SecurityIdentity securityIdentity;

  private final WebClient webClient;

  @Inject
  private AuthService authService;

  public AuthResource(Vertx vertx) {
    this.webClient = WebClient.create(vertx);
  }
  // TODO
  // [X] Username and password authentication
  // Refresh token
  // [X] Create user
  // Update user
  // Assign roles

  @POST
  @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique ID")
  @APIResponses({
      @APIResponse(responseCode = "204", description = "Reservation confirmed"),
      @APIResponse(responseCode = "422", description = "Reservation can not be confirmed", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GenericException.class)))
  })

  public Response createUser(@RequestBody UserRecord userRegistrationRecord) {
    return Response.ok(keycloakService.createUser(userRegistrationRecord)).build();
  }

  @POST
  @Path("/social")
  @Operation(summary = "Social Login", description = "Returns the auth url string")
  public Response google(@RequestBody AuthRequest authRequest) {
    String uri = keycloakService.getUrl(authRequest); 
    AuthResponse authResponse = new AuthResponse();
    authResponse.setUrl(uri);
    authResponse.setSocial("google");
    return Response.ok(authResponse).build();
  }

  @POST
  @Path("/callback") 
  public Response handleGoogleCallback(@RequestBody AuthRequest authRequest) { 
    return keycloakService.getSocialToken(authRequest);
  }

  @POST
  @Path("/login")
  public Response getToken(@RequestBody AuthRequest authRequest) {
    return keycloakService.getAccessToken(authRequest);
  }

  @POST
  @Path("/sign-up")
  @Transactional
  public Response add(SignUpRequest dept) {
    Profile profile = authService.createProfile(dept);
    return Response.ok(profile).status(201).build();
  }
 
  @POST
  @Path("/refresh")
  public Uni<Response> refreshAccessToken(@RequestBody AuthRequest request) {
    String keycloakUrl = "https://auth.skndan.com:8443/auth/realms/rdp-realm/protocol/openid-connect/token";

    return webClient.postAbs(keycloakUrl)
        .addQueryParam("client_id", "rdp-dev")
        .addQueryParam("grant_type", "refresh_token")
        .addQueryParam("refresh_token", request.getRefreshToken())
        .send()
        .onItem().transform(response -> {
          if (response.statusCode() == 200) {
            return Response.ok(response.bodyAsJsonObject()).build();
          } else {
            return Response.status(response.statusCode()).entity(response.bodyAsJsonObject()).build();
          }
        });
  }

}
