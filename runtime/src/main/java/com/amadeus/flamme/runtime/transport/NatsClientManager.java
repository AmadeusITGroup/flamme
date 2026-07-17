package com.amadeus.flamme.runtime.transport;

import io.nats.client.Connection;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NatsClientManager {
  private volatile Connection connection;

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  @PreDestroy
  void onDestroy() {
    if (connection != null) {
      try {
        connection.close();
      } catch (Exception ignored) {
      }
    }
  }
}
