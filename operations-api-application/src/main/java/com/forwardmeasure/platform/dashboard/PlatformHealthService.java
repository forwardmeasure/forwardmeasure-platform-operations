package com.forwardmeasure.platform.dashboard;

import com.forwardmeasure.platform.operations.api.model.PlatformComponent;
import com.forwardmeasure.platform.operations.api.model.PlatformComponent.CategoryEnum;
import com.forwardmeasure.platform.operations.api.model.PlatformComponent.StatusEnum;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Performs bounded, read-only probes of operator-configured service endpoints. Plain constructor
 * taking already-typed configuration - no CDI, no framework config-lookup abstraction - each
 * framework binding resolves its own config and constructs this directly, matching
 * openworkflow-execution-management's ExecutionManagementService.
 */
public final class PlatformHealthService {

  private final List<PlatformDashboardSettings.Component> components;

  private final Duration probeTimeout;

  private final Duration observationTtl;

  private final HttpClient http;

  private final ExecutorService tcpExecutor;

  private volatile Observation cached;

  private volatile CompletableFuture<List<PlatformComponent>> refresh;

  public PlatformHealthService(
      List<PlatformDashboardSettings.Component> components,
      Duration probeTimeout,
      Duration observationTtl) {
    this(
        components,
        probeTimeout,
        observationTtl,
        HttpClient.newBuilder()
            .connectTimeout(probeTimeout)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build());
  }

  PlatformHealthService(
      List<PlatformDashboardSettings.Component> components,
      Duration probeTimeout,
      Duration observationTtl,
      HttpClient http) {
    this.components = List.copyOf(Objects.requireNonNull(components, "components"));
    this.probeTimeout = Objects.requireNonNull(probeTimeout, "probeTimeout");
    this.observationTtl = Objects.requireNonNull(observationTtl, "observationTtl");
    this.http = Objects.requireNonNull(http, "http");
    this.tcpExecutor = Executors.newVirtualThreadPerTaskExecutor();
    if (probeTimeout.isZero() || probeTimeout.isNegative()) {
      throw new IllegalArgumentException("probeTimeout must be positive");
    }
    if (observationTtl.isZero() || observationTtl.isNegative()) {
      throw new IllegalArgumentException("observationTtl must be positive");
    }
    this.components.forEach(component -> validate(component.probe(), component.healthUri()));
  }

  public List<PlatformComponent> observe() {
    Observation current = cached;
    if (current != null && current.validAt(System.nanoTime())) {
      return current.components();
    }
    return refresh().join();
  }

  private synchronized CompletableFuture<List<PlatformComponent>> refresh() {
    Observation current = cached;
    if (current != null && current.validAt(System.nanoTime())) {
      return CompletableFuture.completedFuture(current.components());
    }
    if (refresh != null && !refresh.isDone()) {
      return refresh;
    }
    List<CompletableFuture<PlatformComponent>> probes =
        components.stream().map(component -> probe(component).toCompletableFuture()).toList();
    refresh =
        CompletableFuture.allOf(probes.toArray(CompletableFuture[]::new))
            .thenApply(ignored -> probes.stream().map(CompletableFuture::join).toList())
            .thenApply(
                observed -> {
                  cached = new Observation(observed, System.nanoTime() + observationTtl.toNanos());
                  return observed;
                });
    return refresh;
  }

  private java.util.concurrent.CompletionStage<PlatformComponent> probe(
      PlatformDashboardSettings.Component component) {
    if ("TCP".equalsIgnoreCase(component.probe())) {
      return CompletableFuture.supplyAsync(() -> probeTcp(component), tcpExecutor);
    }
    long started = System.nanoTime();
    HttpRequest request =
        HttpRequest.newBuilder(component.healthUri())
            .timeout(probeTimeout)
            .header("Accept", "application/json")
            .GET()
            .build();
    return http.sendAsync(request, HttpResponse.BodyHandlers.discarding())
        .handle(
            (response, failure) -> {
              long latency = Duration.ofNanos(System.nanoTime() - started).toMillis();
              if (failure != null) {
                return value(component, StatusEnum.DOWN, latency, bounded(rootMessage(failure)));
              }
              StatusEnum status =
                  response.statusCode() >= 200 && response.statusCode() < 300
                      ? StatusEnum.UP
                      : StatusEnum.DOWN;
              return value(
                  component,
                  status,
                  latency,
                  "Health endpoint returned HTTP " + response.statusCode());
            });
  }

  private PlatformComponent probeTcp(PlatformDashboardSettings.Component component) {
    long started = System.nanoTime();
    URI uri = component.healthUri();
    try (Socket socket = new Socket()) {
      socket.connect(
          new InetSocketAddress(uri.getHost(), uri.getPort()),
          Math.toIntExact(probeTimeout.toMillis()));
      long latency = Duration.ofNanos(System.nanoTime() - started).toMillis();
      return value(component, StatusEnum.UP, latency, "TCP service endpoint accepted a connection");
    } catch (Exception failure) {
      long latency = Duration.ofNanos(System.nanoTime() - started).toMillis();
      return value(component, StatusEnum.DOWN, latency, bounded(rootMessage(failure)));
    }
  }

  private static PlatformComponent value(
      PlatformDashboardSettings.Component component,
      StatusEnum status,
      long latency,
      String detail) {
    return new PlatformComponent()
        .id(component.id())
        .name(component.name())
        .category(CategoryEnum.fromValue(component.category()))
        .status(status)
        .checkedAt(new Date())
        .latencyMilliseconds(latency)
        .detail(detail)
        .consoleUrl(component.consoleUri().orElse(null));
  }

  static void validate(String probe, URI uri) {
    String scheme = uri.getScheme();
    boolean http =
        "HTTP".equalsIgnoreCase(probe)
            && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
    boolean tcp =
        "TCP".equalsIgnoreCase(probe) && "tcp".equalsIgnoreCase(scheme) && uri.getPort() > 0;
    if (!(http || tcp)
        || uri.getHost() == null
        || uri.getUserInfo() != null
        || uri.getFragment() != null) {
      throw new IllegalArgumentException(
          "Platform health URI must match its HTTP or TCP probe without user info or fragment");
    }
  }

  private static String rootMessage(Throwable failure) {
    Throwable current = failure;
    while ((current instanceof CompletionException
            || current instanceof java.util.concurrent.ExecutionException)
        && current.getCause() != null) {
      current = current.getCause();
    }
    return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
  }

  private static String bounded(String value) {
    return value.length() <= 512 ? value : value.substring(0, 509) + "...";
  }

  /**
   * Framework bindings call this at their own shutdown hook (Quarkus {@code @PreDestroy}, etc.).
   */
  public void close() {
    tcpExecutor.close();
  }

  private record Observation(List<PlatformComponent> components, long expiresAtNanos) {

    private Observation {
      components = List.copyOf(components);
    }

    private boolean validAt(long nowNanos) {
      return nowNanos - expiresAtNanos < 0;
    }
  }
}
