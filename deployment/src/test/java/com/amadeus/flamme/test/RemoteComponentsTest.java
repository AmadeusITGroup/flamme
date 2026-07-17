package com.amadeus.flamme.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.amadeus.flamme.runtime.errors.FlammeImplRuntimeError;
import com.amadeus.flamme.runtime.payload.MultiPayloadCodec;
import com.amadeus.flamme.runtime.payload.PayloadCodec;
import com.amadeus.flamme.runtime.transport.NatsClientManager;
import com.amadeus.flamme.runtime.utils.Strings;
import com.amadeus.flamme.test.fixtures.Components;
import com.amadeus.flamme.test.fixtures.Components.Gateway;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.nats.client.Dispatcher;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class RemoteComponentsTest {

  private static final PayloadCodec payloadCodec = new MultiPayloadCodec();
  static int NATS_PORT = 4222;

  @SuppressWarnings("resource")
  static NatsContainer container = new NatsContainer("nats:2.9").withExposedPorts(NATS_PORT);

  @Inject NatsClientManager natsClientManager;

  static {
    container.start();
  }

  @AfterAll
  static void stop() {
    container.stop();
  }

  @Inject Gateway gateway;

  @RegisterExtension
  static final QuarkusUnitTest unitTest =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClass(Components.LeafComponent.class)
                      .addClass(Components.LeafComponentImpl.class)
                      .addClass(Components.ToUpperComponentImpl.class)
                      .addClass(Components.ToUpperComponent.class)
                      .addClass(Components.ToUpperProbe.class)
                      .addClass(Components.Gateway.class))
          .overrideConfigKey(
              "flamme.nats.url",
              String.format("nats://localhost:%s", container.getMappedPort(NATS_PORT)))
          .overrideConfigKey("flamme.services.to-upper.remote", "true");

  @Test
  void shouldReplyToNatsInboxWhenLeafNode() {
    Dispatcher dispatcher = natsClientManager.getConnection().createDispatcher();
    CompletableFuture<StringValue> resultFuture = new CompletableFuture<>();
    Map<String, Message> payload = Map.of("VALUE", StringValue.of("Hello"));
    dispatcher.subscribe(
        "INBOX.>",
        (message) -> {
          try {
            StringValue result =
                (StringValue)
                    payloadCodec
                        .decodePayload(
                            message.getData(), Map.of("VALUE", StringValue.class.getName()))
                        .get("VALUE");
            resultFuture.complete(result);
          } catch (FlammeImplRuntimeError e) {
            fail(Strings.FAILED_TO_READ_PAYLOAD);
          }
        });
    natsClientManager.getConnection().publish("fan-out-event", "INBOX.test-reply", encode(payload));
    try {
      StringValue result = resultFuture.get(5, TimeUnit.SECONDS);
      assertEquals(StringValue.of("Hello"), result);
    } catch (TimeoutException | ExecutionException | InterruptedException e) {
      fail(Strings.FUTURE_DID_NOT_COMPLETE, e);
    }
  }

  @Test
  void shouldPublishToNatsWhenSubscriberIsRemote() {
    Dispatcher dispatcher = natsClientManager.getConnection().createDispatcher();
    CompletableFuture<StringValue> resultFuture = new CompletableFuture<>();
    Map<String, Message> payload = Map.of("VALUE", StringValue.of("Hello"));
    dispatcher.subscribe(
        "fan-out-event",
        (message) -> {
          try {
            StringValue result =
                (StringValue)
                    payloadCodec
                        .decodePayload(
                            message.getData(), Map.of("VALUE", StringValue.class.getName()))
                        .get("VALUE");
            resultFuture.complete(result);
          } catch (FlammeImplRuntimeError e) {
            fail(Strings.FAILED_TO_READ_PAYLOAD);
          }
        });
    gateway.call(payload);
    try {
      StringValue result = resultFuture.get(5, TimeUnit.SECONDS);
      assertEquals(StringValue.of("Hello"), result);
    } catch (TimeoutException | ExecutionException | InterruptedException e) {
      fail(Strings.FUTURE_DID_NOT_COMPLETE, e);
    }
  }

  private static byte[] encode(Map<String, Message> payload) {
    try {
      return payloadCodec.encodePayload(payload);
    } catch (FlammeImplRuntimeError e) {
      fail(Strings.FAILED_TO_READ_PAYLOAD);
      return new byte[0];
    }
  }
}
