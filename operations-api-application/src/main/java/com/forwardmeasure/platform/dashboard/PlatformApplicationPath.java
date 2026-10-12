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
package com.forwardmeasure.platform.dashboard;

import java.net.URI;

/** A validated same-origin absolute path exposed to the browser. */
public record PlatformApplicationPath(String value) {

  public static PlatformApplicationPath parse(String raw) {
    if (raw == null || raw.isBlank()) {
      throw invalid(raw);
    }
    if (!raw.startsWith("/")
        || raw.startsWith("//")
        || raw.indexOf('\\') >= 0
        || raw.chars().anyMatch(Character::isWhitespace)) {
      throw invalid(raw);
    }

    final URI uri;
    try {
      uri = URI.create(raw);
    } catch (IllegalArgumentException invalidUri) {
      throw new IllegalArgumentException(
          "Platform application href must be a same-origin absolute path: " + raw, invalidUri);
    }
    if (uri.isAbsolute()
        || uri.getRawAuthority() != null
        || uri.getRawFragment() != null
        || uri.getRawQuery() != null
        || uri.getRawPath() == null
        || !uri.getRawPath().startsWith("/")
        || !uri.normalize().equals(uri)) {
      throw invalid(raw);
    }
    return new PlatformApplicationPath(uri.toASCIIString());
  }

  private static IllegalArgumentException invalid(String raw) {
    return new IllegalArgumentException(
        "Platform application href must be a same-origin absolute path: " + raw);
  }
}
