package com.amadeus.flamme.runtime.transport;

public interface TransportClient {
  boolean isAvailable();

  TransportDispatcher dispatcher();

  void publish(TransportMessage message);
}
