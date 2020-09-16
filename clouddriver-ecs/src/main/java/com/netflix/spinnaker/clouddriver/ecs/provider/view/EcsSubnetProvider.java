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

import com.netflix.spinnaker.clouddriver.aws.provider.view.AmazonSubnetProvider;
import com.netflix.spinnaker.clouddriver.ecs.EcsCloudProvider;
import com.netflix.spinnaker.clouddriver.ecs.model.EcsSubnet;
import com.netflix.spinnaker.clouddriver.model.SubnetProvider;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EcsSubnetProvider implements SubnetProvider<EcsSubnet> {

  final AmazonSubnetProvider amazonSubnetProvider;

  @Autowired
  public EcsSubnetProvider(
      AmazonSubnetProvider amazonSubnetProvider) {
    this.amazonSubnetProvider = amazonSubnetProvider;
  }

  @Override
  public String getCloudProvider() {
    return EcsCloudProvider.ID;
  }

  @Override
  public Set<EcsSubnet> getAll() {
    return amazonSubnetProvider.getAll().stream().map(it -> new EcsSubnet(it)).collect(Collectors.toSet());
  }
}
