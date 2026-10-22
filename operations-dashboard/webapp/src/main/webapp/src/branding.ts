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
import markDark from "@forwardmeasure/presentation-system/brands/kriyagentic/kriyagentic-mark-dark.svg";
import markLight from "@forwardmeasure/presentation-system/brands/kriyagentic/kriyagentic-mark-light.svg";
import symbolDark from "@forwardmeasure/presentation-system/brands/kriyagentic/kriyagentic-symbol-dark.svg";
import symbolLight from "@forwardmeasure/presentation-system/brands/kriyagentic/kriyagentic-symbol-light.svg";

/** The platform's symbol, light and dark, for the TopBar's logo slot (never the lockup). */
export const PLATFORM_SYMBOL = { light: symbolLight, dark: symbolDark };

/** The platform's full mark, light and dark, for the screens shown while the dashboard starts. */
export const PLATFORM_MARK = { light: markLight, dark: markDark };

export function platformDisplayName(): string {
  return window.__FORWARDMEASURE_PLATFORM_CONFIG__?.platformDisplayName?.trim() || "kriyagentic";
}

export const PRODUCT_NAME = "Platform Dashboard";

export function dashboardTitle(): string {
  return `${platformDisplayName()} · ${PRODUCT_NAME}`;
}
