package com.skndan.rdp.client;

import java.time.LocalDateTime;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.model.guacamole.Attributes;
import com.skndan.rdp.model.guacamole.Connection;
import com.skndan.rdp.model.guacamole.Parameters;
import com.skndan.rdp.model.guacamole.TokenResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class GuacamoleService {

  @Inject
  @RestClient
  GuacamoleClient guacamoleClient;

  @ConfigProperty(name = "guacamole.username")
  String username;

  @ConfigProperty(name = "guacamole.password")
  String password;

  @ConfigProperty(name = "guacamole.dataSource")
  String dataSource;

  private String authToken;

  private LocalDateTime tokenExpiry;

  public String authenticate() {
    if (isTokenExpired()) {
      refreshToken();
    }

    return authToken;
  }
  
  private boolean isTokenExpired() {
    return authToken == null ||
        LocalDateTime.now().isAfter(tokenExpiry);
  }

  public void refreshToken() {
    try {
      TokenResponse clientReponse = guacamoleClient.authenticate(username, password);
      System.out.println(clientReponse.getAuthToken());
      this.authToken = clientReponse.getAuthToken();
      this.tokenExpiry = LocalDateTime.now().plusMinutes(60);
      GuacamoleAuthRequestFilter.setToken(this.authToken);
    } catch (Exception e) {
      throw new RuntimeException("Authentication Failed", e);
    }
  }

  // create connection
  @Transactional
  public Connection createConnection(Instance instance) {

    Connection connection = new Connection();
    connection.setName(instance.getInstanceId());
    connection.setParentIdentifier("ROOT");
    connection.setProtocol("rdp");

    Attributes attributes = new Attributes();
    attributes.setGuacdEncryption("none");
    
    connection.setAttributes(attributes);
    
    Parameters parameters = new Parameters();
    parameters.setHostname(instance.getPublicIpAddress());
    parameters.setPassword(instance.getPassword());
    parameters.setSecurity("nla");
    parameters.setIgnoreCert("true");
    parameters.setPort("3389");
    parameters.setDomain(instance.getPublicDnsName());
    parameters.setUsername(instance.getUsername());
    
    connection.setParameters(parameters);
    
    // Check the authentication
    authenticate();

    // WebClient call 
    Connection connectionReponse = guacamoleClient.createConnection(dataSource, connection);

    return connectionReponse;

  }
  // get tunnel
}
