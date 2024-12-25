package com.skndan.rdp.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LabelValue {
  private String value;
  private String label;

  public LabelValue(String value, String label) {
    this.value = value;
    this.label = label;
  }
}