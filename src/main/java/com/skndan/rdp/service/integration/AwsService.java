package com.skndan.rdp.service.integration;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateImageRequest;
import software.amazon.awssdk.services.ec2.model.CreateImageResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeKeyPairsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GetPasswordDataRequest;
import software.amazon.awssdk.services.ec2.model.GetPasswordDataResponse;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.KeyPairInfo;
import software.amazon.awssdk.services.ec2.model.ResourceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skndan.rdp.client.GuacamoleService;
import com.skndan.rdp.config.EntityCopyUtils;
import com.skndan.rdp.entity.Instance;
import com.skndan.rdp.entity.constants.CloudProvider;
import com.skndan.rdp.entity.constants.InstanceState;
import com.skndan.rdp.entity.constants.Platform;
import com.skndan.rdp.exception.GenericException;
import com.skndan.rdp.model.aws.AmiDetails;
import com.skndan.rdp.model.aws.AmiRequestDto;
import com.skndan.rdp.model.aws.AmiResponse;
import com.skndan.rdp.model.aws.InstanceRequestDto;
import com.skndan.rdp.model.aws.InstanceStateResponse;
import com.skndan.rdp.model.aws.KeyPairDetails;
import com.skndan.rdp.model.guacamole.Connection;
import com.skndan.rdp.repo.InstanceRepo;
import com.skndan.rdp.service.integration.aws.AwsCredentialsConfig;
import com.skndan.rdp.service.integration.aws.AwsCredentialsService;
import com.skndan.rdp.service.integration.aws.InstanceStateService;
import com.skndan.rdp.service.integration.aws.AwsPostInstanceQueue;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service class for managing AWS EC2 instances and AMIs.
 * Provides functionality to interact with the AWS EC2 API for instance
 * retrieval,
 * synchronization, creation, and AMI (Amazon Machine Image) operations.
 */
@Startup
@ApplicationScoped
public class AwsService {

  @Inject
  Ec2Client ec2Client;

  @Inject
  InstanceRepo instanceRepo; // JPA repository or equivalent

  @Inject
  EntityCopyUtils entityCopyUtils;

  @Inject
  InstanceStateService instanceStateService;

  @Inject
  GuacamoleService guacamoleService;

  @Inject
  AwsPostInstanceQueue awsPostInstanceQueue;

  private static final Logger LOG = LoggerFactory.getLogger(AwsService.class);

  /**
   * Retrieves EC2 instances from AWS, synchronizes them with the local database,
   * and returns a list of the latest AWS EC2 instances.
   * <p>
   * This method performs the following actions:
   * <ul>
   * <li>Fetches EC2 instances from AWS.</li>
   * <li>Compares the fetched instances with the local database records.</li>
   * <li>Adds new instances to the database if they do not exist.</li>
   * <li>Updates existing database records with the latest AWS data.</li>
   * <li>Deletes database records for instances that no longer exist in AWS.</li>
   * </ul>
   *
   * @return List of {@link Instance} objects representing the latest AWS EC2
   *         instances.
   */
  @Transactional
  public List<Instance> getInstance() {
    LOG.info("Get Instance");
    DescribeInstancesResponse response = ec2Client.describeInstances();

    LOG.info("Fetching SDK Instances");
    List<Instance> awsInstances = response.reservations().stream()
        .flatMap(reservation -> reservation.instances().stream())
        .map(instance -> {

          List<Tag> tags = instance.tags();
          String instanceName = null;

          if (tags != null) {
            Optional<Tag> nameTag = tags.stream()
                .filter(tag -> "Name".equals(tag.key()))
                .findFirst();

            if (nameTag.isPresent()) {
              instanceName = nameTag.get().value();
            }
          }

          Instance res = new Instance();
          res.setName(instanceName);
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
          res.setProvider(CloudProvider.AMAZON_WEB_SERVICE);
          return res;
        })
        .collect(Collectors.toList());
    LOG.info("Total SDK Instances: " + awsInstances.size());

    LOG.info("Fetching DB Instances");

    Iterable<Instance> dbInstances = instanceRepo.findAll();
    LOG.info("Total DB Instances: " + StreamSupport.stream(dbInstances.spliterator(), false).count());

    LOG.info("Creating Instance ID Map");
    Map<Object, Instance> dbInstanceMap = StreamSupport.stream(dbInstances.spliterator(), false)
        .collect(Collectors.toMap(instance -> instance.getInstanceId(), Function.identity()));

    // Map AWS instances by ID for quick lookup
    LOG.info("Map AWS instances by ID for quick lookup");
    Set<String> awsInstanceIds = awsInstances.stream()
        .map(instance -> instance.getInstanceId())
        .collect(Collectors.toSet());

    LOG.info("Looping Aws instance and update the DB");
    for (Instance awsInstance : awsInstances) {
      LOG.info("InstanceID : " + awsInstance.getInstanceId());
      Instance dbInstance = dbInstanceMap.get(awsInstance.getInstanceId());
      if (dbInstance == null) {
        // Add new instance
        LOG.info("New Instance : " + awsInstance.getInstanceId());
        instanceRepo.save(awsInstance);
      } else {
        // Update existing instance
        LOG.info("Existing Instance : " + awsInstance.getInstanceId());
        entityCopyUtils.copyProperties(dbInstance, awsInstance);

        if(((dbInstance.getPassword() == null) || dbInstance.getPassword().equals("")) && !(dbInstance.getState().toLowerCase().equals("terminated"))) {
          LOG.info("Scheduled to get password : " + awsInstance.getInstanceId());
          awsPostInstanceQueue.schedulePasswordRetrieval(dbInstance);
        }

        instanceRepo.save(dbInstance);
        LOG.info("Updating DB Instance : " + awsInstance.getInstanceId());
      }
    }

    // Remove instances that no longer exist in AWS
    LOG.info("Remove instances that no longer exist in AWS");
    for (Instance dbInstance : dbInstances) {
      if (!awsInstanceIds.contains(dbInstance.getInstanceId())) {
        LOG.info("Removed InstanceID : " + dbInstance.getInstanceId());
        instanceRepo.delete(dbInstance);
      }
    }

    return awsInstances;
  }

