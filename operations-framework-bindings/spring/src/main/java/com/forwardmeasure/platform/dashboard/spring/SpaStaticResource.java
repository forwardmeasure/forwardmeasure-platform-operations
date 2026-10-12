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
package com.forwardmeasure.platform.dashboard.spring;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Map;

/**
 * Serves the bundled React SPA's static assets (operations-dashboard-webapp, packaged under
 * classpath {@code META-INF/resources}) - Spring-specific, not shared: Quarkus and Micronaut both
 * serve {@code META-INF/resources} natively (Vert.x's static-resource handling for Quarkus,
 * micronaut-http-server-netty's classpath-resource detection for Micronaut), but Jersey, when
 * registered as the owning servlet for the whole {@code /platform/*} prefix (this binding's {@code
 * spring.jersey.application-path}), has no equivalent - Spring's own static-resource serving needs
 * Spring MVC's DispatcherServlet, which isn't in play here. Confirmed the hard way: without this,
 * every static asset request 404'd.
 *
 * <p>Registered as a JAX-RS resource, not a separate servlet mapping, deliberately: JAX-RS resolves
 * the most specific matching {@code @Path} first, so the more specific resources ({@code
 * /api/v1/components}, {@code /config.js}, {@code /tenant-context}) always win over this wildcard -
 * no path-prefix conflict to resolve at the servlet-container level.
 */
@Path("{path: .*}")
public class SpaStaticResource {

  private static final String RESOURCE_ROOT = "META-INF/resources/";

  private static final Map<String, String> CONTENT_TYPES =
      Map.of(
          "html", "text/html",
          "js", "application/javascript",
          "css", "text/css",
          "map", "application/json",
          "woff2", "font/woff2");

  @GET
  public Response get(@PathParam("path") String path) {
    String requested = (path == null || path.isBlank() || path.equals("/")) ? "index.html" : path;
    if (requested.contains("..")) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    InputStream resource =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(RESOURCE_ROOT + requested);
    if (resource == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    String extension =
        requested.contains(".") ? requested.substring(requested.lastIndexOf('.') + 1) : "";
    String contentType = CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");

    CacheControl cache = new CacheControl();
    cache.setNoStore(true);
    return Response.ok(resource, contentType).cacheControl(cache).build();
  }
}
