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

package com.netflix.spinnaker.clouddriver.aws.security;

import com.netflix.spinnaker.clouddriver.security.CredentialsProvider;
import com.netflix.spinnaker.credentials.CredentialsRepository;
import java.util.Set;

public class AmazonCredentialProvider<T extends NetflixAmazonCredentials>
    implements CredentialsProvider<T> {
  private final CredentialsRepository<T> repository;

  public AmazonCredentialProvider(CredentialsRepository<T> repository) {
    this.repository = repository;
  }

  @Override
  public Set<T> getAll() {
    return repository.getAll();
  }

  @Override
  public T getCredentials(String name) {
    return repository.getOne(name);
  }
}
