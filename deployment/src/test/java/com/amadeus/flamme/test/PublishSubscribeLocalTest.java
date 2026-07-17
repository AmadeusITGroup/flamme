package com.amadeus.flamme.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.amadeus.flamme.runtime.utils.Strings;
import com.amadeus.flamme.test.fixtures.Components;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
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

public class PublishSubscribeLocalTest {
  static int NATS_PORT = 4222;

  @SuppressWarnings("resource")
  static NatsContainer container = new NatsContainer("nats:2.9").withExposedPorts(NATS_PORT);

  @Inject Components.Gateway gateway;
  @Inject Components.ToUpperProbe probe;

  static {
    container.start();
  }

  @AfterAll
  static void stop() {
    container.stop();
  }

  @RegisterExtension
  static final QuarkusUnitTest unitTest =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClass(Components.Gateway.class)
                      .addClass(Components.ToUpperComponent.class)
                      .addClass(Components.ToUpperComponentImpl.class)
                      .addClass(Components.LeafComponent.class)
                      .addClass(Components.ToUpperProbe.class)
                      .addClass(Components.LeafComponentImpl.class))
          .overrideConfigKey(
              "flamme.nats.url",
              String.format("nats://localhost:%s", container.getMappedPort(NATS_PORT)));

  @Test
  void gatewayShouldReceiveResultsFromLeafNode() {
    Map<String, Message> payload = Map.of("VALUE", StringValue.of("Hello"));
    CompletableFuture<Map<String, Message>> future = gateway.call(payload);
    try {
      assertEquals(StringValue.of("Hello"), future.get(5, TimeUnit.SECONDS).get("VALUE"));
    } catch (TimeoutException | ExecutionException | InterruptedException exception) {
      fail(Strings.FUTURE_DID_NOT_COMPLETE);
    }
  }

  @Test
  void nonLeafSubscriberShouldReceiveAndProcessMessage() {
    Map<String, Message> payload = Map.of("VALUE", StringValue.of("Hello"));
    Map<String, Message> expectedOutput = Map.of("VALUE", StringValue.of("HELLO"));
    gateway.call(payload);
    try {
      assertTrue(probe.called.await(2, TimeUnit.SECONDS), "ToUpper was not invoked");
      assertEquals(payload, probe.lastInput.get());
      assertEquals(expectedOutput, probe.lastOutput.get());
    } catch (InterruptedException e) {
      fail(Strings.THREAD_WAS_INTERRUPTED);
    }
  }
}
