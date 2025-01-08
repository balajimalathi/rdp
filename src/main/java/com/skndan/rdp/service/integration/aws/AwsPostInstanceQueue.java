package com.skndan.rdp.service.integration.aws;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

import com.skndan.rdp.client.GuacamoleService;
import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.repo.InstanceRepo;
import com.skndan.rdp.service.integration.AwsService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.ec2.Ec2Client;

@ApplicationScoped
public class AwsPostInstanceQueue {
  private static final Logger LOG = Logger.getLogger(AwsPostInstanceQueue.class);

  @Inject
  ScheduledExecutorService executor;

  @Inject
  Ec2Client ec2Client; // AWS EC2 Client

  @Inject
  InstanceRepo instanceRepo;

  @Inject
  AwsService awsService;

  @Inject
  AwsCredentialsService awsCredentialsService;

  @Inject
  GuacamoleService guacamoleService;

  private static final int MAX_RETRY_ATTEMPTS = 5;
  private static final int INITIAL_DELAY = 5;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public void waitForInstanceDetails(String instanceId) {
    // Schedule a task to run every 5 seconds
    scheduler.scheduleWithFixedDelay(() -> {
      try {
        LOG.info("Checking instance details...");
        Instance instance = awsService.getInstanceById(instanceId);

        if (instance != null && instance.getPublicDnsName() != null) {
          LOG.info("Instance is ready with public IP: " + instance.getPublicDnsName()); 
          awsService.updateInstance(instance, instanceId);
          scheduler.shutdown(); // Stop polling once the IP is available
        } else {
          LOG.info("Instance is still initializing..." + instanceId);
        }
      } catch (Exception e) {
        LOG.error("Error fetching instance details: " + e.getMessage());
        scheduler.shutdown(); // Stop polling on error
      }
    }, INITIAL_DELAY, MAX_RETRY_ATTEMPTS, TimeUnit.SECONDS); // Initial delay = 0, polling interval = 5 seconds
  }

}
