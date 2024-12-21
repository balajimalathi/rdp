package com.skndan.rdp.resource.auth;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.keycloak.representations.idm.RoleRepresentation;

import com.skndan.rdp.service.keycloak.KeycloakService;

@Path("/api/v1/role")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Roles Resource", description = "Operations on roles resource.")
public class RoleResource {

  @Inject
  private KeycloakService keycloakService;

  @GET
  @Operation(summary = "Get all roles")
  public Response getRoles() {
    List<RoleRepresentation> clientRoles = keycloakService.getAllRoles();
    return Response.ok(clientRoles).build();
  }
}
