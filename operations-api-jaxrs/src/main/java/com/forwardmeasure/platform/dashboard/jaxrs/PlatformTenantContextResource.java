package com.forwardmeasure.platform.dashboard.jaxrs;

import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import com.forwardmeasure.platform.dashboard.PlatformTenantResolver;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the tenant bootstrap from the request hostname allowlist. Host matching/canonicalization
 * itself lives in {@link PlatformTenantResolver} (operations-api-application) - this resource only
 * builds the {@code Response} around the result, keeping HTTP concerns out of the domain layer.
 *
 * <p>{@code headers} is {@code @Context}-injected as a field, not a constructor parameter: this
 * resource is a single application-scoped instance in every binding, but the request's headers are
 * necessarily per-request. Every JAX-RS runtime here (RESTEasy Reactive under Quarkus, Jersey under
 * Spring, micronaut-jaxrs-server under Micronaut) resolves an {@code @Context}-annotated field on a
 * registered resource against the current request regardless of who constructed the instance -
 * unlike a constructor parameter, which would require each framework's own DI container to supply
 * an application-scoped {@code HttpHeaders} bean, something only Quarkus's CDI does out of the box.
 */
@Path("/tenant-context")
public class PlatformTenantContextResource {

  @Context private HttpHeaders headers;

  private final PlatformTenantResolver tenants;

  private final PlatformDashboardSettings settings;

  public PlatformTenantContextResource(
      PlatformTenantResolver tenants, PlatformDashboardSettings settings) {
    this.tenants = Objects.requireNonNull(tenants, "tenants");
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response context() {
    final Optional<PlatformDashboardSettings.Tenant> tenant;
    try {
      tenant = tenants.resolve(headers.getHeaderString(HttpHeaders.HOST));
    } catch (IllegalArgumentException invalidHost) {
      return problem(
          Response.Status.BAD_REQUEST,
          "invalid-host",
          "Invalid Host",
          "The HTTP Host header is invalid");
    }
    if (tenant.isEmpty()) {
      return problem(
          Response.Status.NOT_FOUND,
          "unknown-host",
          "Tenant Not Found",
          "This hostname is not assigned to a platform tenant");
    }

    var value = new LinkedHashMap<String, String>();
    value.put("tenant_did", tenant.get().did());
    value.put("tenant_name", tenant.get().name());
    value.put("display_name", tenant.get().displayName());
    value.put("oidc_url", settings.oidcUrl());
    value.put("oidc_realm", settings.oidcRealm());
    value.put("oidc_client_id", settings.oidcClientId());

    var cache = new CacheControl();
    cache.setNoStore(true);
    return Response.ok(value)
        .cacheControl(cache)
        .header("X-Content-Type-Options", "nosniff")
        .build();
  }

  private static Response problem(
      Response.Status status, String type, String title, String detail) {
    return Response.status(status)
        .type("application/problem+json")
        .entity(
            Map.of(
                "type",
                "https://problems.forwardmeasure.com/tenant/" + type,
                "title",
                title,
                "status",
                status.getStatusCode(),
                "detail",
                detail))
        .header("Cache-Control", "no-store")
        .header("X-Content-Type-Options", "nosniff")
        .build();
  }
}
