package com.skndan.rdp.service.keycloak;

import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;

import com.skndan.rdp.model.AuthRequest;
import com.skndan.rdp.model.UserRecord;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public interface KeycloakService {

  String createUser(UserRecord userRecord);

  UserRecord getUser(String userId);

  UserRecord updateUser(String userId, UserRecord userRecord);

  void assignRolesToUser(String userId, List<RoleRepresentation> roles, String clientId);

  List<RoleRepresentation> getClientRoles(String clientId);

  // TODO
  void emailVerification(String userId);

  Response getAccessToken(AuthRequest authRequest);
  Response getRefreshToken(AuthRequest authRequest);

  String getUrl(AuthRequest authRequest);
  Response getSocialToken(AuthRequest authRequest);
}
