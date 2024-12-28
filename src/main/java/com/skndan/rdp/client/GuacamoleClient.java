package com.skndan.rdp.client;

import java.util.List;

// import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.skndan.rdp.model.guacamole.TokenResponse;

import io.vertx.mutiny.ext.web.Session;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// @RegisterRestClient
@Path("/guacamole/api")
public interface GuacamoleClient {

  @POST
  @Path("/tokens")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TokenResponse authenticate(@FormParam("username") String username,
      @FormParam("password") String password);

  @GET
  @Path("/sessions")
  @Produces(MediaType.APPLICATION_JSON)
  List<Session> listSessions(@HeaderParam("Authorization") String token);
}