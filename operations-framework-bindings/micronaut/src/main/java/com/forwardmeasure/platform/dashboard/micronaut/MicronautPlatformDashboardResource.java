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

import com.forwardmeasure.platform.dashboard.PlatformHealthService;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardResource;
import jakarta.inject.Singleton;

/**
 * Micronaut compile-time discovery edge for the shared portable dashboard resource. Path is
 * inherited unchanged from {@link PlatformDashboardResource} via {@code micronaut-jaxrs-server} -
 * must not be hand-declared here, matching {@code MicronautExecutionResource}'s pattern in
 * openworkflow-execution-management. Extends rather than composes: adds nothing but the bean scope
 * Micronaut needs to discover it as a singleton at all.
 *
 * <p>The inherited {@code @RolesAllowed} has no effect under Micronaut - see {@link
 * MicronautPlatformDashboardConfigResource}'s Javadoc. This resource's actual role enforcement is
 * the {@code intercept-url-map} entry in application.properties, kept in sync with the roles
 * {@code @RolesAllowed} declares by hand since Micronaut can't read it directly.
 */
@Singleton
public class MicronautPlatformDashboardResource extends PlatformDashboardResource {

  public MicronautPlatformDashboardResource(PlatformHealthService health) {
    super(health);
  }
}
