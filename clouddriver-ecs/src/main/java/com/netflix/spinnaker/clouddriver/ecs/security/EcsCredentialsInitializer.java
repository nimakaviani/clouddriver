/*
 * Copyright 2017 Lookout, Inc.
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

package com.netflix.spinnaker.clouddriver.ecs.security;

import com.netflix.spinnaker.clouddriver.aws.security.NetflixAmazonCredentials;
import com.netflix.spinnaker.clouddriver.aws.security.config.CredentialsLoader;
import com.netflix.spinnaker.clouddriver.security.AccountCredentials;
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsRepository;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class EcsCredentialsInitializer {
  @Bean
  @DependsOn("netflixAmazonCredentials")
  public List<NetflixAmazonCredentials> netflixECSCredentials(
      CredentialsLoader<? extends NetflixAmazonCredentials> credentialsLoader,
      AccountCredentialsRepository accountCredentialsRepository)
      throws Throwable {
    return synchronizeECSAccounts(
        credentialsLoader, accountCredentialsRepository);
  }

  private List<NetflixAmazonCredentials> synchronizeECSAccounts(
      CredentialsLoader<? extends NetflixAmazonCredentials> credentialsLoader,
      AccountCredentialsRepository accountCredentialsRepository)
      throws Throwable {

    // TODO: add support for mutable accounts.
    List<NetflixAmazonCredentials> credentials = new LinkedList<>();

    for (AccountCredentials accountCredentials : accountCredentialsRepository.getAll()) {
      if (accountCredentials instanceof NetflixAmazonCredentials) {
          NetflixAmazonCredentials amazonCreds = (NetflixAmazonCredentials) accountCredentials;
          if (amazonCreds.getEcsEnabled()) {
              credentials.add(amazonCreds);
          }
      }
    }

    return credentials;
  }
}
