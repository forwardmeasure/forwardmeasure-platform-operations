export function platformDisplayName(): string {
  return window.__FORWARDMEASURE_PLATFORM_CONFIG__?.platformDisplayName?.trim() || "kriyagentic";
}

export function dashboardTitle(): string {
  return `${platformDisplayName()} · Platform Dashboard`;
}
