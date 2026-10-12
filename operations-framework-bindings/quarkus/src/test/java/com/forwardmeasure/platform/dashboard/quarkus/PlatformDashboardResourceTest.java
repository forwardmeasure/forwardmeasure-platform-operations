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
        .body(containsString("__FORWARDMEASURE_PLATFORM_CONFIG__"))
        .body(containsString("\"platformDisplayName\":\"kriyagentic\""));
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
