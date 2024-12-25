package com.skndan.rdp.service.integration.aws;

import java.util.List;
import java.util.stream.Collectors;

import com.skndan.rdp.model.LabelValue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.ec2.Ec2Client;

import software.amazon.awssdk.services.ec2.model.DescribeRegionsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeRegionsResponse;

@ApplicationScoped
public class AwsRegionService {

  @Inject
  Ec2Client ec2Client;

  public List<LabelValue> getAvailableRegions() {

    // Describe all available regions
    DescribeRegionsRequest describeRegionsRequest = DescribeRegionsRequest.builder().allRegions(true).build();
    DescribeRegionsResponse describeRegionsResponse = ec2Client.describeRegions(describeRegionsRequest);

    // Extract region names using the Region class from ec2.model
    List<String> dd = describeRegionsResponse.regions().stream()
        .map(region -> region.regionName()) // Use Region::regionName
        .collect(Collectors.toList());

    List<LabelValue> labelValueList = dd.stream()
        .map(str -> new LabelValue(str, str))
        .collect(Collectors.toList());

    return labelValueList;
  }
}
