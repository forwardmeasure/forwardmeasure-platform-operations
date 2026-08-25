package com.forwardmeasure.platform.dashboard.quarkus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

/**
 * End-to-end proof that the {@code @Path} fix on {@code PlatformDashboardResource} actually
 * resolves {@code /platform/api/v1/components} - a plain unauthenticated 404 here (rather than 401)
 * would mean the class- and interface-level {@code @Path} silently failed to concatenate again,
 * exactly the bug this reimplementation fixes. Test config lives under the "%test" profile in
 * src/main/resources/application.yaml.
 */
@QuarkusTest
class PlatformDashboardResourceTest {

  @Test
  void componentsRequiresAuthentication() {
    given().when().get("/api/v1/components").then().statusCode(401);
  }

  @Test
  @TestSecurity(user = "test-user", roles = "platform-viewer")
  void componentsListsConfiguredComponent() {
    given()
        .when()
        .get("/api/v1/components")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].id", equalTo("test-component"));
  }

  @Test
  void configJsIsPubliclyReadable() {
    given()
        .when()
        .get("/config.js")
        .then()
        .statusCode(200)
        .body(containsString("__FORWARDMEASURE_PLATFORM_CONFIG__"));
  }

  @Test
  void tenantContextResolvesConfiguredHost() {
    given()
        .header("Host", "dashboard.test.local")
        .when()
        .get("/tenant-context")
        .then()
        .statusCode(200)
        .body("tenant_name", equalTo("test-tenant"));
  }

  @Test
  void tenantContextRejectsUnknownHost() {
    given()
        .header("Host", "unknown.test.local")
        .when()
        .get("/tenant-context")
        .then()
        .statusCode(404);
  }
}
