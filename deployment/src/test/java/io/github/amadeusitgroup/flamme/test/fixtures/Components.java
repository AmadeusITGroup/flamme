package io.github.amadeusitgroup.flamme.test.fixtures;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;
import io.github.amadeusitgroup.flamme.runtime.annotations.FlammeImpl;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Components {
  @Flamme(
      serviceName = "gateway",
      produces = {"fan-out-event"})
  public interface Gateway {
    CompletableFuture<Any> call(Any payload);
  }

  @Flamme(
      serviceName = "to-upper",
      consumes = {"fan-out-event"},
      produces = {"upper-case-event"})
  public interface ToUpperComponent {
    Any toUpper(Any input) throws InvalidProtocolBufferException;
  }

  @Flamme(
      serviceName = "leaf",
      consumes = {"fan-out-event"})
  public interface LeafComponent {
    Any backToCaller(Any payload);
  }

  @FlammeImpl
  @Unremovable
  @ApplicationScoped
  public static class ToUpperComponentImpl implements ToUpperComponent {
    @Inject ToUpperProbe probe;

    public Any toUpper(Any input) throws InvalidProtocolBufferException {
      probe.lastInput.set(input);
      StringValue stringValue = (StringValue) input.unpack(StringValue.class);
      Any result = Any.pack(StringValue.of(stringValue.getValue().toUpperCase()));
      probe.lastOutput.set(result);
      probe.called.countDown();
      return result;
    }
  }

  @FlammeImpl
  @Unremovable
  @ApplicationScoped
  public static class LeafComponentImpl implements LeafComponent {
    public Any backToCaller(Any payload) {
      return payload;
    }
  }

  @ApplicationScoped
  public static class ToUpperProbe {
    public final CountDownLatch called = new CountDownLatch(1);
    public final AtomicReference<Any> lastInput = new AtomicReference<>();
    public final AtomicReference<Any> lastOutput = new AtomicReference<>();
  }

  @Flamme(
      serviceName = "failing-component",
      consumes = {"fan-out-event"},
      produces = {"produces"})
  public interface FailingComponent {
    Any fail(Any args);
  }

  @FlammeImpl
  @Unremovable
  @ApplicationScoped
  public static class FailingComponentImpl implements FailingComponent {
    @Override
    public Any fail(Any args) {
      throw new RuntimeException("failing component failed!");
    }
  }
}
