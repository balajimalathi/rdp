package com.skndan.rdp.service.integration.aws;

import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AwsConfigSource implements ConfigSource {

  @Inject
  AwsCredentialsService awsCredentialsService;

  @Override
  public Map<String, String> getProperties() {
    AwsCredentialsConfig credentials = awsCredentialsService.fetchAwsCredentials();

    return Map.of(
        "quarkus.amazon.aws.access-key-id", credentials.getAccessKeyId(),
        "quarkus.amazon.aws.secret-access-key", credentials.getSecretAccessKey(),
        "quarkus.amazon.aws.region", credentials.getRegion());
  }

  @Override
  public String getValue(String name) {
    // Handle custom logic if needed
    return null;
  }

  @Override
  public int getOrdinal() {
    return 100; // Define the order in which the config source is applied
  }

  @Override
  public String getName() {
    return "AwsConfigSource"; // Name of this custom config source
  }

  @Override
  public Set<String> getPropertyNames() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getPropertyNames'");
  }
 
}