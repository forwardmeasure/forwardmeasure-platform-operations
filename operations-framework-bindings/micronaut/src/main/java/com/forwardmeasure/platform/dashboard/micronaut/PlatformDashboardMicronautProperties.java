package com.forwardmeasure.platform.dashboard.micronaut;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;
import java.time.Duration;

/**
 * Micronaut-native mirror of Quarkus's {@code PlatformDashboardConfiguration}
 * {@code @ConfigMapping} - scalars only. Tenants/components/applications are NOT record components
 * here: binding a {@code List<SomeRecord>} on a {@code @ConfigurationProperties} record silently
 * produces an empty list for indexed config (confirmed the hard way, even with
 * {@code @Introspected} on the nested type) - Micronaut's actual mechanism for a repeatable,
 * indexed config section is {@code @EachProperty}, which builds one bean per list entry rather than
 * binding into a single record's list field. See {@link PlatformDashboardMicronautTenant} and its
 * siblings.
 */
@ConfigurationProperties("forwardmeasure.platform.dashboard")
public record PlatformDashboardMicronautProperties(
    String oidcUrl,
    String oidcRealm,
    String oidcClientId,
    @Bindable(defaultValue = "Production") String environment,
    @Bindable(defaultValue = "PT2S") Duration probeTimeout,
    @Bindable(defaultValue = "PT10S") Duration observationTtl) {}
