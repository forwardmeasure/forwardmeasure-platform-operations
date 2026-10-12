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
import type { PlatformComponent } from "../generated-api/src/index";

export type PlatformSummary = {
  available: number;
  attention: number;
  total: number;
};

export function summarize(components: PlatformComponent[]): PlatformSummary {
  return components.reduce((result, component) => {
    result.total += 1;
    if (component.status === "UP") result.available += 1;
    else result.attention += 1;
    return result;
  }, { available: 0, attention: 0, total: 0 });
}

export function titleCase(value: string): string {
  return value.toLowerCase().split("_")
    .map(part => part.charAt(0).toUpperCase() + part.slice(1)).join(" ");
}
