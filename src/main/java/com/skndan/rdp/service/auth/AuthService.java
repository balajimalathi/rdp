package com.skndan.rdp.service.auth;

import java.util.Optional;

import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.model.SignUpRequest;
import com.skndan.rdp.model.UserRecord;
import com.skndan.rdp.repo.ProfileRepo;
import com.skndan.rdp.service.keycloak.KeycloakService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
      return setProfile(dept, userId);
    } else {
      UserRecord user = new UserRecord(
          "", dept.getEmail(), dept.getEmail(), dept.getFirstName(), dept.getLastName(), dept.getPassword());
      // update keycloak
      userId = keycloakService.createUser(user);
      return setProfile(dept, userId);
    }

    // mailService.sendVendorRegistrationMail(dept, user.getPassword());

  }

  public Profile setProfile(SignUpRequest dept, String userId) {
    Profile profile = new Profile();
    profile.setName((dept.getFirstName() + " " + dept.getLastName()).trim());
    profile.setEmail(dept.getEmail());
    profile.setMobile(dept.getMobile());
    profile.setUserId(userId);

    // save profile

    Optional<Profile> existingProfile = profileRepo.findByUserId(userId);
    if(existingProfile.isPresent()){
      Profile _profile = existingProfile.get();
      return _profile;
    } else {
      profile = profileRepo.save(profile);
    }
    return profile;
  }

}
