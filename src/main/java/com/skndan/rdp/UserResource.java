package com.skndan.rdp;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET; 
import jakarta.ws.rs.Path; 
import jakarta.ws.rs.core.Response;

@Path("/api/me")
@Authenticated
public class UserResource {
 

  @Inject
  SecurityIdentity securityIdentity;
 
  @GET 
  public Response resendVerificationEmail() {
 
    // keycloakUserService.emailVerification(userId);
    return Response.ok(new User(securityIdentity)).status(200).build();
  }


  public static class User {

    private final String userName;

    User(SecurityIdentity identity) {
        this.userName = identity.getPrincipal().getName();
    }

    public String getUserName() {
        return userName;
    }
}
}
