package com.forwardmeasure.platform.dashboard.quarkus;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forwardmeasure.platform.dashboard.PlatformDashboardConfigurationValidator;
import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.PlatformHealthService;
import com.forwardmeasure.platform.dashboard.PlatformTenantResolver;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardConfigResource;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardResource;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformTenantContextResource;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Quarkus composition root for the platform dashboard capability - host integration only (bean
 * registration, config resolution, lifecycle wiring), no domain logic, matching
 * PROJECT_MANIFESTO.md 3.10's binding-module contract.
 *
 * <p>Every producer below is scoped {@code @Singleton} (CDI's pseudo-scope), not
 * {@code @ApplicationScoped}: {@code @ApplicationScoped} is a normal scope, which Arc implements
 * with a generated client proxy subclassing the bean type - impossible for the record/final-class
 * types these producers return ({@link PlatformDashboardSettings} is a record; {@link
 * PlatformHealthService}, {@link PlatformTenantResolver}, and {@link
 * QuarkusPlatformDashboardResource} are all {@code final}). {@code @Singleton} beans are injected
 * directly with no proxy, which is exactly what a composition root full of immutable,
 * app-lifetime-singleton services needs anyway.
 */
@ApplicationScoped
public class PlatformDashboardQuarkusBinding {

  @Produces
  @Singleton
  PlatformDashboardSettings settings(PlatformDashboardConfiguration configuration,
      @ConfigProperty(name = "forwardmeasure.platform.display-name", defaultValue = "kriyagentic") String platformDisplayName) {
    return new PlatformDashboardSettings(
        configuration.oidcUrl(),
        configuration.oidcRealm(),
        configuration.oidcClientId(),
        configuration.environment(),
        configuration.probeTimeout(),
        configuration.observationTtl(),
        configuration.tenants().stream().map(PlatformDashboardQuarkusBinding::toTenant).toList(),
        configuration.applications().stream()
            .map(PlatformDashboardQuarkusBinding::toApplication)
            .toList(),
        configuration.components().stream()
            .map(PlatformDashboardQuarkusBinding::toComponent)
            .toList(),
        platformDisplayName);
  }

  // Observer, not just @Startup on this class - @Startup eagerly instantiates the whole bean,
  // but validation needs the *settings* producer to have already run; an explicit StartupEvent
  // observer taking PlatformDashboardSettings as a parameter guarantees that ordering and mirrors
  // Spring's ApplicationReadyEvent/Micronaut's StartupEvent listeners used for the same purpose
  // in the other two bindings.
  void validateOnStartup(@Observes StartupEvent event, PlatformDashboardSettings settings) {
    PlatformDashboardConfigurationValidator.validate(settings);
  }

  @Produces
  @Singleton
  PlatformHealthService healthService(PlatformDashboardSettings settings) {
    return new PlatformHealthService(
        settings.components(), settings.probeTimeout(), settings.observationTtl());
  }

  void closeHealthService(@Disposes PlatformHealthService health) {
    health.close();
  }

  @Produces
  @Singleton
  PlatformTenantResolver tenantResolver(PlatformDashboardSettings settings) {
    return new PlatformTenantResolver(settings.tenants());
  }

  // PlatformDashboardResource directly, no Quarkus-only subclass: RESTEasy Reactive finds
  // resource classes by scanning for a directly-present @Path via Jandex at build time, and
  // @Path isn't @Inherited - a subclass that only inherits it (as an earlier version of this
  // binding had, purely to attach @Blocking) is invisible to that scan, so its route silently
  // never registers (confirmed the hard way: CDI flags the producer as an unused/removed bean,
  // and every request 404s). @Blocking wasn't needed anyway: methods with a plain, non-reactive
  // return type (Response, not Uni<Response>) already dispatch to a worker thread by default,
  // matching openworkflow-execution-jaxrs.ExecutionResource's own blocking JPA calls, which
  // likewise never annotate anything with @Blocking.
  @Produces
  @Singleton
  PlatformDashboardResource dashboardResource(PlatformHealthService health) {
    return new PlatformDashboardResource(health);
  }

  @Produces
  @Singleton
  PlatformDashboardConfigResource configResource(
      PlatformDashboardSettings settings, ObjectMapper json) {
    return new PlatformDashboardConfigResource(settings, json);
  }

  @Produces
  @Singleton
  PlatformTenantContextResource tenantContextResource(
      PlatformTenantResolver tenants, PlatformDashboardSettings settings) {
    return new PlatformTenantContextResource(tenants, settings);
  }

  private static PlatformDashboardSettings.Tenant toTenant(
      PlatformDashboardConfiguration.Tenant tenant) {
    return new PlatformDashboardSettings.Tenant(
        tenant.host(), tenant.did(), tenant.name(), tenant.displayName());
  }

  private static PlatformDashboardSettings.Application toApplication(
      PlatformDashboardConfiguration.Application application) {
    return new PlatformDashboardSettings.Application(
        application.id(), application.name(), application.description(), application.href());
  }

  private static PlatformDashboardSettings.Component toComponent(
      PlatformDashboardConfiguration.Component component) {
    return new PlatformDashboardSettings.Component(
        component.id(),
        component.name(),
        component.category(),
        component.probe(),
        component.healthUri(),
        component.consoleUri());
  }
}
