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
    List<Component> components) {

  public PlatformDashboardSettings {
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