  /**
   * Creates a new EC2 instance on AWS with the specified configuration.
   *
   * @return The instance ID of the newly created EC2 instance.
   */
  @Transactional
  public Instance createEc2Instance(InstanceRequestDto request) {

    // TODO: To be checked - start
    DescribeImagesResponse amiDetails = ec2Client.describeImages(DescribeImagesRequest.builder()
        .imageIds(request.getAmiId()).build());

    String bootMode = amiDetails.images().get(0).bootMode().name();

    InstanceType instanceType = bootMode.equals("uefi") ? InstanceType.T3_MICRO : InstanceType.T2_MICRO;
    // To be checked - end

    // Create EC2 instance
    RunInstancesRequest runRequest = RunInstancesRequest.builder()
        .imageId(request.getAmiId()) // Replace with appropriate AMI ID
        .instanceType(InstanceType.T3_MICRO)
        .minCount(1)
        .maxCount(1)
        .keyName(request.getKeyName())
        .tagSpecifications(TagSpecification.builder()
            .resourceType(ResourceType.INSTANCE)
            .tags(Tag.builder()
                .key("Name")
                .value(request.getName())
                .build())
            .build())
        .build();

    RunInstancesResponse runResponse = ec2Client.runInstances(runRequest);
    var dd = runResponse.instances().get(0);

    Instance res = new Instance();
    res.setName(request.getName());
    res.setInstanceId(dd.instanceId());
    res.setState(dd.state().name().name());
    res.setPublicDnsName(dd.publicDnsName());
    res.setPublicIpAddress(dd.publicIpAddress());
    res.setPrivateIpAddress(dd.privateIpAddress());
    res.setInstanceType(dd.instanceTypeAsString());
    res.setImageId(dd.imageId());
    res.setKeyName(dd.keyName());
    res.setLaunchTime(dd.launchTime().toString());
    res.setAvailabilityZone(dd.placement().availabilityZone());
    res.setProvider(CloudProvider.AMAZON_WEB_SERVICE);
    res.setPlatform(Platform.WINDOWS);
    res.setUsername("Administrator");

    awsPostInstanceQueue.schedulePasswordRetrieval(res);
    // GetPasswordDataResponse passwordDataResponse = ec2Client.getPasswordData(
    // GetPasswordDataRequest.builder()
    // .instanceId(dd.instanceId())
    // .build());

    // String password = passwordDataResponse.passwordData();
    // boolean encrypted = true;

    // try {
    // AwsCredentialsConfig credentials =
    // awsCredentialsService.fetchAwsCredentials();
    // password = decryptPassword(password, credentials.getKeyPair());
    // encrypted = false;
    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    // res.setPassword(password);
    // res.setEncrypted(encrypted);

    // create guacamole connection
    // Connection connection = guacamoleService.createConnection(res);

    // res.setGuacamoleIdentifier(connection.getIdentifier());

    // base64 convert {identifier}/c/{dataSource}
    // res.setGuacamoleConnectionString();

    instanceRepo.save(res);

    return res; // Return the instance ID
  }

  /**
   * Retrieves the list of AMIs (Amazon Machine Images) owned by the user.
   * <p>
   * This method fetches AMI details such as image ID, name, description, and
   * state.
   *
   * @return List of {@link AmiDetails} objects containing the details of the
   *         user's AMIs.
   */
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

  /**
   * Creates a new AMI (Amazon Machine Image) based on the specified instance ID
   * and other details.
   *
   * @param amiRequestDto The {@link AmiRequestDto} object containing details of
   *                      the AMI to be created:
   *                      instance ID, name, description, and a flag indicating
   *                      whether to reboot the instance.
   * @return An {@link AmiResponse} object containing the AMI ID and a success
   *         message.
   * @throws GenericException If the AMI creation fails due to an AWS error.
   */
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

  @Transactional
  public InstanceStateResponse changeState(String instanceId, InstanceState state) {
    switch (state) {
      case START:
        return instanceStateService.startInstance(instanceId);
      case STOP:
        return instanceStateService.stopInstance(instanceId);
      case REBOOT:
        return instanceStateService.rebootInstance(instanceId);
      case HIBERNATE:
        return instanceStateService.hibernateInstance(instanceId);
      default:
        return instanceStateService.terminateInstance(instanceId);
    }
  }

  public List<KeyPairDetails> getKeyPairs() {
    DescribeKeyPairsRequest request = DescribeKeyPairsRequest.builder().build();
    DescribeKeyPairsResponse response = ec2Client.describeKeyPairs(request);

    List<KeyPairDetails> keyPairList = response.keyPairs().stream()
        .map(this::mapKeyPairInfoToDetails)
        .collect(Collectors.toList());

    return keyPairList;
  }

  private KeyPairDetails mapKeyPairInfoToDetails(KeyPairInfo keyPairInfo) {
    return new KeyPairDetails(
        keyPairInfo.keyName(),
        keyPairInfo.keyFingerprint());
  }
}
