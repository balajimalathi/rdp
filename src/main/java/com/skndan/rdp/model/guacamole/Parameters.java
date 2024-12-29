package com.skndan.rdp.model.guacamole;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Parameters{
  
  public String hostname;
  
  public String password;
  
  public String security;
    
  @JsonProperty("ignore-cert")
  public String ignoreCert;
  
  public String port;
  
  public String domain;
  
  public String username;
}

