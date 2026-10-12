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
import { describe, expect, it } from "vitest";
import { summarize, titleCase } from "./status";

describe("platform status presentation", () => {
  it("separates available components from components requiring attention", () => {
    const summary = summarize([
      { id: "kafka", name: "Kafka", category: "MESSAGING", status: "UP", checkedAt: new Date() },
      { id: "search", name: "Search", category: "SEARCH", status: "DOWN", checkedAt: new Date() },
    ]);
    expect(summary).toEqual({ available: 1, attention: 1, total: 2 });
  });

  it("presents contract enums as business labels", () => {
    expect(titleCase("SCHEMA_GOVERNANCE")).toBe("Schema Governance");
  });
});
