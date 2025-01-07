package com.skndan.rdp.client;

import java.util.Objects;

import com.skndan.rdp.entity.Instance;

public class InstanceValidator {

  public static void validateInstance(Instance instance) {
    Objects.requireNonNull(instance.getInstanceId(), "Instance ID must not be null");
    Objects.requireNonNull(instance.getPublicIpAddress(), "Public IP address must not be null");
    Objects.requireNonNull(instance.getPassword(), "Password must not be null");
    Objects.requireNonNull(instance.getUsername(), "Username must not be null");
    Objects.requireNonNull(instance.getPublicDnsName(), "Public DNS name must not be null");
    Objects.requireNonNull(instance.getImageId(), "Image ID must not be null");
    Objects.requireNonNull(instance.getKeyName(), "Key name must not be null");
    Objects.requireNonNull(instance.getInstanceType(), "Instance type must not be null");
    Objects.requireNonNull(instance.getLaunchTime(), "Launch time must not be null");
    Objects.requireNonNull(instance.getAvailabilityZone(), "Availability zone must not be null"); 
  }
}