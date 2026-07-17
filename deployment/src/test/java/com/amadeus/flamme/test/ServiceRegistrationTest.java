package com.amadeus.flamme.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amadeus.flamme.runtime.Broker;
import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.github.amadeusitgroup.testcontainers.nats.NatsContainer;
import io.quarkus.test.QuarkusUnitTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ServiceRegistrationTest {

  static int NATS_PORT = 4222;
  static final String SERVICE_NAME = "flamme-service";
  static final String CONSUMER_SUBJECT = "subject";

  @SuppressWarnings("resource")
  static NatsContainer container = new NatsContainer("nats:2.9").withExposedPorts(NATS_PORT);

  static {
    container.start();
  }

  @AfterAll
  static void stop() {
    container.stop();
  }

  @Flamme(
      serviceName = SERVICE_NAME,
      produces = {},
      consumes = {CONSUMER_SUBJECT})
  interface FlammeComponent {
    Map<String, Message> execute(Map<String, Message> multipayload);
  }

  @FlammeImpl
  class FlammeComponentImpl implements FlammeComponent {
    public Map<String, Message> execute(Map<String, Message> multipayload) {
      return Map.of("key", StringValue.of("value"));
    }
  }

  @RegisterExtension
  static final QuarkusUnitTest unitTest =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () ->
                  ShrinkWrap.create(JavaArchive.class)
                      .addClass(FlammeComponent.class)
                      .addClass(FlammeComponentImpl.class))
          .overrideConfigKey(
              "flamme.nats.url",
              String.format("nats://localhost:%s", container.getMappedPort(NATS_PORT)));

  @Inject Broker broker;

  @Test
  void shouldRegisterLocalServices() {
    assertTrue(broker.getLocalSubscriptions().containsKey(CONSUMER_SUBJECT));
    assertEquals(1, broker.getLocalSubscriptions().get(CONSUMER_SUBJECT).size());
  }
}
