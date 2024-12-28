package com.skndan.rdp.service.integration.aws;

import com.skndan.rdp.model.aws.InstanceStateResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.RebootInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StartInstancesResponse;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesResponse;

@ApplicationScoped
public class InstanceStateService {

  @Inject
  Ec2Client ec2Client;

  public InstanceStateResponse startInstance(String instanceId) {
    try {
      StartInstancesRequest startRequest = StartInstancesRequest.builder()
          .instanceIds(instanceId)
          .build();

      StartInstancesResponse startResponse = ec2Client.startInstances(startRequest);
      System.out.println("Starting Instance: " + startResponse.startingInstances());
      return new InstanceStateResponse(
          instanceId,
          "starting",
          "The instance is being started", 200);
    } catch (Ec2Exception e) {
      return new InstanceStateResponse(
          instanceId,
          "starting",
          e.getMessage(), 400);
    }
  }

  public InstanceStateResponse stopInstance(String instanceId) {
    try {
      StopInstancesRequest stopRequest = StopInstancesRequest.builder()
          .instanceIds(instanceId)
          .build();

      StopInstancesResponse stopResponse = ec2Client.stopInstances(stopRequest);
      System.out.println("Stopping Instance: " + stopResponse.stoppingInstances());
      return new InstanceStateResponse(
          instanceId,
          "stopping",
          "The instance is being stopped", 200);
    } catch (Ec2Exception e) {
      return new InstanceStateResponse(
          instanceId,
          "stopping",
          e.getMessage(), 400);
    }
  }

  public InstanceStateResponse rebootInstance(String instanceId) {
    try {
      RebootInstancesRequest rebootRequest = RebootInstancesRequest.builder()
          .instanceIds(instanceId)
          .build();

      ec2Client.rebootInstances(rebootRequest);
      System.out.println("Rebooting Instance: " + instanceId);
      return new InstanceStateResponse(
          instanceId,
          "rebooting",
          "The instance is being rebooted", 200);
    } catch (Ec2Exception e) {
      return new InstanceStateResponse(
          instanceId,
          "rebooting",
          e.getMessage(), 400);
    }
  }

  public InstanceStateResponse hibernateInstance(String instanceId) {
    try {
      StopInstancesRequest hibernateRequest = StopInstancesRequest.builder()
          .instanceIds(instanceId)
          .hibernate(true)
          .build();

      StopInstancesResponse hibernateResponse = ec2Client.stopInstances(hibernateRequest);
      System.out.println("Hibernating Instance: " + hibernateResponse.stoppingInstances());
      return new InstanceStateResponse(
          instanceId,
          "stopping",
          "The instance is being stopped", 200);
    } catch (Ec2Exception e) {
      return new InstanceStateResponse(
          instanceId,
          "stopping",
          e.getMessage(), 400);
    }
  }

  public InstanceStateResponse terminateInstance(String instanceId) {
    try {
      TerminateInstancesRequest terminateRequest = TerminateInstancesRequest.builder()
          .instanceIds(instanceId)
          .build();

      TerminateInstancesResponse terminateResponse = ec2Client.terminateInstances(terminateRequest);
      System.out.println("Terminating Instance: " + terminateResponse.terminatingInstances());

      return new InstanceStateResponse(
          instanceId,
          "terminate",
          "The instance is being terminated", 200);
    } catch (Ec2Exception e) {
      return new InstanceStateResponse(
          instanceId,
          "terminate",
          e.getMessage(), 400);
    }
  }

}
