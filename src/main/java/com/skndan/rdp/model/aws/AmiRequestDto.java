package com.skndan.rdp.model.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class AmiRequestDto {
  private String instanceId;
  private String name;
  private String description;
  private boolean noReboot;
}
