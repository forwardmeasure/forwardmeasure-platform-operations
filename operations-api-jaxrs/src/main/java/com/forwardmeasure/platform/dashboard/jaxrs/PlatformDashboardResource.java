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
package com.forwardmeasure.platform.dashboard.jaxrs;

import com.forwardmeasure.platform.dashboard.PlatformHealthService;
import com.forwardmeasure.platform.operations.api.PlatformApi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

/**
 * Authenticated, read-only shared-platform operational summary. Portable implementation shared
 * unchanged by Quarkus, Spring, and Micronaut - matches
 * openworkflow-execution-jaxrs.ExecutionResource's own Javadoc convention. Quarkus and Spring
 * register this class directly via a producer method; Micronaut needs a thin, override-free
 * subclass purely for its bean-discovery edge.
 *
 * <p>{@code @Path} is the fixed full path, not "/api/v1" alone - {@link PlatformApi}'s own
 * interface-level {@code @Path("/components")} is NOT concatenated with this class's own type-level
 * {@code @Path} by JAX-RS (confirmed the hard way: the class-level path wins exclusively), so the
 * class must declare the complete path itself. The other two resources in this package ({@code
 * /tenant-context}, {@code /config.js}) are deliberately unprefixed - do not "fix" this via a
 * blanket {@code @ApplicationPath} that would also catch them.
 */
@Path("/api/v1/components")
@RolesAllowed({"platform-viewer", "platform-admin"})
public class PlatformDashboardResource implements PlatformApi {

  private final PlatformHealthService health;

  public PlatformDashboardResource(PlatformHealthService health) {
    this.health = Objects.requireNonNull(health, "health");
  }

  @Override
  public Response listPlatformComponents() {
    return Response.ok(health.observe()).build();
  }
}
