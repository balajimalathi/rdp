package com.skndan.rdp.service.integration;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateImageRequest;
import software.amazon.awssdk.services.ec2.model.CreateImageResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

import java.util.List;
import java.util.stream.Collectors;

import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.model.aws.AmiDetails;
import com.skndan.rdp.model.aws.AmiRequestDto;
import com.skndan.rdp.model.aws.AmiResponse;
import com.skndan.rdp.model.aws.InstanceResponse;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class AwsService {

  @Inject
  Ec2Client ec2Client;

  public List<InstanceResponse> getInstance() {
    DescribeInstancesResponse response = ec2Client.describeInstances();

    return response.reservations().stream()
        .flatMap(reservation -> reservation.instances().stream())
        .map(instance -> {
          InstanceResponse res = new InstanceResponse();
          res.setInstanceId(instance.instanceId());
          res.setState(instance.state().name().name());
          res.setPublicDnsName(instance.publicDnsName()); 
          res.setPublicIpAddress(instance.publicIpAddress());
          res.setPrivateIpAddress(instance.privateIpAddress());
          res.setInstanceType(instance.instanceTypeAsString());
          res.setImageId(instance.imageId());
          res.setKeyName(instance.keyName());
          res.setLaunchTime(instance.launchTime().toString());
          res.setAvailabilityZone(instance.placement().availabilityZone()); 
          return res;
        })
        .collect(Collectors.toList());
  }

  public String createEc2Instance() {

    // Create EC2 instance
    RunInstancesRequest runRequest = RunInstancesRequest.builder()
        .imageId("ami-xxxxxxxxxx") // Replace with appropriate AMI ID
        .instanceType(InstanceType.T2_MICRO)
        .minCount(1)
        .maxCount(1)
        .keyName("your-key-pair")
        .build();

    RunInstancesResponse runResponse = ec2Client.runInstances(runRequest);
    String instanceId = runResponse.instances().get(0).instanceId();

    return instanceId; // Return the instance ID
  }

  public List<AmiDetails> getAmis() {

    // Using amazon is taking infinite time to load
    DescribeImagesRequest request = DescribeImagesRequest.builder()
        .owners("self") // Retrieve only the user's AMIs "amazon", "self",
        .build();

    DescribeImagesResponse response = ec2Client.describeImages(request);

    List<AmiDetails> amiList = response.images().stream()
        .map(image -> new AmiDetails(image.imageId(), image.name(), image.description(), image.stateAsString()))
        .collect(Collectors.toList());

    return amiList;
  }

  public AmiResponse createAmis(AmiRequestDto amiRequestDto) {
    try {
      CreateImageRequest request = CreateImageRequest.builder()
          .instanceId(amiRequestDto.getInstanceId())
          .name(amiRequestDto.getName())
          .description(amiRequestDto.getDescription())
          .noReboot(amiRequestDto.isNoReboot())
          .build();

      CreateImageResponse response = ec2Client.createImage(request);

      return new AmiResponse(response.imageId(), "AMI created successfully");
    } catch (Ec2Exception e) {
      throw new GenericException(400, "Failed to create AMI");
    }
  }
}
