# RDP


### Creating instance

```java
RunInstancesRequest runRequest = RunInstancesRequest.builder()
    .imageId("ami-xxxxxxxxxxxxxxxxx")           // Replace with the appropriate AMI ID
    .instanceType(InstanceType.T3_MICRO)        // Instance type (e.g., T3_MICRO, M5_LARGE)
    .minCount(1)                                // Minimum number of instances to launch
    .maxCount(1)                                // Maximum number of instances to launch
    .keyName("your-keypair-name")               // Key pair name for SSH access
    .securityGroupIds("sg-xxxxxxxxxxxxxxxxx")   // Security group IDs
    .subnetId("subnet-xxxxxxxxxxxxxxxxx")       // Subnet ID for the instance
    .iamInstanceProfile(IamInstanceProfileSpecification.builder()
        .arn("arn:aws:iam::xxxxxxxxxxxx:instance-profile/your-profile")  // IAM instance profile ARN
        .build())
    .userData("base64-encoded-user-data")       // Base64-encoded user data script
    .tagSpecifications(TagSpecification.builder()
        .resourceType(ResourceType.INSTANCE)
        .tags(Tag.builder()
            .key("Name")
            .value("InstanceName")
            .build())
        .build())
    .build();
```

```java

  public String getPassword() {
    GetPasswordDataResponse passwordDataResponse = ec2Client.getPasswordData(
        GetPasswordDataRequest.builder()
            .instanceId("i-0b321116948c9eba6")
            .build());

    String encryptedPassword = passwordDataResponse.passwordData();
    System.out.println(encryptedPassword);

    AwsCredentialsConfig credentials = awsCredentialsService.fetchAwsCredentials();
    try {
      encryptedPassword = decryptPassword(encryptedPassword, credentials.getKeyPair());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return encryptedPassword;
  }
```

```shell script
keytool -importcert -alias guacamole-server-cert -file self.cert -keystore self-truststore.jks -storepass password
```