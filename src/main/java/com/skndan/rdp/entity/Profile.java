package com.skndan.rdp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Profile extends BaseEntity {
  
  @NotBlank(message = "First name should be present")
  private String firstName;

  @NotBlank(message = "Last name should be present")
  private String lastName;

  @Email
  @Column(unique = true)
  private String email;

  @Column(unique = true)
  private String mobile;

  @Column(unique = true)
  private String userId;

  @Column(unique = true)
  private String fcm;

  private String roleId;

}
