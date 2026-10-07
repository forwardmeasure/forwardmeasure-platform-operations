# Platform Operations dashboard

This repository supplies the dashboard currently deployed as `forwardmeasure-platform-dashboard`
in the `forwardmeasure-platform` namespace, using its Quarkus image. The source has a shared
application/JAX-RS layer and Quarkus, Spring and Micronaut bindings. The current platform chart
only wires the Quarkus host. The old `operations-keycloak-theme` module remains in the reactor,
but the platform deployment uses stock Keycloak with `keycloak.v2`, not that theme image.

## October 6, 2026 source review

Reviewed baseline: `334730e` on `develop`, plus the local branding/build repairs described below.
This is a source/configuration review, not proof of behavior of the deployed image. The operator
provided Helm output showing dashboard revision 13 deployed. No cluster queries or tests were run
for this review.

The dashboard is useful as a read-only launcher and infrastructure summary, but it is not yet
aligned with the current multi-tenant platform contract.

1. **Tenant membership is still a browser check on the old top-level claim.**
   [runtime.ts](operations-dashboard/webapp/src/main/webapp/src/runtime.ts) compares
   `claims.tenant_did` with the host's configured DID, requests no tenant-specific organization
   scope, and does not inspect organization membership. This cannot represent a user belonging
   to multiple tenants correctly when the user's scalar attribute names only one tenant.
   [PlatformDashboardResource](operations-api-jaxrs/src/main/java/com/forwardmeasure/platform/dashboard/jaxrs/PlatformDashboardResource.java)
   authorizes global `platform-viewer`/`platform-admin` roles and returns the same shared health
   view without checking the request host or organization. The browser check is not API authorization.
   This is an architectural mismatch, not evidence of exposed tenant business records: this API
   currently returns shared infrastructure status. Define the tenant launcher separately from the
   platform-operator view, then enforce the chosen access contract at the API boundary.
2. **JWT validation differs by framework.** Quarkus configures the dashboard audience. Spring
   configures an issuer and explicitly maps Keycloak realm roles, but no audience constraint.
   Micronaut configures a JWKS URL and role-based URL rules, but no explicit issuer/audience
   constraints or equivalent Keycloak realm-role mapping. Those are source gaps; wrong-audience,
   wrong-issuer and nested-role token behavior must be demonstrated with signed tokens on each
   framework before claiming parity. The current deployed chart selects Quarkus.
3. **Deployment and build selection bypass this component.**
   The platform compatibility reactor and product image planner omit this repository. Its chart
   has a fixed Quarkus image, `QUARKUS_CONFIG_LOCATIONS` and `/q/health/...` probes. Changing the
   shared framework selection changes monitored product probe URLs, but not the dashboard host.
   Do not claim the new central selection covers the dashboard's own framework yet.
4. **Tenant routing remains single-host.** The chart permits a tenant list for bootstrap but
   creates one HTTPRoute host (`gateway.tenantHost`); the FOWF health-card console URL is taken
   from `tenants[0]`. The application links `/owf/studio/` and `/ei/studio/` are correctly relative
   to the current host, but the card link and route are not sufficient for multiple tenants.
5. **The health inventory is incomplete.** It contains FOWF definition management, the selected
   engine, Keycloak, Kafka, Apicurio, OpenSearch, Superset and NER. It omits FOWF execution/human-task
   services and FEI/FDS/FDE services/workers. Enabling FDE does not add it to this list. The engine
   name, namespace and framework probe path already follow shared values, which should be retained.
6. **UP has a limited meaning.** TCP checks establish socket connectivity, not application
   readiness; HTTP checks accept any 2xx and discard the body. The UI must retain that distinction
   when richer readiness is added. Do not present this dashboard as end-to-end workflow or decision
   evaluation acceptance. The configured timeouts and shared in-flight refresh are useful existing
   behavior, not grounds for a rewrite.
7. **Build and documentation had drifted.** The root still referenced platform parent 1.0.0,
   causing compilation to stop during model resolution against the current 1.1.0 checkout.
   All three binding POMs published the same unsuffixed image name, while the deployed inventory
   consumes a Quarkus-suffixed name. These two defects are fixed locally: the parent is 1.1.0 and
   image names now end in `-quarkus`, `-spring` and `-micronaut`. The product version remains 1.0.0.
   The unused theme module and omitted build orchestration still need an explicit retirement/
   integration decision; neither was silently changed by this review.
8. **Integration evidence is missing.** Existing tests cover hostname/path helpers and a Quarkus
   API boot path; the latter bypasses real OIDC discovery with test security. They do not prove
   real Keycloak membership, audience enforcement, Spring/Micronaut behavior, multi-tenant routes,
   or product readiness. Source comments describing past manual tests are not current execution
   evidence.

## Local branding implementation and verification

All three bindings now accept `forwardmeasure.platform.display-name` (default `kriyagentic`) and
publish `platformDisplayName` in the uncached runtime `config.js`. The SPA uses it in its brand,
page title and startup/error screens. The platform chart receives it from the existing shared
selection. Tenant DID, OIDC client, URLs, Java packages and artifact identities are unchanged.
Regression sources cover the default and custom JSON-escaped brand, frontend fallback, and
unchanged endpoint configuration. The frontend Maven test execution now honors `skipTests`.

Full reactor production and test-source compilation, including all three framework bindings and
frontend compilation, passed using:

```bash
/home/pn/Documents/code/forwardmeasure/forwardmeasure-openworkflow/scripts/build-bounded.sh \
  -f /home/pn/Documents/code/forwardmeasure/forwardmeasure-platform-operations/pom.xml \
  -B -ntp -Dmaven.build.cache.enabled=false -DskipTests -DskipITs -Dmaven.test.skip=false test-compile
```

Log: `/tmp/platform-dashboard-branding-compile-2026-10-06.log`. Dashboard Helm chart lint and
synthetic rendering also passed. Tests were not executed. No image was built/pushed and no
live release was changed. The current deployment needs only the
`forwardmeasure/forwardmeasure-platform-operations-quarkus:1.0.0` dashboard image rebuilt for
these branding changes; the unresolved correctness items above are not fixed by rebuilding it.

Recommended repair order: agree/enforce the operator-versus-tenant authorization boundary; align
real token validation on all frameworks; wire tenant routes and selected product inventory; bring
this repository into build/selection and cache policy; then exercise two-tenant and all-framework
integration fixtures. Keep the existing shared application structure.
