package com.skndan.rdp.model.guacamole;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class Attributes {
  @JsonProperty("guacd-encryption")
  public String guacdEncryption;
}
