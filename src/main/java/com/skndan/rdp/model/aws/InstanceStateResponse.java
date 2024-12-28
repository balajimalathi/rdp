package com.skndan.rdp.model.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InstanceStateResponse {
  private String instanceId;
  private String state;
  private String message;
  private int code;

  // Constructor
  public InstanceStateResponse(String instanceId, String state, String message, int code) {
    this.instanceId = instanceId;
    this.state = state;
    this.message = message;
    this.code = code;
  }

}