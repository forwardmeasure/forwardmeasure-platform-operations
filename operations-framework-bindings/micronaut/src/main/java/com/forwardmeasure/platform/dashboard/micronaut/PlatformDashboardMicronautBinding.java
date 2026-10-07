package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forwardmeasure.platform.dashboard.PlatformDashboardConfigurationValidator;
import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.PlatformHealthService;
import com.forwardmeasure.platform.dashboard.PlatformTenantResolver;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import java.util.List;

/**
 * Micronaut composition root for the platform dashboard capability - host integration only, no
 * domain logic, matching PROJECT_MANIFESTO.md 3.10's binding-module contract and the identical
 * shape openworkflow-execution-management-micronaut-binding uses. The three shared -jaxrs resources
 * need no producer methods here: each is discovered directly via its own thin,
 * {@code @Singleton}-annotated Micronaut subclass (see {@link MicronautPlatformDashboardResource}
 * and its siblings), matching {@code MicronautExecutionResource}'s pattern rather than the
 * producer-method registration Quarkus/Spring use for the unmodified resource classes.
 */
@Factory
public class PlatformDashboardMicronautBinding {

  // This platform version's micronaut-jackson-databind only produces a Jackson 3
  // (tools.jackson.databind) mapper - confirmed by inspecting ObjectMapperFactory's bytecode.
  // PlatformDashboardConfigResource (operations-api-jaxrs, shared unchanged across all three
  // frameworks) needs the Jackson 2 (com.fasterxml.jackson.databind) ObjectMapper that Quarkus
  // and Spring both still provide out of the box; Micronaut needs an explicit producer for it.
  @Singleton
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  // tenants/linkedApps/components: injected as List<T> - Micronaut collects every @EachProperty
  // bean of that type into the list automatically, in index order. See
  // PlatformDashboardMicronautProperties's Javadoc for why these aren't record components there.
  @Singleton
  PlatformDashboardSettings settings(
      PlatformDashboardMicronautProperties properties,
      List<PlatformDashboardMicronautTenant> tenants,
      List<PlatformDashboardMicronautLinkedApp> linkedApps,
      List<PlatformDashboardMicronautComponent> components,
      @Value("${forwardmeasure.platform.display-name:kriyagentic}") String platformDisplayName) {
    return new PlatformDashboardSettings(
        properties.oidcUrl(),
        properties.oidcRealm(),
        properties.oidcClientId(),
        properties.environment(),
        properties.probeTimeout(),
        properties.observationTtl(),
        tenants.stream().map(PlatformDashboardMicronautBinding::toTenant).toList(),
        linkedApps.stream().map(PlatformDashboardMicronautBinding::toApplication).toList(),
        components.stream().map(PlatformDashboardMicronautBinding::toComponent).toList(),
        platformDisplayName);
  }

  @Singleton
  ApplicationEventListener<StartupEvent> validateSettingsOnStartup(
      PlatformDashboardSettings settings) {
    return event -> PlatformDashboardConfigurationValidator.validate(settings);
  }

  // preDestroy = "close" - Micronaut's factory-bean disposal hook, invoked by name on the
  // produced instance at context shutdown; the direct Micronaut equivalent of Quarkus's
  // @Disposes and Spring's inferred-destroy-method convention for the same plain close() method.
  @Bean(preDestroy = "close")
  @Singleton
  PlatformHealthService healthService(PlatformDashboardSettings settings) {
    return new PlatformHealthService(
        settings.components(), settings.probeTimeout(), settings.observationTtl());
  }

  @Singleton
  PlatformTenantResolver tenantResolver(PlatformDashboardSettings settings) {
    return new PlatformTenantResolver(settings.tenants());
  }

  private static PlatformDashboardSettings.Tenant toTenant(
      PlatformDashboardMicronautTenant tenant) {
    return new PlatformDashboardSettings.Tenant(
        tenant.host(), tenant.did(), tenant.name(), tenant.displayName());
  }

  private static PlatformDashboardSettings.Application toApplication(
      PlatformDashboardMicronautLinkedApp application) {
    return new PlatformDashboardSettings.Application(
        application.id(), application.name(), application.description(), application.href());
  }

  private static PlatformDashboardSettings.Component toComponent(
      PlatformDashboardMicronautComponent component) {
    return new PlatformDashboardSettings.Component(
        component.id(),
        component.name(),
        component.category(),
        component.probe(),
        component.healthUri(),
        component.consoleUri());
  }
}
