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
