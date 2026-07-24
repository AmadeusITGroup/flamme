package com.amadeus.flamme.test.fixtures;

import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Components {
  @Flamme(
      serviceName = "gateway",
      produces = {"fan-out-event"})
  public interface Gateway {
    CompletableFuture<Map<String, Message>> call(Map<String, Message> payload);
  }

  @Flamme(
      serviceName = "to-upper",
      consumes = {"fan-out-event"},
      produces = {"upper-case-event"})
  public interface ToUpperComponent {
    Map<String, Message> toUpper(Map<String, Message> input);
  }

  @Flamme(
      serviceName = "leaf",
      multiPayloadKeys = {@MultiPayloadKey(id = "VALUE", type = StringValue.class)},
      consumes = {"fan-out-event"})
  public interface LeafComponent {
    Map<String, Message> backToCaller(Map<String, Message> payload);
  }

  @FlammeImpl
  @Unremovable
  @ApplicationScoped
  public static class ToUpperComponentImpl implements ToUpperComponent {
    @Inject ToUpperProbe probe;

    public Map<String, Message> toUpper(Map<String, Message> input) {
      probe.lastInput.set(input);
      StringValue stringValue = (StringValue) input.get("VALUE");
      Map<String, Message> result = new HashMap<>();
      result.put("VALUE", StringValue.of(stringValue.getValue().toUpperCase()));
      probe.lastOutput.set(result);
      probe.called.countDown();
      return result;
    }
  }

  @FlammeImpl
  @Unremovable
  @ApplicationScoped
  public static class LeafComponentImpl implements LeafComponent {
    public Map<String, Message> backToCaller(Map<String, Message> payload) {
      return payload;
    }
  }

  @ApplicationScoped
  public static class ToUpperProbe {
    public final CountDownLatch called = new CountDownLatch(1);
    public final AtomicReference<Map<String, Message>> lastInput = new AtomicReference<>();
    public final AtomicReference<Map<String, Message>> lastOutput = new AtomicReference<>();
  }
}
