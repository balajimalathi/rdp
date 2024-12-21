package com.skndan.rdp.resource.profile;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.skndan.rdp.config.EntityCopyUtils;
import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.repo.ProfileRepo;
import com.skndan.rdp.service.auth.AuthService;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/profile")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Profiles", description = "Profile Endpoints")
public class ProfileResource {

  @Inject
  ProfileRepo profileRepo;

  @Inject
  AuthService authService;

  @Inject
  EntityCopyUtils entityCopyUtils;

  @GET
  public Response list(
      @QueryParam("pageNo") @DefaultValue("0") int pageNo,
      @QueryParam("pageSize") @DefaultValue("25") int pageSize,
      @QueryParam("sortField") @DefaultValue("name") String sortField,
      @QueryParam("sortDir") @DefaultValue("ASC") String sortDir) {

    Sort sortSt = sortDir.equals("DESC") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

    Page<Profile> user = profileRepo.findAll(PageRequest.of(pageNo, pageSize, sortSt));
    return Response.ok(user).status(200).build();
  }

  @GET
  @Path("/{id}")
  @RolesAllowed({ "user", "admin" })
  public Response getByID(@PathParam("id") UUID id) {
    Optional<Profile> optional = profileRepo.findById(id);

    if (optional.isPresent()) {
      Profile profile = optional.get();
      return Response.ok(profile).status(200).build();
    }

    throw new IllegalArgumentException("No profile with id " + id + " exists");
  }

  @POST
  @Transactional
  public Response add(SignUpRequest profile) {
    // if (profile.id != null) {
    // throw new WebApplicationException("Id was invalidly set on request.", 422);
    // }

    Profile pro = authService.createProfile(profile);
    return Response.ok(pro).status(201).build();
  }

  @DELETE
  @Path("/{id}")
  @Transactional
  public Response delete(@PathParam("id") UUID id) {
    Profile entity = profileRepo.findById(id)
        .orElseThrow(() -> new WebApplicationException("profile with id of " + id + " does not exist.", 404));
    entity.setActive(false);
    profileRepo.save(entity);
    return Response.status(204).build();
  }

  // @PUT
  // @Path("/{id}")
  // public Response update(@PathParam("id") UUID id, Profile greeting) {
  // Optional<Profile> optional = profileRepo.findById(id);

  // if (optional.isPresent()) {
  // Profile profile = optional.get();
  // entityCopyUtils.copyProperties(profile, greeting);
  // profile.setUser(profileService.updateUser(profile));
  // Profile updateUsers = profileRepo.save(profile);
  // return Response.ok(updateUsers).status(200).build();
  // }

  // throw new IllegalArgumentException("No profile with id " + id + " exists");
  // }

  @GET
  @Path("/get-by-user/{id}")
  public Response get_by_user(@PathParam("id") String id) {
    Optional<Profile> profile = profileRepo.findByUserId(id);
    return Response.ok(profile.get()).status(200).build();
  }
}
