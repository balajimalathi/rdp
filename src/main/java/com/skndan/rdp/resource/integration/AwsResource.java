package com.skndan.rdp.resource.integration;

import java.util.List;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.skndan.rdp.model.LabelValue;
import com.skndan.rdp.model.aws.AmiDetails;
import com.skndan.rdp.model.aws.AmiRequestDto;
import com.skndan.rdp.model.aws.AmiResponse;
import com.skndan.rdp.model.aws.InstanceRequestDto;
import com.skndan.rdp.model.aws.InstanceStateRequest;
import com.skndan.rdp.model.aws.InstanceStateResponse;
import com.skndan.rdp.model.aws.KeyPairDetails;
import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.service.integration.AwsService;
import com.skndan.rdp.service.integration.aws.AwsRegionService;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/aws")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "AWS", description = "Aws Endpoints")
public class AwsResource {

  @Inject
  AwsRegionService awsRegionService;

  @Inject
  AwsService awsService;

  @GET
  @Path("/regions")
  public Response getRegions() {
    List<LabelValue> regions = awsRegionService.getAvailableRegions();
    return Response.ok(regions).status(200).build();
  }

  @POST
  @Path("/instance")
  public Response createInstance(InstanceRequestDto request) {
    Instance instance = awsService.createEc2Instance(request);
    return Response.ok(instance).status(201).build();
  }

  @GET
  @Path("/instance")
  public Response getInstance() {
    List<Instance> instances = awsService.getInstance();
    return Response.ok(instances).status(200).build();
  }

  @POST
  @Path("/ami")
  public Response createAmi(AmiRequestDto request) {
    AmiResponse amis = awsService.createAmis(request);
    return Response.ok(amis).status(200).build();
  }

  @GET
  @Path("/ami")
  public Response getAmi() {
    List<AmiDetails> amis = awsService.getAmis();
    return Response.ok(amis).status(200).build();
  }

  @POST
  @Path("/state")
  @Transactional
  public Response updateState(InstanceStateRequest request) {
    InstanceStateResponse instanceState = awsService.changeState(request.getInstanceId(), request.getState());
    return Response.ok(instanceState).status(200).build();
  }

  @GET
  @Path("/keypair")
  public Response listKeyPairs() {
    List<KeyPairDetails> response = awsService.getKeyPairs();
    return Response.ok(response).build();
  }
}