package com.skndan.rdp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AuthResponse {

  private String profileId;

  private String name;

  private String email;

  private String mobile;

}
