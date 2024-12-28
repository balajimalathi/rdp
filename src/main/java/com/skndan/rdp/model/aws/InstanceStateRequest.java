package com.skndan.rdp.model.aws;

import com.skndan.rdp.entity.constants.InstanceState;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InstanceStateRequest {
  private String instanceId;
  private InstanceState state;
}