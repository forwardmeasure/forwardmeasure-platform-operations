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
