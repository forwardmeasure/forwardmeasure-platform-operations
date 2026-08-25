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
