package com.amadeus.flamme.runtime.transport;

import java.util.function.Consumer;

public interface TransportDispatcher {
  void subscribe(String subject, Consumer<TransportMessage> handler);
}
