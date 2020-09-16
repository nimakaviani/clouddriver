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

package com.netflix.spinnaker.clouddriver.ecs.provider.view;

import com.netflix.spinnaker.clouddriver.aws.provider.view.AmazonSecurityGroupProvider;
import com.netflix.spinnaker.clouddriver.ecs.EcsCloudProvider;
import com.netflix.spinnaker.clouddriver.ecs.model.EcsSecurityGroup;
import com.netflix.spinnaker.clouddriver.model.SecurityGroupProvider;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class EcsSecurityGroupProvider implements SecurityGroupProvider<EcsSecurityGroup> {

  final String cloudProvider = EcsCloudProvider.ID;

  final AmazonSecurityGroupProvider amazonSecurityGroupProvider;

  @Autowired
  EcsSecurityGroupProvider(
      AmazonSecurityGroupProvider amazonSecurityGroupProvider){
    this.amazonSecurityGroupProvider = amazonSecurityGroupProvider;
  }

  @Override
  public Collection<EcsSecurityGroup> getAll(boolean includeRules) {
    return amazonSecurityGroupProvider.getAll(includeRules).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());
  }

  @Override
  public Collection<EcsSecurityGroup> getAllByRegion(boolean includeRules, String region) {
    return amazonSecurityGroupProvider.getAllByRegion(includeRules, region).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());
  }

  @Override
  public Collection<EcsSecurityGroup> getAllByAccount(boolean includeRules, String account) {
    return amazonSecurityGroupProvider.getAllByAccount(includeRules, account).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());
  }

  @Override
  public Collection<EcsSecurityGroup> getAllByAccountAndName(
      boolean includeRules, String account, String name) {
    return amazonSecurityGroupProvider.getAllByAccountAndName(includeRules, account, name).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());
  }

  @Override
  public Collection<EcsSecurityGroup> getAllByAccountAndRegion(
      boolean includeRules, String account, String region) {
    return amazonSecurityGroupProvider.getAllByAccountAndRegion(includeRules, account, region).stream().map(it -> new EcsSecurityGroup(it)).collect(Collectors.toSet());
  }

  @Override
  public EcsSecurityGroup get(String account, String region, String name, String vpcId) {
    return new EcsSecurityGroup(amazonSecurityGroupProvider.get(account, region, name, vpcId));
  }

  @Override
  public EcsSecurityGroup getById(String account, String region, String id, String vpcId) {
    return new EcsSecurityGroup(amazonSecurityGroupProvider.getById(account, region, id, vpcId));
  }

  @Override
  public String getCloudProvider() {
    return cloudProvider;
  }
}
