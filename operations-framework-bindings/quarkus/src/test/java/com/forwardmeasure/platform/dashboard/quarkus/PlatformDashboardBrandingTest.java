package com.forwardmeasure.platform.dashboard.quarkus;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(PlatformDashboardBrandingTest.CustomBrand.class)
class PlatformDashboardBrandingTest {
  private static final String LABEL = "Company \"A\" & <B>";

  public static class CustomBrand implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return Map.of("forwardmeasure.platform.display-name", LABEL);
    }
  }

  @Test
  void publishesDeploymentBrandAsJsonWithoutChangingIdentityEndpoints() throws Exception {
    var response = given().get("/config.js").then().statusCode(200).extract().response();
    assertTrue(response.header("Cache-Control").contains("no-store"));
    String body = response.asString();
    String prefix = "window.__FORWARDMEASURE_PLATFORM_CONFIG__=";
    assertTrue(body.startsWith(prefix));
    var config = new ObjectMapper().readTree(body.substring(prefix.length(), body.length() - 1));
    assertEquals(LABEL, config.path("platformDisplayName").asText());
    assertEquals("/platform/tenant-context", config.path("tenantContextUrl").asText());
    assertEquals("/platform/api/v1", config.path("apiUrl").asText());
  }
}
