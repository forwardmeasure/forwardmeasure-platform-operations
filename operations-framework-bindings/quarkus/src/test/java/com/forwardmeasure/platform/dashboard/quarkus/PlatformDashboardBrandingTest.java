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
