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
import { afterEach, describe, expect, it, vi } from "vitest";
import { dashboardTitle, platformDisplayName } from "./branding";

afterEach(() => vi.unstubAllGlobals());

describe("runtime platform branding", () => {
  it("accepts a deployment label while leaving API and tenant configuration unchanged", () => {
    const config = { platformDisplayName: 'Example & <Company>', apiUrl: "/platform/api/v1",
      tenantContextUrl: "/platform/tenant-context", environment: "Production", applications: [] };
    const original = structuredClone(config);
    vi.stubGlobal("window", { __FORWARDMEASURE_PLATFORM_CONFIG__: config });
    expect(platformDisplayName()).toBe('Example & <Company>');
    expect(dashboardTitle()).toBe('Example & <Company> · Platform Dashboard');
    expect(config).toEqual(original);
  });

  it("supports an older host without a display name", () => {
    vi.stubGlobal("window", { __FORWARDMEASURE_PLATFORM_CONFIG__: {} });
    expect(platformDisplayName()).toBe("kriyagentic");
  });

  it("uses the default on the startup error screen when runtime config is missing", () => {
    vi.stubGlobal("window", {});
    expect(dashboardTitle()).toBe("kriyagentic · Platform Dashboard");
  });
});
