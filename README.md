# ForwardMeasure Platform Operations

Platform Operations owns the shared operational API, operator dashboard and
Keycloak login experience. It is Apache License 2.0 software and contains no
Entity Intelligence domain implementation.

The source layout follows the capability boundaries used by ForwardMeasure
OpenWorkflow:

- `operations-api-specifications` contains handwritten API specifications only.
- `operations-api-language-bindings` owns generated language bindings.
- `operations-api-application` is reserved for framework-neutral application logic.
- `operations-api-jaxrs` is reserved for the portable JAX-RS boundary.
- `operations-dashboard` owns the packaged web application.
- `operations-framework-bindings` owns Quarkus, Spring and Micronaut bindings.
- `operations-keycloak-theme` builds the shared Keycloak theme image.

The existing Platform Dashboard implementation has been preserved under the
Quarkus binding. Its application/JAX-RS/framework layering requires a dedicated
remediation pass; it has deliberately not been misrepresented as portable code.
