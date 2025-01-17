package com.skndan.rdp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
/**
 * Represents an EC2 instance managed by AWS.
 * Extends the {@link BaseEntity} to include common entity attributes such as
 * ID, timestamps, etc.
 * This class is mapped to a database table and stores details of EC2 instances.
 */
public class Ami extends BaseEntity {
  /**
   * The name of the EC2 instance.
   * This field is mapped to a database column with a unique constraint.
   */
  @Column(unique = true)
  private String name;

  /**
   * The unique identifier of the EC2 instance.
   * This field is mapped to a database column with a unique constraint.
   */
  @Column(unique = true)
  private String instanceId;

  private String username;

  private String imageId;

  private String password;

  private String description;
  
  private String state;
}
