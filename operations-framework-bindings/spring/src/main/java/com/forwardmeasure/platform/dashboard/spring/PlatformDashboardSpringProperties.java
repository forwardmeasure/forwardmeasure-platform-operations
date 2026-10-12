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
package com.forwardmeasure.platform.dashboard.spring;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Spring-native mirror of Quarkus's {@code PlatformDashboardConfiguration} {@code @ConfigMapping}.
 */
@ConfigurationProperties(prefix = "forwardmeasure.platform.dashboard")
public record PlatformDashboardSpringProperties(
    String oidcUrl,
    String oidcRealm,
    String oidcClientId,
    @DefaultValue("Production") String environment,
    @DefaultValue("PT2S") Duration probeTimeout,
    @DefaultValue("PT10S") Duration observationTtl,
    List<Tenant> tenants,
    List<Application> applications,
    List<Component> components) {

  public record Tenant(String host, String did, String name, String displayName) {}

  public record Component(
      String id,
      String name,
      String category,
      @DefaultValue("HTTP") String probe,
      URI healthUri,
      Optional<URI> consoleUri) {}

  public record Application(String id, String name, String description, String href) {}
}
