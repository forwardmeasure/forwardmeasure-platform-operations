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
import React from "react";
import ReactDOM from "react-dom/client";
import { App } from "./App";
import { initialize } from "./runtime";
import "@forwardmeasure/presentation-system/index.css";
import { applyThemePreference, storedThemePreference } from "@forwardmeasure/presentation-system/theme";
import "./styles.css";
import { dashboardTitle } from "./branding";

// Sets data-theme on <html> from the person's light / dark / match-system choice before the first
// render, so nothing paints in the wrong theme. App keeps following the operating system.
applyThemePreference(storedThemePreference());

document.title = dashboardTitle();

const root = ReactDOM.createRoot(document.getElementById("root")!);
root.render(<div className="boot">Opening {dashboardTitle()}…</div>);

initialize()
  .then(identity => root.render(<React.StrictMode><App identity={identity}/></React.StrictMode>))
  .catch(failure => root.render(<main className="fatal" role="alert"><p>{dashboardTitle()}</p><h1>Unable to Start This View</h1><pre>{failure instanceof Error ? failure.message : String(failure)}</pre></main>));
