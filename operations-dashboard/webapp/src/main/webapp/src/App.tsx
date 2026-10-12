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
import { useEffect, useState } from "react";
import type { PlatformComponent } from "../generated-api/src/index";
import {
  Boxes,
  CircleAlert,
  CircleCheck,
  CircleHelp,
  ExternalLink,
  Inbox,
  RefreshCw,
  TriangleAlert,
} from "lucide-react";
import { initTheme } from "@forwardmeasure/presentation-system/theme";
import type { PlatformIdentity } from "./runtime";
import { summarize, titleCase } from "./status";
import { platformDisplayName, PRODUCT_NAME } from "./branding";
import { TopBar } from "@forwardmeasure/presentation-system/shell";

const ICON = { size: 16, strokeWidth: 1.5, absoluteStrokeWidth: true, "aria-hidden": true } as const;

type Section = "overview" | "applications" | "services";

export function App({ identity }: { identity: PlatformIdentity }) {
  const [components, setComponents] = useState<PlatformComponent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>();
  const [section, setSection] = useState<Section>("overview");

  async function refresh() {
    setLoading(true);
    setError(undefined);
    try {
      setComponents(await identity.api.listPlatformComponents());
    } catch (failure) {
      setError(failure instanceof Error ? failure.message : String(failure));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void refresh(); }, []);
  // Keep following the operating system while the person's choice is "Match system".
  useEffect(() => initTheme(), []);

  function navigate(next: Section) {
    setSection(next);
    document.getElementById(next)?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  const summary = summarize(components);
  return <>
    <TopBar<Section>
      platform={platformDisplayName()}
      product={PRODUCT_NAME}
      sections={[
        { value: "overview", label: "Overview" },
        { value: "applications", label: "Applications" },
        { value: "services", label: "Services" },
      ]}
      current={section}
      onNavigate={navigate}
      tenant={identity.tenantName}
      person={{ name: identity.displayName }}
      onThemeChange={() => {}}
      onSignOut={() => void identity.keycloak.logout({ redirectUri: window.location.origin })}
    />
    <main className="dashboard">
      <section className="page-head" id="overview">
        <p className="kg-text-label page-eyebrow">Shared Platform · <span className="kg-tenant">{identity.environment}</span></p>
        <h1 className="kg-text-title">Operational Clarity Across the Stack</h1>
        <p className="kg-text-body page-lede">Identity, messaging, schemas, search, analytics and model services, observed through one authenticated, read-only view.</p>
      </section>
      {error && <div className="kg-banner kg-banner--danger" role="alert">
        <CircleAlert {...ICON} className="kg-icon"/>
        <div className="kg-banner__text"><p className="kg-banner__title">Couldn't Load Platform Status</p><p className="kg-banner__body">{error}</p></div>
        <div className="kg-banner__actions"><button type="button" className="kg-btn kg-btn--sm kg-btn--secondary" onClick={() => void refresh()}>Try Again</button></div>
      </div>}
      <section className="metrics" aria-label="Platform Summary">
        <article className="kg-card"><p className="kg-card__meta">Available</p><strong className="metric">{summary.available}</strong><p className="kg-card__meta">Healthy Components</p></article>
        <article className="kg-card"><p className="kg-card__meta">Needs Attention</p><strong className="metric">{summary.attention}</strong><p className="kg-card__meta">Degraded, Down or Unknown</p></article>
        <article className="kg-card"><p className="kg-card__meta">Observed</p><strong className="metric">{summary.total}</strong><p className="kg-card__meta">Configured Platform Components</p></article>
      </section>
      <section className="dashboard-section" id="applications">
        <div className="section-head"><h2 className="kg-text-subheading">Tenant Applications</h2><span className="kg-card__meta">{identity.applications.length} applications</span></div>
        {identity.applications.length === 0
          ? <div className="kg-card"><div className="kg-empty"><Inbox {...ICON} className="kg-icon"/><h3 className="kg-empty__title">No Tenant Applications Yet</h3><p className="kg-empty__body">Applications appear here once they are configured for this tenant.</p></div></div>
          : <div className="card-grid">{identity.applications.map(application =>
            <a className="kg-card application-card" href={application.href} key={application.id}>
              <span className="application-icon"><Boxes {...ICON}/></span>
              <span><span className="kg-card__title application-name">{application.name}</span><span className="kg-card__meta">{application.description}</span></span>
              <ExternalLink {...ICON} className="kg-icon"/>
            </a>)}</div>}
      </section>
      <section className="dashboard-section" id="services">
        <div className="section-head">
          <h2 className="kg-text-subheading">Platform Services</h2>
          <span className="section-actions">
            <span className="kg-card__meta">{loading ? "Refreshing…" : `${components.length} components`}</span>
            <button className="kg-btn kg-btn--sm kg-btn--secondary" disabled={loading} onClick={() => void refresh()} type="button">
              <RefreshCw {...ICON} className={loading ? "kg-icon kg-spinner" : "kg-icon"}/>Refresh
            </button>
          </span>
        </div>
        {loading && components.length === 0
          ? <span className="kg-loading"><RefreshCw {...ICON} className="kg-icon kg-spinner"/>Loading platform observations</span>
          : components.length === 0
            ? <div className="kg-card"><div className="kg-empty"><Inbox {...ICON} className="kg-icon"/><h3 className="kg-empty__title">No Platform Components Yet</h3><p className="kg-empty__body">Components appear here once they are configured for this platform.</p></div></div>
            : <div className="card-grid">{components.map(component => <ComponentCard key={component.id} value={component}/>)}</div>}
      </section>
    </main>
  </>;
}

const STATUS: Record<string, { tone: string; word: string; Icon: typeof CircleCheck }> = {
  UP: { tone: "kg-opstatus--success", word: "Healthy", Icon: CircleCheck },
  DEGRADED: { tone: "kg-opstatus--warning", word: "Degraded", Icon: TriangleAlert },
  DOWN: { tone: "kg-opstatus--danger", word: "Down", Icon: CircleAlert },
};

function ComponentCard({ value }: { value: PlatformComponent }) {
  const status = STATUS[value.status] ?? { tone: "", word: titleCase(value.status), Icon: CircleHelp };
  return <article className="kg-card service-card">
    <div className="kg-card__head">
      <span className="kg-card__meta">{titleCase(value.category)}</span>
      <span className={`kg-opstatus ${status.tone}`}><status.Icon {...ICON} className="kg-icon"/>{status.word}</span>
    </div>
    <h3 className="kg-card__title">{value.name}</h3>
    <p className="service-detail">{value.detail ?? "No health detail was supplied."}</p>
    <div className="kg-card__foot service-foot">
      <span className="kg-card__meta">{value.latencyMilliseconds === undefined ? "No latency reported" : `${value.latencyMilliseconds} ms`}</span>
      <span className="kg-card__meta">Checked {value.checkedAt.toLocaleTimeString()}</span>
      {value.consoleUrl && <a className="kg-btn kg-btn--sm kg-btn--quiet" href={value.consoleUrl} target="_blank" rel="noreferrer">Open Console<ExternalLink {...ICON} className="kg-icon"/></a>}
    </div>
  </article>;
}
