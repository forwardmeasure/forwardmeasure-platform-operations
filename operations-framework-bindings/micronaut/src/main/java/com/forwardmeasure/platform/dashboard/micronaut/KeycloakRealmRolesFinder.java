package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.token.DefaultRolesFinder;
import io.micronaut.security.token.RolesFinder;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;

/**
 * Extracts Keycloak's nested {@code realm_access.roles} claim - {@link DefaultRolesFinder} only
 * resolves a flat, configurably-named claim, not a nested object, so {@code @RolesAllowed} on the
 * shared resources (checked via micronaut-jaxrs-server-security against {@code
 * Authentication.getRoles()}) would otherwise never match anything. The Spring/Jersey equivalent of
 * this is {@code PlatformDashboardSpringBinding}'s {@code jwtAuthenticationConverter()}; Quarkus
 * has no equivalent to write since quarkus-oidc resolves Keycloak's realm roles out of the box.
 */
@Singleton
@Replaces(DefaultRolesFinder.class)
public class KeycloakRealmRolesFinder implements RolesFinder {

  @Override
  public List<String> resolveRoles(Map<String, Object> claims) {
    if (claims.get("realm_access") instanceof Map<?, ?> realmAccess
        && realmAccess.get("roles") instanceof List<?> roles) {
      return roles.stream().map(String::valueOf).toList();
    }
    return List.of();
  }
}
