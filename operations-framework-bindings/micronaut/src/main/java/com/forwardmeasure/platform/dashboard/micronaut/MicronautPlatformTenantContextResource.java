package com.forwardmeasure.platform.dashboard.micronaut;

import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.PlatformTenantResolver;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformTenantContextResource;
import jakarta.inject.Singleton;

/**
 * Micronaut compile-time discovery edge - see {@link MicronautPlatformDashboardResource}. No
 * security annotation here - see {@link MicronautPlatformDashboardConfigResource}'s Javadoc for
 * why; authorization for this path is the {@code intercept-url-map} entry in
 * application.properties.
 */
@Singleton
public class MicronautPlatformTenantContextResource extends PlatformTenantContextResource {

  public MicronautPlatformTenantContextResource(
      PlatformTenantResolver tenants, PlatformDashboardSettings settings) {
    super(tenants, settings);
  }
}
