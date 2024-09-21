package com.skndan.rdp;

import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import com.skndan.rdp.model.UserRegistrationRecord;
import com.skndan.rdp.service.keycloak.KeycloakUserService;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/api/admin")
public class RoleResource {

  @Inject
  private KeycloakUserService keycloakUserService;

  @Inject
  SecurityIdentity securityIdentity;

  @POST
  public UserRegistrationRecord createUser(@RequestBody UserRegistrationRecord userRegistrationRecord) {
    return keycloakUserService.createUser(userRegistrationRecord);
  }

  @GET
  @Path("/me")
  public Response resendVerificationEmail() {

    String username = securityIdentity.getAttribute("preferred_username");
    // keycloakUserService.emailVerification(userId);
    return Response.ok(securityIdentity).status(200).build();
  }

}
