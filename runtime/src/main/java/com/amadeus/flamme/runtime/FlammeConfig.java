package com.amadeus.flamme.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Map;

/**
 * Runtime configuration for the Flamme EDA extension.
 *
 * <p>All properties are prefixed with {@code flamme} in the application configuration file.
 *
 * <p>Example:
 *
 * <pre>{@code
 * flamme.thread-pool-size=32
 * flamme.services.order-service.remote=true
 * }</pre>
 */
@ConfigMapping(prefix = "flamme")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface FlammeConfig {

  /**
   * Per-service configuration, keyed by service name as declared in
   * {@code @Flamme(serviceName=...)}.
   */
  Map<String, ServiceConfig> services();

  /** Time the gateway waits for a reply before timing out. */
  @WithDefault("30")
  Integer replyTimeout();

  /** NATS transport configuration. */
  NatsConfig nats();

  interface NatsConfig {
    /** NATS server URL used by the transport client. */
    @WithDefault("nats://localhost:4222")
    String url();

    /** Connection name shown in NATS server monitoring and logs. */
    @WithDefault("flamme")
    String connectionName();
  }

  /** Configuration for an individual {@code @Flamme}-annotated service. */
  interface ServiceConfig {

    /**
     * Whether this service communicates with a remote NATS server. When {@code false}, the service
     * uses the local in-process broker. Defaults to {@code false}.
     */
    @WithDefault("false")
    Boolean remote();
  }
}
