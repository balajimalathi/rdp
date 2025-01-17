package com.skndan.rdp.service.integration.aws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skndan.rdp.entity.Integration;
import com.skndan.rdp.entity.constants.CloudProvider;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.repo.IntegrationRepo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.SendCommandRequest;
import software.amazon.awssdk.services.ssm.model.SendCommandResponse;

@ApplicationScoped
public class AwsCredentialsService {

  private static final Logger LOG = LoggerFactory.getLogger(AwsCredentialsService.class);

  @Inject
  IntegrationRepo integrationRepo;

  ObjectMapper objectMapper = new ObjectMapper();

  @Transactional
  public AwsCredentialsConfig fetchAwsCredentials() {

    Integration integration = integrationRepo.findOneByCloudProvider(CloudProvider.AMAZON_WEB_SERVICE)
        .orElseThrow(() -> new GenericException(400, "AWS integration is not completed"));

    HashMap<String, Object> hashMap = new HashMap<>();

    try {
      hashMap = objectMapper.readValue(integration.getConfiguration(), new TypeReference<HashMap<String, Object>>() {
      });
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    AwsCredentialsConfig credentials = new AwsCredentialsConfig();

    credentials.setAccessKeyId(hashMap.get("accessKeyId").toString());
    credentials.setSecretAccessKey(hashMap.get("secretAccessKey").toString());
    credentials.setRegion(hashMap.get("region").toString());
    credentials.setKeyPair(hashMap.get("keyPair").toString());
    credentials.setKeyPairName(hashMap.get("keyPairName").toString());

    return credentials;
  }

  public void executeCommand(String instanceId, String region, String username, String password, boolean isWindows) {

    AwsCredentialsConfig credentials = fetchAwsCredentials();

    // Create the SsmClient with the specified region
    try (SsmClient ssm = SsmClient.builder()
        .region(Region.EU_NORTH_1)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                credentials.getAccessKeyId(),
                credentials.getSecretAccessKey())))
        .build()) {
      String commands;
      String documentName;

      if (isWindows) {
        commands = String.format(
            "net user %s %s /add; net localgroup Administrators %s /add",
            username, password, username);
        documentName = "AWS-RunPowerShellScript";
      } else {
        commands = String.format(
            "sudo adduser %s && echo '%s:%s' | sudo chpasswd && sudo usermod -aG sudo %s",
            username, username, password, username);
        documentName = "AWS-RunShellScript";
      }

      SendCommandRequest commandRequest = SendCommandRequest.builder()
          .instanceIds(instanceId)
          .documentName(documentName)
          .parameters(Map.of("commands", List.of(commands)))
          .build();

      SendCommandResponse commandResponse = ssm.sendCommand(commandRequest);
      System.out.println("Command sent: " + commandResponse.command().commandId());
    } catch (Exception ex) {
      LOG.error("Execute command failed", ex);
    }
  }
}
