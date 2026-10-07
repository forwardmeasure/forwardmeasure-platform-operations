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
