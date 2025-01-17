package com.skndan.rdp.service.integration.aws;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;

import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.service.integration.AwsService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AwsPostInstanceQueue {
  private static final Logger LOG = Logger.getLogger(AwsPostInstanceQueue.class);

  @Inject
  AwsService awsService;

  private final BlockingQueue<String> instanceQueue = new LinkedBlockingQueue<>();
  private volatile boolean isWorkerRunning = false;

  public void enqueueInstance(String instanceId) {
    try {
      instanceQueue.put(instanceId);
      startWorker();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      LOG.error("Failed to enqueue instance ID: " + e.getMessage());
    }
  }

  private synchronized void startWorker() {
    if (!isWorkerRunning) {
      isWorkerRunning = true;

      new Thread(() -> {
        try {
          while (!instanceQueue.isEmpty()) {
            String instanceId = instanceQueue.poll(5, TimeUnit.SECONDS); // Wait for 5 seconds if the queue is empty
            Thread.sleep(3000);
            if (instanceId != null) {
              processInstance(instanceId);
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          LOG.error("Worker thread interrupted: " + e.getMessage());
        } finally {
          isWorkerRunning = false; // Allow worker to restart if needed
        }
      }).start();
    }
  }

  @Transactional
  public void processInstance(String instanceId) {
    try {
      LOG.info("Checking instance details for ID: " + instanceId);

      for (int i = 0; i < 3; i++) { // Retry for up to 3 attempts
        Instance instance = awsService.getInstanceById(instanceId);
        LOG.info("For for ID: " + instanceId);
        if (instance != null && "RUNNING".equals(instance.getState())) {

          LOG.info("Instance is running");

          // Update the instance in a transactional context
          awsService.updateInstance(instance, instanceId);

          return; // Stop further checks once the instance is ready

        } else {
          LOG.info("Instance is still initializing: " + instanceId);
        }

        // Delay between retries
        Thread.sleep(5000); // 5 seconds
      }

      LOG.warn("Instance did not reach the expected state within the retry limit: " + instanceId);
    } catch (Exception e) {
      LOG.error("Error processing instance ID " + instanceId, e);
    }
  }
}