package com.skndan.rdp.model.aws;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AmiDetails {
  private String imageId;
  private String name;
  private String description;
  private String state;

  public AmiDetails(String imageId, String name, String description, String state) {
      this.imageId = imageId;
      this.name = name;
      this.description = description;
      this.state = state;
  }
}