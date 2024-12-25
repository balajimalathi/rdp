package com.skndan.rdp.service.integration.aws;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skndan.rdp.entity.Integration;
import com.skndan.rdp.entity.constants.CloudProvider;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.repo.IntegrationRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AwsCredentialsService {

  @Inject
  IntegrationRepo integrationRepo;

  ObjectMapper objectMapper = new ObjectMapper();

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

    return credentials;
  }
}
