package com.forwardmeasure.platform.dashboard.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forwardmeasure.platform.dashboard.PlatformDashboardConfigurationValidator;
import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.PlatformHealthService;
import com.forwardmeasure.platform.dashboard.PlatformTenantResolver;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardConfigResource;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformDashboardResource;
import com.forwardmeasure.platform.dashboard.jaxrs.PlatformTenantContextResource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring composition root for the platform dashboard capability - host integration only, no domain
 * logic, matching PROJECT_MANIFESTO.md 3.10's binding-module contract and the identical shape
 * openworkflow-execution-management-spring-binding uses. Registers the shared -jaxrs resources into
 * Jersey via {@link ResourceConfigCustomizer}, the same pattern that binding's own comment
 * attributes to OpenWorkflowDefinitionManagementSpringBinding.
 *
 * <p>{@code RolesAllowedDynamicFeature} enforces {@code @RolesAllowed} against the JAX-RS {@code
 * SecurityContext}, which in servlet-hosted Jersey delegates to {@code
 * HttpServletRequest.isUserInRole} - populated, in turn, by Spring Security's own {@code
 * SecurityContextHolderAwareRequestFilter} (part of its default filter chain) from the granted
 * authorities on the current {@code Authentication}. {@link #jwtAuthenticationConverter()} is what
 * puts Keycloak's {@code realm_access.roles} claim into those authorities as unprefixed role
 * strings (e.g. {@code "platform-viewer"}), matching the exact literal role names
 * {@code @RolesAllowed} declares on the shared resources - the Spring/Jersey equivalent of
 * quarkus-oidc's own role enforcement. {@code rolePrefix("")} in {@link #platformDashboardSecurity}
 * is required for this to actually work: {@code
 * SecurityContextHolderAwareRequestWrapper.isUserInRole} unconditionally prepends its role prefix
 * (default {@code "ROLE_"}) before checking - there is no fallback to the unprefixed string -
 * confirmed by decompiling it after a real signed-JWT test found every correctly-roled request
 * still 403ing. Verified end-to-end against a locally-minted, real RS256-signed JWT (valid
 * signature, `realm_access.roles` claim) for all of: correct role → 200, wrong role → 403, no roles
 * → 403, invalid signature → 401. OpenWorkflow's own Spring binding has no equivalent to mirror
 * here: its capabilities authorize through {@code AuthorizationService}/AuthZEN instead of
 * {@code @RolesAllowed}.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PlatformDashboardSpringProperties.class)
public class PlatformDashboardSpringBinding {

  @Bean
  PlatformDashboardSettings settings(PlatformDashboardSpringProperties properties) {
    return new PlatformDashboardSettings(
        properties.oidcUrl(),
        properties.oidcRealm(),
        properties.oidcClientId(),
        properties.environment(),
        properties.probeTimeout(),
        properties.observationTtl(),
        properties.tenants().stream().map(PlatformDashboardSpringBinding::toTenant).toList(),
        properties.applications().stream()
            .map(PlatformDashboardSpringBinding::toApplication)
            .toList(),
        properties.components().stream().map(PlatformDashboardSpringBinding::toComponent).toList());
  }

  @Bean
  ApplicationListener<ApplicationReadyEvent> validateSettingsOnStartup(
      PlatformDashboardSettings settings) {
    return event -> PlatformDashboardConfigurationValidator.validate(settings);
  }

  // No explicit destroyMethod - Spring's default "inferred" destroy method detects and calls a
  // public no-arg close() by name convention alone (AbstractAutowireCapableBeanFactory), which
  // PlatformHealthService already has; no framework-specific subclass needed here, unlike
  // Quarkus's @Disposes (a CDI-specific mechanism with no such name-based inference).
  @Bean
  PlatformHealthService healthService(PlatformDashboardSettings settings) {
    return new PlatformHealthService(
        settings.components(), settings.probeTimeout(), settings.observationTtl());
  }

  @Bean
  PlatformTenantResolver tenantResolver(PlatformDashboardSettings settings) {
    return new PlatformTenantResolver(settings.tenants());
  }

  @Bean
  PlatformDashboardResource dashboardResource(PlatformHealthService health) {
    return new PlatformDashboardResource(health);
  }

  @Bean
  PlatformDashboardConfigResource configResource(
      PlatformDashboardSettings settings, ObjectMapper json) {
    return new PlatformDashboardConfigResource(settings, json);
  }

  @Bean
  PlatformTenantContextResource tenantContextResource(
      PlatformTenantResolver tenants, PlatformDashboardSettings settings) {
    return new PlatformTenantContextResource(tenants, settings);
  }

  // permitAll(), not authenticated(): only PlatformDashboardResource carries @RolesAllowed -
  // PlatformDashboardConfigResource and PlatformTenantContextResource are deliberately public
  // (confirmed the hard way: anyRequest().authenticated() blanket-401'd config.js and
  // tenant-context too). Authorization at this granularity belongs to Jersey's
  // RolesAllowedDynamicFeature, which already reads @RolesAllowed per-resource, matching
  // Quarkus's own behavior (quarkus-oidc only gates @RolesAllowed/@Authenticated resources).
  // oauth2ResourceServer() still runs here regardless of permitAll(): it's the authentication
  // step (parse a bearer token if one is present, populate SecurityContext), not authorization;
  // RolesAllowedDynamicFeature's downstream isUserInRole check is what actually denies access.
  @Bean
  SecurityFilterChain platformDashboardSecurity(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.jwt(
                    jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        // Empty prefix, not the "ROLE_" default: isUserInRole("platform-viewer") would otherwise
        // check for authority "ROLE_platform-viewer", which jwtAuthenticationConverter() never
        // grants - confirmed the hard way (a valid, correctly-roled token still 403'd).
        .servletApi(servletApi -> servletApi.rolePrefix(""))
        .build();
  }

  /** Extracts Keycloak's {@code realm_access.roles} claim - not read by Spring's own default. */
  private static Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return jwt -> {
      Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
      @SuppressWarnings("unchecked")
      Collection<String> roles =
          realmAccess == null
              ? List.of()
              : (Collection<String>) realmAccess.getOrDefault("roles", List.of());
      var authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
      return new JwtAuthenticationToken(jwt, authorities);
    };
  }

  @Bean
  SpaStaticResource spaStaticResource() {
    return new SpaStaticResource();
  }

  @Bean
  ResourceConfigCustomizer platformDashboardResourceConfigCustomizer(
      PlatformDashboardResource dashboard,
      PlatformDashboardConfigResource config,
      PlatformTenantContextResource tenantContext,
      SpaStaticResource spa) {
    return resourceConfig ->
        resourceConfig
            .register(dashboard)
            .register(config)
            .register(tenantContext)
            .register(spa)
            .register(RolesAllowedDynamicFeature.class);
  }

  private static PlatformDashboardSettings.Tenant toTenant(
      PlatformDashboardSpringProperties.Tenant tenant) {
    return new PlatformDashboardSettings.Tenant(
        tenant.host(), tenant.did(), tenant.name(), tenant.displayName());
  }

  private static PlatformDashboardSettings.Application toApplication(
      PlatformDashboardSpringProperties.Application application) {
    return new PlatformDashboardSettings.Application(
        application.id(), application.name(), application.description(), application.href());
  }

  private static PlatformDashboardSettings.Component toComponent(
      PlatformDashboardSpringProperties.Component component) {
    return new PlatformDashboardSettings.Component(
        component.id(),
        component.name(),
        component.category(),
        component.probe(),
        component.healthUri(),
        component.consoleUri());
  }
}
