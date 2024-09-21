package com.skndan.rdp.service.keycloak;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.skndan.rdp.model.UserRegistrationRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService {

  @Value("${keycloak.realm}")
  private String realm;
  private final Keycloak keycloak;

  public KeycloakUserServiceImpl(Keycloak keycloak) {
    this.keycloak = keycloak;
  }

  @Override
  public UserRegistrationRecord createUser(UserRegistrationRecord userRegistrationRecord) {

    UserRepresentation user = getUserRepresentation(userRegistrationRecord);

    UsersResource usersResource = getUsersResource();

    Response response = usersResource.create(user);

    System.out.println("Status " + response.getStatus());
    if (Objects.equals(201, response.getStatus())) {

      // List<UserRepresentation> representationList = usersResource.searchByUsername(userRegistrationRecord.username(),
      //     true);
      // if (!CollectionUtils.isEmpty(representationList)) {
      //   UserRepresentation userRepresentation1 = representationList.stream()
      //       .filter(userRepresentation -> Objects.equals(false, userRepresentation.isEmailVerified())).findFirst()
      //       .orElse(null);
      //   assert userRepresentation1 != null;

      //   System.out.println(userRepresentation1.getEmail());
      //   // emailVerification(userRepresentation1.getId());
      // }
      return userRegistrationRecord;
    }

    return null;
  }

  private static UserRepresentation getUserRepresentation(UserRegistrationRecord userRegistrationRecord) {
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(userRegistrationRecord.username());
    user.setEmail(userRegistrationRecord.email());
    user.setFirstName(userRegistrationRecord.firstName());
    user.setLastName(userRegistrationRecord.lastName());
    user.setEmailVerified(true);

    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setValue(userRegistrationRecord.password());
    credentialRepresentation.setTemporary(false);
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);

    List<CredentialRepresentation> list = new ArrayList<>();
    list.add(credentialRepresentation);
    user.setCredentials(list);
    return user;
  }

  private UsersResource getUsersResource() {
    RealmResource realm1 = keycloak.realm(realm);
    return realm1.users();
  }

  @Override
  public void emailVerification(String userId) {

    UsersResource usersResource = getUsersResource();
    // usersResource.get(userId).sendVerifyEmail();
  }

}
