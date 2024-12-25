package com.skndan.rdp.resource;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skndan.rdp.config.EntityCopyUtils;
import com.skndan.rdp.entity.Integration;
import com.skndan.rdp.entity.constants.CloudProvider;
import com.skndan.rdp.repo.IntegrationRepo;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/integration")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Integrations", description = "Integration Endpoints")
public class IntegrationResource {

  @Inject
  IntegrationRepo integrationRepo;

  @Inject
  EntityCopyUtils entityCopyUtils;

  ObjectMapper objectMapper = new ObjectMapper();

  @GET
  public Response list(
      @QueryParam("pageNo") @DefaultValue("0") int pageNo,
      @QueryParam("pageSize") @DefaultValue("25") int pageSize,
      @QueryParam("sortField") @DefaultValue("createdAt") String sortField,
      @QueryParam("sortDir") @DefaultValue("ASC") String sortDir) {

    Sort sortSt = sortDir.equals("DESC") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
    Page<Integration> deptList = integrationRepo.findAll(
        PageRequest.of(pageNo, pageSize, sortSt));
    return Response.ok(deptList).status(200).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response add(Integration integration) {
    // if (dept.id != null) {
    // throw new WebApplicationException("Id was invalidly set on request.", 422);
    // }

    // integrationRepo.save(dept);
    // return Response.ok(dept).status(201).build();

    Optional<Integration> optional = integrationRepo.findOneByCloudProvider(integration.getCloudProvider());

    if (optional.isPresent()) {
      Integration dept = optional.get();
      entityCopyUtils.copyProperties(dept, integration);
      Integration updateDept = integrationRepo.save(dept);
      return Response.ok(updateDept).status(200).build();
    } else {
      integration = integrationRepo.save(integration);
      return Response.ok(integration).status(201).build();
    }
  }

  @GET
  @Path("/{cloudProvider}")
  public Response getByID(@PathParam("cloudProvider") CloudProvider cloudProvider) {
    Optional<Integration> optional = integrationRepo.findOneByCloudProvider(cloudProvider);

    HashMap<String, Object> hashMap = new HashMap<>();
    if (optional.isPresent()) {

      Integration integration = optional.get();

      try {
        hashMap = objectMapper.readValue(integration.getConfiguration(), new TypeReference<HashMap<String, Object>>() {
        });
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }

    return Response.ok(hashMap).status(200).build();
  }
}
