package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.bind.annotation.Bindable;
import java.net.URI;
import java.util.Optional;

/**
 * One {@code forwardmeasure.platform.dashboard.components[N]} entry - see {@link
 * PlatformDashboardMicronautProperties}.
 */
@EachProperty(value = "forwardmeasure.platform.dashboard.components", list = true)
public record PlatformDashboardMicronautComponent(
    String id,
    String name,
    String category,
    @Bindable(defaultValue = "HTTP") String probe,
    URI healthUri,
    Optional<URI> consoleUri) {}
