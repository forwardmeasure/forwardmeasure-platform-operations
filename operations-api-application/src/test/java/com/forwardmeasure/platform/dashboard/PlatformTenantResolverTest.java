package com.forwardmeasure.platform.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PlatformTenantResolverTest {

  @Test
  void canonicalizesOnlyAnExactHttpHost() {
    assertEquals(
        "tenant.example.com", PlatformTenantResolver.canonicalHost("TENANT.Example.Com.:8443"));
    assertThrows(IllegalArgumentException.class, () -> PlatformTenantResolver.canonicalHost(""));
    assertThrows(
        IllegalArgumentException.class, () -> PlatformTenantResolver.canonicalHost("bad host"));
  }
}
