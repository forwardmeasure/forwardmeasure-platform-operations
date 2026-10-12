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

import java.net.IDN;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves a tenant from a request hostname against the configured allowlist. Pure host-matching
 * logic - no HTTP awareness - so the JAX-RS resource that calls it only needs to build a {@code
 * Response} around the result, matching how execution-management's application layer keeps
 * transport concerns entirely out of its own module.
 */
public final class PlatformTenantResolver {

  private final Map<String, PlatformDashboardSettings.Tenant> tenantsByHost;

  public PlatformTenantResolver(java.util.List<PlatformDashboardSettings.Tenant> tenants) {
    Objects.requireNonNull(tenants, "tenants");
    this.tenantsByHost =
        tenants.stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    tenant -> canonicalHost(tenant.host()),
                    Function.identity(),
                    (first, duplicate) -> {
                      throw new IllegalArgumentException(
                          "Duplicate Platform Dashboard tenant host: " + duplicate.host());
                    }));
    if (tenantsByHost.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one Platform Dashboard tenant host must be configured");
    }
  }

  /**
   * @throws IllegalArgumentException if {@code rawHost} isn't a valid hostname.
   */
  public Optional<PlatformDashboardSettings.Tenant> resolve(String rawHost) {
    return Optional.ofNullable(tenantsByHost.get(canonicalHost(rawHost)));
  }

  /**
   * @throws IllegalArgumentException if {@code raw} isn't a valid hostname.
   */
  public static String canonicalHost(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("HTTP Host is required");
    }
    String value = raw.strip();
    int colon = value.lastIndexOf(':');
    if (colon > 0 && value.indexOf(':') == colon) {
      value = value.substring(0, colon);
    }
    if (value.endsWith(".")) {
      value = value.substring(0, value.length() - 1);
    }
    return IDN.toASCII(value, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
  }
}
