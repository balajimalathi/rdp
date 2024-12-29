package com.skndan.rdp.client;

import java.util.List;

import org.apache.http.conn.ConnectionRequest;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.skndan.rdp.model.guacamole.Connection;
import com.skndan.rdp.model.guacamole.TokenResponse;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "https://127.0.0.1:8443/api", configKey = "guacamole-api")
@RegisterProvider(GuacamoleAuthRequestFilter.class)
public interface GuacamoleClient {

  @POST
  @Path("/tokens")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  TokenResponse authenticate(@FormParam("username") String username,
      @FormParam("password") String password);

  @GET
  @Path("/session/data/{dataSource}/connections")
  @Produces(MediaType.APPLICATION_JSON)
  List<Connection> listConnections(@PathParam("dataSource") String dataSource);

  @POST
  @Path("/session/data/{dataSource}/connections")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Connection createConnection(@PathParam("dataSource") String dataSource,
      Connection connectionRequest);
}