package com.skndan.rdp.model.guacamole;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenResponse {
  private String authToken;

  private String username;

  private int expiresIn;
  
  private String dataSource;
}
