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
package com.forwardmeasure.platform.dashboard;

import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Fails deployment-owned dashboard configuration that's unsafe. Plain static validation, no
 * CDI/{@code @Startup} - each framework binding invokes {@link #validate} at its own startup hook
 * (Quarkus {@code @Startup}, Spring {@code ApplicationReadyEvent}, Micronaut {@code StartupEvent}),
 * matching how lifecycle wiring is binding-module scope per PROJECT_MANIFESTO.md's convention.
 */
public final class PlatformDashboardConfigurationValidator {

  private PlatformDashboardConfigurationValidator() {}

  public static void validate(PlatformDashboardSettings settings) {
    Objects.requireNonNull(settings, "settings");
    validateOidcUrl(settings.oidcUrl());
    requireText(settings.oidcRealm(), "oidcRealm");
    requireText(settings.oidcClientId(), "oidcClientId");
    requireText(settings.environment(), "environment");
    requirePositive(settings.probeTimeout(), "probeTimeout");
    requirePositive(settings.observationTtl(), "observationTtl");

    if (settings.tenants().isEmpty()) {
      throw new IllegalArgumentException(
          "At least one Platform Dashboard tenant must be configured");
    }
    Set<String> tenantHosts = new HashSet<>();
    Set<String> tenantDids = new HashSet<>();
    Set<String> tenantNames = new HashSet<>();
    for (PlatformDashboardSettings.Tenant tenant : settings.tenants()) {
      String host = PlatformTenantResolver.canonicalHost(tenant.host());
      if (!tenantHosts.add(host)) {
        throw new IllegalArgumentException("Duplicate Platform Dashboard tenant host: " + host);
      }
      requireDid(tenant.did());
      if (!tenantDids.add(tenant.did())) {
        throw new IllegalArgumentException(
            "Duplicate Platform Dashboard tenant DID: " + tenant.did());
      }
      requireText(tenant.name(), "tenant.name");
      if (!tenantNames.add(tenant.name())) {
        throw new IllegalArgumentException(
            "Duplicate Platform Dashboard tenant name: " + tenant.name());
      }
      requireText(tenant.displayName(), "tenant.displayName");
    }

    Set<String> applicationIds = new HashSet<>();
    for (PlatformDashboardSettings.Application application : settings.applications()) {
      requireUniqueId(application.id(), applicationIds, "application");
      requireText(application.name(), "application.name");
      requireText(application.description(), "application.description");
      PlatformApplicationPath.parse(application.href());
    }

    Set<String> componentIds = new HashSet<>();
    for (PlatformDashboardSettings.Component component : settings.components()) {
      requireUniqueId(component.id(), componentIds, "component");
      requireText(component.name(), "component.name");
      requireText(component.category(), "component.category");
      PlatformHealthService.validate(component.probe(), component.healthUri());
      component.consoleUri().ifPresent(PlatformDashboardConfigurationValidator::validateConsoleUri);
    }
  }

  private static void requirePositive(Duration value, String field) {
    if (value.isZero() || value.isNegative()) {
      throw new IllegalArgumentException(field + " must be positive");
    }
  }

  private static void requireUniqueId(String id, Set<String> observed, String kind) {
    requireText(id, kind + ".id");
    if (!observed.add(id)) {
      throw new IllegalArgumentException("Duplicate Platform Dashboard " + kind + " id: " + id);
    }
  }

  private static void requireText(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
  }

  private static void requireDid(String value) {
    requireText(value, "tenant.did");
    int methodSeparator = value.indexOf(':', 4);
    if (!value.startsWith("did:")
        || methodSeparator < 5
        || methodSeparator == value.length() - 1
        || value.chars().anyMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("Platform Dashboard tenant DID is invalid: " + value);
    }
  }

  private static void validateOidcUrl(String value) {
    requireText(value, "oidcUrl");
    URI uri;
    try {
      uri = URI.create(value);
    } catch (IllegalArgumentException invalid) {
      throw new IllegalArgumentException("Platform Dashboard OIDC URL is invalid", invalid);
    }
    if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
        || uri.getHost() == null
        || uri.getUserInfo() != null
        || uri.getQuery() != null
        || uri.getFragment() != null) {
      throw new IllegalArgumentException(
          "Platform Dashboard OIDC URL must be an HTTP(S) base URL "
              + "without user info, query or fragment");
    }
  }

  private static void validateConsoleUri(URI uri) {
    String scheme = uri.getScheme();
    if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
        || uri.getHost() == null
        || uri.getUserInfo() != null
        || uri.getFragment() != null) {
      throw new IllegalArgumentException(
          "Platform console URI must be an HTTP(S) URI without user info or fragment");
    }
  }
}
