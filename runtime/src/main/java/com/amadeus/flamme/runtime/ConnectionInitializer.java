package com.amadeus.flamme.runtime;

import com.amadeus.flamme.runtime.errors.NatsConnectionError;
import com.amadeus.flamme.runtime.transport.NatsClientManager;
import com.amadeus.flamme.runtime.utils.Strings;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ConnectionInitializer {

  private static final Logger log = LoggerFactory.getLogger(ConnectionInitializer.class);

  @Inject private NatsClientManager natsClientManager;

  @Inject private FlammeConfig config;

  void onStart(@Observes @Priority(1) StartupEvent startupEvent) throws NatsConnectionError {
    try {
      Options options =
          new Options.Builder()
              .server(config.nats().url())
              .connectionName(config.nats().connectionName())
              .build();
      Connection connection = Nats.connect(options);
      natsClientManager.setConnection(connection);
    } catch (Throwable t) {
      log.atError().log(Strings.NATS_CONNECTION_ERROR);
      throw new NatsConnectionError(Strings.NATS_CONNECTION_ERROR, t);
    }
  }
}
