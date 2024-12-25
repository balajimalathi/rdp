package com.skndan.rdp.model.aws;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

@Getter
@Setter
@Data
public class AmiResponse {
  private String amiId;
  private String message;

  public AmiResponse(String amiId, String message) {
    this.amiId = amiId;
    this.message = message;
  }
}