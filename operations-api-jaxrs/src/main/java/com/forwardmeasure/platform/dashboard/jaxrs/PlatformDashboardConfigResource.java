package com.forwardmeasure.platform.dashboard.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forwardmeasure.platform.dashboard.PlatformApplicationPath;
import com.forwardmeasure.platform.dashboard.PlatformDashboardSettings;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.Objects;

/** Publishes browser-safe deployment configuration without rebuilding the SPA. */
@Path("/config.js")
public class PlatformDashboardConfigResource {

  private final PlatformDashboardSettings settings;

  private final ObjectMapper json;

  public PlatformDashboardConfigResource(PlatformDashboardSettings settings, ObjectMapper json) {
    this.settings = Objects.requireNonNull(settings, "settings");
    this.json = Objects.requireNonNull(json, "json");
  }

  @GET
  @Produces("application/javascript")
  public Response configuration() {
    try {
      String body =
          "window.__FORWARDMEASURE_PLATFORM_CONFIG__="
              + json.writeValueAsString(
                  Map.of(
                      "tenantContextUrl",
                      "/platform/tenant-context",
                      "apiUrl",
                      "/platform/api/v1",
                      "platformDisplayName",
                      settings.platformDisplayName(),
                      "environment",
                      settings.environment(),
                      "applications",
                      settings.applications().stream()
                          .map(PlatformDashboardConfigResource::browserApplication)
                          .toList()))
              + ";";
      CacheControl cache = new CacheControl();
      cache.setNoStore(true);
      return Response.ok(body)
          .cacheControl(cache)
          .header("X-Content-Type-Options", "nosniff")
          .build();
    } catch (JsonProcessingException failure) {
      throw new IllegalStateException(
          "Platform Dashboard configuration cannot be serialized", failure);
    }
  }

  private static BrowserApplication browserApplication(
      PlatformDashboardSettings.Application application) {
    PlatformApplicationPath href = PlatformApplicationPath.parse(application.href());
    return new BrowserApplication(
        application.id(), application.name(), application.description(), href.value());
  }

  record BrowserApplication(String id, String name, String description, String href) {}
}
