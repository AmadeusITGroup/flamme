package io.github.amadeusitgroup.flamme.runtime.transport;

public interface TransportClient {
  boolean isAvailable();

  TransportDispatcher dispatcher();

  void publish(TransportMessage message);
}
