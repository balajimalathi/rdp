package com.skndan.rdp.service.integration.aws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

@ApplicationScoped
public class Ec2ClientProducer {

  private final AwsCredentialsService awsCredentialsService;

  public Ec2ClientProducer(AwsCredentialsService awsCredentialsService) {
    this.awsCredentialsService = awsCredentialsService;
  }

  @Produces
  @ApplicationScoped
  public Ec2Client ec2Client() {
    AwsCredentialsConfig credentials = awsCredentialsService.fetchAwsCredentials();

    return Ec2Client.builder()
        .region(Region.of(credentials.getRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                credentials.getAccessKeyId(),
                credentials.getSecretAccessKey())))
        .build();
  }
}