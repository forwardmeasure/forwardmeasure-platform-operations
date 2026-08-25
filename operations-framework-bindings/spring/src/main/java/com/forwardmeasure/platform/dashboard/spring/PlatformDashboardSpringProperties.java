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
