package com.skndan.rdp.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.skndan.rdp.service.integration.aws.AwsRegionService;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/test")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Test", description = "Test Endpoints")
public class TestResource {

  @Inject
  AwsRegionService awsRegionService;

  // @GET
  // @Path("/regions")
  // public Response getByID() {
  //   List<String> regions = awsRegionService.getAvailableRegions();
  //   return Response.ok(regions).status(200).build();
  // }
}