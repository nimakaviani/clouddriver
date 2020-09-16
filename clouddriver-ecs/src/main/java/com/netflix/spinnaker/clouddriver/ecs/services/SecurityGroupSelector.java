/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.netflix.spinnaker.clouddriver.aws.provider.view.AmazonSecurityGroupProvider;
import com.netflix.spinnaker.clouddriver.ecs.model.EcsSecurityGroup;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityGroupSelector {

  AmazonSecurityGroupProvider amazonSecurityGroupProvider;

  @Autowired
  public SecurityGroupSelector(
      AmazonSecurityGroupProvider amazonSecurityGroupProvider){
    this.amazonSecurityGroupProvider = amazonSecurityGroupProvider;
  }

  public Collection<String> resolveSecurityGroupNames(
      String accountName,
      String region,
      Collection<String> securityGroupNames,
      Collection<String> vpcIds) {

    Collection<EcsSecurityGroup> ecsSecurityGroups =
      amazonSecurityGroupProvider.getAllByAccountAndRegion(
        true, accountName, region).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());

    Set<String> securityGroupNamesSet = new HashSet<String>(securityGroupNames);
    Set<String> vpcIdsSet = new HashSet<String>(vpcIds);

    Set<String> filteredSecurityGroupIds =
        ecsSecurityGroups.stream()
            .filter(group -> securityGroupNamesSet.contains(group.getName()))
            .filter(group -> vpcIdsSet.contains(group.getVpcId()))
            .map(EcsSecurityGroup::getId)
            .collect(Collectors.toSet());

    return filteredSecurityGroupIds;
  }
}
