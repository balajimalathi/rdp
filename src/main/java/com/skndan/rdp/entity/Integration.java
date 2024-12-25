package com.skndan.rdp.entity;

import com.skndan.rdp.entity.constants.CloudProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Integration extends BaseEntity {

  @Enumerated(value = EnumType.STRING)
  private CloudProvider cloudProvider;

  @Column(columnDefinition = "text")
  private String configuration;
}
