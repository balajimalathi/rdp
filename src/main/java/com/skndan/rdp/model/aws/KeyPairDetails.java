package com.skndan.rdp.model.aws;

import lombok.Data;

@Data
public class KeyPairDetails {
  private final String keyName;
  private final String keyFingerprint;

  public KeyPairDetails(String keyName, String keyFingerprint) {
    this.keyName = keyName;
    this.keyFingerprint = keyFingerprint;
  }
}
