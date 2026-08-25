package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.EachProperty;

/**
 * One {@code forwardmeasure.platform.dashboard.tenants[N]} entry - see {@link
 * PlatformDashboardMicronautProperties}.
 */
@EachProperty(value = "forwardmeasure.platform.dashboard.tenants", list = true)
public record PlatformDashboardMicronautTenant(
    String host, String did, String name, String displayName) {}
