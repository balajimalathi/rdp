package com.skndan.rdp.service.integration.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AwsCredentialsConfig {

  private String accessKeyId;

  private String secretAccessKey;

  private String region;

  @Override
  public String toString() {
    return "AwsCredentialsConfig{" +
        "accessKeyId='" + accessKeyId + '\'' +
        ", secretAccessKey='" + secretAccessKey + '\'' +
        ", region='" + region + '\'' +
        '}';
  }
}
