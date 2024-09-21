package com.skndan.rdp.service.keycloak;

import com.skndan.rdp.model.UserRegistrationRecord;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface KeycloakUserService {

  UserRegistrationRecord createUser(UserRegistrationRecord userRegistrationRecord);

  void emailVerification(String userId);

}
