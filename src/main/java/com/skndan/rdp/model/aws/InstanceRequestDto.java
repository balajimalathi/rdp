package com.skndan.rdp.model.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InstanceRequestDto {
  private String name;
  private String amiId;
  private String keyName; 
}
