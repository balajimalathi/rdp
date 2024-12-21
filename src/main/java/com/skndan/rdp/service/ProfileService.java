package com.skndan.rdp.service;

import com.skndan.rdp.entity.Profile;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.model.AuthResponse;
import com.skndan.rdp.repo.ProfileRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProfileService {

  @Inject
  ProfileRepo profileRepo;

  public AuthResponse getProfile(String sub) {
    AuthResponse response = new AuthResponse();

    Profile profile = profileRepo.findByUserId(sub).orElseThrow(() -> new GenericException(400, "User not found"));

    if(!profile.getActive()) {
      throw new GenericException(400, "Your account is inactive. Please contact the administrator");
    }

    response.setProfileId(profile.getId().toString());
    response.setName(profile.getName());
    response.setEmail(profile.getEmail());
    response.setMobile(profile.getMobile());
    
    return response;
  }
}
