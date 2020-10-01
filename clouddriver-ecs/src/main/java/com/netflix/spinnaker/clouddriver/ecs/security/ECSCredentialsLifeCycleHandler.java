/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.ecs.security;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spectator.api.Registry;
import com.netflix.spinnaker.cats.agent.Agent;
import com.netflix.spinnaker.cats.module.CatsModule;
import com.netflix.spinnaker.clouddriver.aws.security.AmazonClientProvider;
import com.netflix.spinnaker.clouddriver.aws.security.AmazonCredentials;
import com.netflix.spinnaker.clouddriver.ecs.provider.EcsProvider;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.ContainerInstanceCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.EcsCloudMetricAlarmCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.EcsClusterCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.IamPolicyReader;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.IamRoleCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.ScalableTargetsCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.SecretCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.ServiceCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.ServiceDiscoveryCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.TargetHealthCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.TaskCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.TaskDefinitionCachingAgent;
import com.netflix.spinnaker.clouddriver.ecs.provider.agent.TaskHealthCachingAgent;
import com.netflix.spinnaker.clouddriver.security.ProviderUtils;
import com.netflix.spinnaker.credentials.CredentialsLifecycleHandler;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ECSCredentialsLifeCycleHandler
    implements CredentialsLifecycleHandler<NetflixECSCredentials> {
  protected final EcsProvider ecsProvider;
  protected final AmazonClientProvider amazonClientProvider;
  protected final AWSCredentialsProvider awsCredentialsProvider;
  protected final Registry registry;
  protected final IamPolicyReader iamPolicyReader;
  protected final ObjectMapper objectMapper;
  protected final CatsModule catsModule;

  @Override
  public void credentialsAdded(@NotNull NetflixECSCredentials credentials) {
    scheduleAgents(credentials);
  }

  @Override
  public void credentialsUpdated(@NotNull NetflixECSCredentials credentials) {
    ProviderUtils.unscheduleAndDeregisterAgents(
        Collections.singleton(credentials.getName()), catsModule);
    scheduleAgents(credentials);
  }

  @Override
  public void credentialsDeleted(NetflixECSCredentials credentials) {
    ProviderUtils.unscheduleAndDeregisterAgents(
        Collections.singleton(credentials.getName()), catsModule);
  }

  private void scheduleAgents(NetflixECSCredentials credentials) {
    Set<String> scheduledAccounts = ProviderUtils.getScheduledAccounts(ecsProvider);
    List<Agent> newAgents = new LinkedList<>();
    newAgents.add(
        new IamRoleCachingAgent(
            credentials, amazonClientProvider, awsCredentialsProvider, iamPolicyReader));
    if (!scheduledAccounts.contains(credentials.getName())) {
      for (AmazonCredentials.AWSRegion region : credentials.getRegions()) {
        newAgents.add(
            new EcsClusterCachingAgent(
                credentials, region.getName(), amazonClientProvider, awsCredentialsProvider));
        newAgents.add(
            new ServiceCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                registry));
        newAgents.add(
            new TaskCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                registry));
        newAgents.add(
            new ContainerInstanceCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                registry));
        newAgents.add(
            new TaskDefinitionCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                registry,
                objectMapper));
        newAgents.add(
            new TaskHealthCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                objectMapper));
        newAgents.add(
            new EcsCloudMetricAlarmCachingAgent(
                credentials, region.getName(), amazonClientProvider, awsCredentialsProvider));
        newAgents.add(
            new ScalableTargetsCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                objectMapper));
        newAgents.add(
            new SecretCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                objectMapper));
        newAgents.add(
            new ServiceDiscoveryCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                objectMapper));
        newAgents.add(
            new TargetHealthCachingAgent(
                credentials,
                region.getName(),
                amazonClientProvider,
                awsCredentialsProvider,
                objectMapper));
      }
    }

    ProviderUtils.rescheduleAgents(ecsProvider, newAgents);
    ecsProvider.getAgents().addAll(newAgents);
    ecsProvider.synchronizeHealthAgents();
  }
}
