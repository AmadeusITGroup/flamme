package com.amadeus.flamme.runtime.transport;

import com.amadeus.flamme.runtime.utils.FlammeUtils;
import io.nats.client.Dispatcher;
import io.nats.client.impl.NatsMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class NatsTransportClient implements TransportClient {

  @Inject NatsClientManager clientManager;

  private Dispatcher dispatcher;

  @Override
  public synchronized TransportDispatcher dispatcher() {
    if (dispatcher == null && isAvailable()) {
      dispatcher = clientManager.getConnection().createDispatcher();
    }
    Dispatcher d = dispatcher;
    return (subject, handler) ->
        d.subscribe(
            subject,
            message ->
                handler.accept(
                    new TransportMessage(
                        message.getSubject(),
                        FlammeUtils.getMapFromHeaders(message.getHeaders()),
                        message.getData(),
                        message.getReplyTo())));
  }

  @Override
  public boolean isAvailable() {
    return clientManager != null && clientManager.getConnection() != null;
  }

  @Override
  public void publish(TransportMessage message) {
    Map<String, String> headers = message.headers();
    clientManager
        .getConnection()
        .publish(
            NatsMessage.builder()
                .subject(message.subject())
                .headers(FlammeUtils.getHeadersFromMap(headers))
                .data(message.data())
                .replyTo(message.replyTo())
                .build());
  }
}
