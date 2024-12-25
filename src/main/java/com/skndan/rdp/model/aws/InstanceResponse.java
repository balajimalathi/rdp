package com.skndan.rdp.model.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InstanceResponse { 
  private String instanceId;
  private String state;
  private String publicDnsName;
  private String publicIpAddress;
  private String privateIpAddress;
  private String instanceType;
  private String imageId;
  private String keyName;
  private String launchTime;
  private String availabilityZone;
}