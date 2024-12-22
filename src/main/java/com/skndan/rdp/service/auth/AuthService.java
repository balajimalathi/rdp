package com.skndan.rdp.service.auth;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.keycloak.representations.idm.RoleRepresentation;

import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.model.UserRecord;
import com.skndan.rdp.repo.ProfileRepo;
import com.skndan.rdp.service.keycloak.KeycloakService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

@ApplicationScoped
public class AuthService {

  @Inject
  private KeycloakService keycloakService;

  @Inject
  private ProfileRepo profileRepo;

  public Profile createProfile(SignUpRequest dept) {

    String userId = "";

    if (dept.isSocial()) {
      userId = dept.getUserId();
      setDefaultRole(dept);
      return setProfile(dept, userId);
    } else {
      UserRecord user = new UserRecord(
          "", dept.getEmail(), dept.getEmail(), dept.getFirstName(), dept.getLastName(), dept.getPassword());
      // update keycloak
      userId = keycloakService.createUser(user);
      dept.setUserId(userId);
      setDefaultRole(dept);
      return setProfile(dept, userId);
    }

    // mailService.sendVendorRegistrationMail(dept, user.getPassword());

  }

  private void setDefaultRole(SignUpRequest dept) {

    String defaultRole = "becff04f-e159-4dc1-8b43-13b169d8e482"; // user

    String roleToFilter = dept.getRoleId() != null ? dept.getRoleId() : defaultRole;

    List<RoleRepresentation> clientRoles = keycloakService.getAllRoles();

    List<RoleRepresentation> filteredRoles = clientRoles.stream()
        .filter(role -> role.getId().equals(roleToFilter))
        .collect(Collectors.toList());

    // keycloakService.assignRolesToUser(dept.getUserId(), filteredRoles);
  }

  public Profile setProfile(SignUpRequest dept, String userId) {
    Profile profile = new Profile();
    profile.setName((dept.getFirstName() + " " + dept.getLastName()).trim());
    profile.setEmail(dept.getEmail());
    profile.setMobile(dept.getMobile());
    profile.setUserId(userId);

    // save profile
    Optional<Profile> existingProfile = profileRepo.findByUserId(userId);

    // user is already present
    if (existingProfile.isPresent()) {
      Profile _profile = existingProfile.get();
      return _profile;
    } else {
      try {
        // user not present
        Optional<Profile> existingProfile2 = profileRepo.findByEmail(dept.getEmail());

        // but profile is exist with email
        if (existingProfile2.isPresent()) {
          profile.setUserId(userId);
          profile = profileRepo.save(profile);
        } else {
          // but profile is not exists at-all
          profile = profileRepo.save(profile);
        }
      } catch (ConstraintViolationException e) {
        // Assuming the mobile number field is causing the unique constraint violation
        if (e.getMessage() != null && e.getMessage().contains("mobile_number")) {
          throw new GenericException(400, "Mobile number already exists.");
        }
        throw new GenericException(400, "An unknown error occurred while saving the user.");
      }
    }

    return profile;
  }

}
