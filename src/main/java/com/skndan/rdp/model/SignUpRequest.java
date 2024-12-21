package com.skndan.rdp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SignUpRequest {
  private String userId;
  private String roleId = null;
  private String firstName;
  private String lastName;
  private String email;
  private String mobile;
  private String password;
  private boolean social = false;
}
