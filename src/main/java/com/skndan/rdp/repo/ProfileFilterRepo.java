package com.skndan.rdp.repo;

import com.skndan.rdp.entity.Profile;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProfileFilterRepo extends BaseRepo<Profile> {

  public ProfileFilterRepo() {
    super(Profile.class);
  }
}
