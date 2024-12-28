package com.skndan.rdp.client;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GuacamoleConfig {
  @ConfigProperty(name = "guacamole.api.url")
  public String apiUrl;

  @ConfigProperty(name = "guacamole.username")
  public String username;

  @ConfigProperty(name = "guacamole.password")
  public String password;

  @ConfigProperty(name = "guacamole.reauth.buffer", defaultValue = "60")
  public int reauthBufferSeconds; // Token reauthentication buffer time
}
