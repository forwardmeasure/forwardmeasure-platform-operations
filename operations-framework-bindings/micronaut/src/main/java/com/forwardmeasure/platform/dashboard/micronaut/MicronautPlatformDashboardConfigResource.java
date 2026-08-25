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
