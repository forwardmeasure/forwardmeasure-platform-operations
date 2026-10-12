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
package com.forwardmeasure.platform.dashboard.micronaut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardConfigResource;
import jakarta.inject.Singleton;

/**
 * Micronaut compile-time discovery edge - see {@link MicronautPlatformDashboardResource}.
 *
 * <p>No {@code @RolesAllowed}/{@code @PermitAll}/{@code @Secured} here, deliberately: none of them
 * have any effect on a resource dispatched through micronaut-jaxrs-server. Micronaut Security's
 * annotation-based rule evaluates the *matched route's* annotation metadata, and JAX-RS resources
 * here are dispatched through a single generic bridge route that carries none of the resource
 * class's own annotations - confirmed by testing all three annotations directly and finding no
 * difference in behavior. The actual, working authorization mechanism is path-based: {@code
 * micronaut.security.intercept-url-map} in application.properties, which permits this path
 * anonymously. See that file's comment for the pattern-matching gotcha (patterns are relative to
 * the route, without the {@code micronaut.server.context-path} prefix).
 */
@Singleton
public class MicronautPlatformDashboardConfigResource extends PlatformDashboardConfigResource {

  public MicronautPlatformDashboardConfigResource(
      PlatformDashboardSettings settings, ObjectMapper json) {
    super(settings, json);
  }
}
