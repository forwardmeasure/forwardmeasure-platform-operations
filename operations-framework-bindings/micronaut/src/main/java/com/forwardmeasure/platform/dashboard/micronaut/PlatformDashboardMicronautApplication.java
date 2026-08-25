package com.forwardmeasure.platform.dashboard.micronaut;

import com.forwardmeasure.platform.operations.api.model.PlatformComponent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;

// PlatformComponent is a generated OpenAPI model (operations-api-models, shared unchanged across
// all three frameworks) - it has no Micronaut @Serdeable annotation of its own, and can't (it's
// not our source to annotate). Without @SerdeImport, serializing List<PlatformComponent> - the
// /api/v1/components response body - fails at request time with a 500 ("No serializable
// introspection present"), confirmed the hard way once a correctly-authorized request actually
// reached that code path (every earlier test of this endpoint had been rejected by security
// first, so this never surfaced until the role-check bugs were fixed).
@SerdeImport(PlatformComponent.class)
public final class PlatformDashboardMicronautApplication {
  private PlatformDashboardMicronautApplication() {}

  public static void main(String[] arguments) {
    Micronaut.run(PlatformDashboardMicronautApplication.class, arguments);
  }
}
