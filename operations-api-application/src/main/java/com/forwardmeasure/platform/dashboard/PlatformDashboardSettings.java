/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forwardmeasure.platform.dashboard;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Resolved, already-typed dashboard configuration. Each framework binding resolves its own raw
 * config (Quarkus {@code @ConfigMapping}, Spring/Micronaut {@code @ConfigurationProperties}) and
 * maps it into this plain, framework-neutral value object before handing it to application-layer
 * classes - mirrors how openworkflow-execution-management's binding classes resolve
 * {@code @ConfigProperty}/{@code @Value} at the producer method and pass already-typed values into
 * a plain constructor, rather than passing a config-lookup abstraction across the module boundary.
 */
public record PlatformDashboardSettings(
    String oidcUrl,
    String oidcRealm,
    String oidcClientId,
    String environment,
    Duration probeTimeout,
    Duration observationTtl,
    List<Tenant> tenants,
    List<Application> applications,
    List<Component> components,
    String platformDisplayName) {

  public PlatformDashboardSettings {
    if (platformDisplayName == null || platformDisplayName.isBlank()) {
      throw new IllegalArgumentException("platformDisplayName must not be blank");
    }
    tenants = List.copyOf(tenants);
    applications = List.copyOf(applications);
    components = List.copyOf(components);
  }

  public record Tenant(String host, String did, String name, String displayName) {}

  public record Component(
      String id,
      String name,
      String category,
      String probe,
      URI healthUri,
      Optional<URI> consoleUri) {}

  public record Application(String id, String name, String description, String href) {}
}
