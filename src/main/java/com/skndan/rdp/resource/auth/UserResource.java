package com.skndan.rdp.resource.auth;

import com.skndan.rdp.model.AuthResponse;
import com.skndan.rdp.service.ProfileService;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/me")
@Authenticated
public class UserResource {

  @Inject
  SecurityIdentity securityIdentity;

  @Inject
  ProfileService profileService;

  @GET
  public Response resendVerificationEmail() {

    DefaultJWTCallerPrincipal jwtPrincipal = (DefaultJWTCallerPrincipal) securityIdentity.getPrincipal();
    String subject = jwtPrincipal.getSubject(); // Access the subject
    AuthResponse response = profileService.getProfile(subject);
    return Response.ok(response).build();
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
