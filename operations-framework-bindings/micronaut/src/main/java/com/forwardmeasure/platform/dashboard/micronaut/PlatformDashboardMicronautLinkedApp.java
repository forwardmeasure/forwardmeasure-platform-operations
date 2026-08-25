package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.EachProperty;

/**
 * One {@code forwardmeasure.platform.dashboard.applications[N]} entry - see {@link
 * PlatformDashboardMicronautProperties}. Named "LinkedApp", not "Application": that name is already
 * taken by {@link PlatformDashboardMicronautApplication}, the {@code Micronaut.run(...)} entry
 * point.
 */
@EachProperty(value = "forwardmeasure.platform.dashboard.applications", list = true)
public record PlatformDashboardMicronautLinkedApp(
    String id, String name, String description, String href) {}
