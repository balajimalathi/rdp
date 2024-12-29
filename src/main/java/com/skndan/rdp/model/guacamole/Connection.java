package com.skndan.rdp.model.guacamole;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Embedded;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Connection {
  
  public String name;
  
  public String identifier;
  
  @JsonProperty("parent-identifier")
  public String parentIdentifier;
  
  public String protocol;

  public Attributes attributes;
  
  public Parameters parameters;
}

