/*
 * Copyright 2018 Lookout, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.ecs.services;

import com.netflix.spinnaker.clouddriver.aws.cache.Keys;
import com.netflix.spinnaker.clouddriver.aws.model.AmazonSubnet;
import com.netflix.spinnaker.clouddriver.aws.provider.view.AmazonSubnetProvider;
import com.netflix.spinnaker.clouddriver.ecs.model.EcsSubnet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubnetSelector {

  AmazonSubnetProvider amazonSubnetProvider;

  @Autowired
  public SubnetSelector(
      AmazonSubnetProvider amazonSubnetProvider){
    this.amazonSubnetProvider = amazonSubnetProvider;
  }

  public Collection<String> resolveSubnetsIds(
      String accountName,
      String region,
      Collection<String> availabilityZones,
      String subnetType) {

    Set<EcsSubnet> ecsSubnets = amazonSubnetProvider.getAllMatchingKeyPattern(
      Keys.getSubnetKey("*", region, accountName)).stream().map(it -> new EcsSubnet(it)).collect(Collectors.toSet());

    Set<String> filteredSubnetIds =
        ecsSubnets.stream()
            .filter(subnet -> subnetType.equals(subnet.getPurpose()))
            .filter(subnet -> availabilityZones.contains(subnet.getAvailabilityZone()))
            .map(AmazonSubnet::getId)
            .collect(Collectors.toSet());

    return filteredSubnetIds;
  }

  public Collection<String> getSubnetVpcIds(
      String accountName, String region, Collection<String> subnetIds) {

    Set<String> subnetKeys =
        subnetIds.stream()
            .map(subnetId -> Keys.getSubnetKey(subnetId, region, accountName))
            .collect(Collectors.toSet());

    Set<EcsSubnet> ecsSubnets = amazonSubnetProvider.loadResults(subnetKeys).stream().
      map(it -> new EcsSubnet(it)).collect(Collectors.toSet());

    return ecsSubnets.stream().map(AmazonSubnet::getVpcId).collect(Collectors.toSet());
  }
}
