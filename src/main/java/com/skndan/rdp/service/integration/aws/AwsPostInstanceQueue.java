package com.skndan.rdp.service.integration.aws;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.logging.Logger;

import com.skndan.rdp.client.GuacamoleService;
import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.model.guacamole.Connection;
import com.skndan.rdp.repo.InstanceRepo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.GetPasswordDataRequest;
import software.amazon.awssdk.services.ec2.model.GetPasswordDataResponse;

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
  AwsCredentialsService awsCredentialsService;

  @Inject
  GuacamoleService guacamoleService;

  private static final int MAX_RETRY_ATTEMPTS = 5;

  @Transactional
  public void schedulePasswordRetrieval(Instance instance) {
    schedulePasswordRetrievalWithRetry(instance, 0);
  }

  private void schedulePasswordRetrievalWithRetry(Instance instance, int attemptCount) {
    executor.schedule(() -> {
      try {
        LOG.info("Getting Instance State");
        // Check instance state
        DescribeInstancesResponse describeResponse = ec2Client.describeInstances(
            DescribeInstancesRequest.builder()
                .instanceIds(instance.getInstanceId())
                .build());

        String currentState = describeResponse.reservations().get(0)
            .instances().get(0)
            .state().name().name();

        if ("running".equalsIgnoreCase(currentState)) {
          // Retrieve and process password
          retrieveAndProcessPassword(instance);
        } else {
          // Reschedule if not running
          handleNonRunningState(instance, attemptCount);
        }
      } catch (Exception e) {
        LOG.error("Error retrieving instance state", e);
        handleNonRunningState(instance, attemptCount);
      }
    }, 10, TimeUnit.SECONDS);
  }

  private void handleNonRunningState(Instance instance, int attemptCount) {
    if (attemptCount > MAX_RETRY_ATTEMPTS) {
      LOG.info("Instance not running. Rescheduling password retrieval. Attempt: " + (attemptCount + 1));
      schedulePasswordRetrievalWithRetry(instance, attemptCount + 1);
    } else {
      LOG.warn("Max retry attempts reached for instance: " + instance.getInstanceId());
      // Optional: Mark instance as failed or send notification
    }
  }

  private void retrieveAndProcessPassword(Instance instance) {
    try {
      GetPasswordDataResponse passwordDataResponse = ec2Client.getPasswordData(
          GetPasswordDataRequest.builder()
              .instanceId(instance.getInstanceId())
              .build());

      String password = passwordDataResponse.passwordData();
      boolean encrypted = true;

      try {
        AwsCredentialsConfig credentials = awsCredentialsService.fetchAwsCredentials();
        password = decryptPassword(password, credentials.getKeyPair());
        encrypted = false;
      } catch (Exception e) {
        LOG.error("Password decryption failed", e);
      }

      // Update instance with password
      instance.setPassword(password);
      instance.setEncrypted(encrypted);
      instanceRepo.save(instance);

      // Optional: Create Guacamole connection
      Connection connection = guacamoleService.createConnection(instance);
      instance.setGuacamoleIdentifier(connection.getIdentifier());
      instanceRepo.save(instance);

    } catch (Exception e) {
      LOG.error("Failed to retrieve password", e);
      handleNonRunningState(instance, 0);
    }
  }

  private String decryptPassword(String encryptedPassword, String privateKeyPem) throws Exception {
    Security.addProvider(new BouncyCastleProvider());

    // Remove PEM header and footer, and decode base64
    String privateKeyPEM = privateKeyPem
        .replace("-----BEGIN RSA PRIVATE KEY-----", "")
        .replace("-----END RSA PRIVATE KEY-----", "")
        .replace("\n", "")
        .replace(" ", "");

    byte[] encodedPrivateKey = Base64.getDecoder().decode(privateKeyPEM.trim());

    // Generate PrivateKey object
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

    // Decrypt using Cipher
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, privateKey);

    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));

    return new String(decryptedBytes);
  }

}
