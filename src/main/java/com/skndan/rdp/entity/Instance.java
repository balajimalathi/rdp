package com.skndan.rdp.entity;

import com.skndan.rdp.entity.constants.CloudProvider;
import com.skndan.rdp.entity.constants.Platform;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Instance extends BaseEntity {
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

  /**
   * The current state of the EC2 instance.
   * Example values: "running", "stopped", "terminated".
   */
  private String state;

  /**
   * The public DNS name of the EC2 instance.
   * This is null if the instance does not have a public IP address.
   */
  private String publicDnsName;

  /**
   * The public IP address of the EC2 instance.
   * This is null if the instance does not have a public IP address.
   */
  private String publicIpAddress;

  /**
   * The private IP address of the EC2 instance.
   * This is used for communication within the same network.
   */
  private String privateIpAddress;

  /**
   * The type of the EC2 instance.
   * Example values: "t2.micro", "m5.large".
   */
  private String instanceType;

  /**
   * The unique identifier of the AMI (Amazon Machine Image) used to launch the
   * instance.
   */
  private String imageId;

  /**
   * The name of the key pair associated with the EC2 instance.
   * Used for SSH access to the instance.
   */
  private String keyName;

  /**
   * The launch time of the EC2 instance, stored as a string.
   * Example format: "2024-12-28T12:34:56Z".
   */
  private String launchTime;

  /**
   * The availability zone where the EC2 instance is hosted.
   * Example values: "us-east-1a", "eu-west-1b".
   */
  private String availabilityZone;

  /**
   * The cloud provider associated with the EC2 instance.
   * This field is stored as a string in the database.
   * Example: {@link CloudProvider#AMAZON_WEB_SERVICE}.
   */
  @Enumerated(value = EnumType.STRING)
  private CloudProvider provider;

  /**
   * Platform of the EC2 instance.
   * This field is stored as a string in the database.
   * Example: {@link Platform#WINDOWS}.
   */
  @Enumerated(value = EnumType.STRING)
  private Platform platform = Platform.WINDOWS;

  private String username;

  private String password;

  private Boolean encrypted;

  private String guacamoleIdentifier;
}
