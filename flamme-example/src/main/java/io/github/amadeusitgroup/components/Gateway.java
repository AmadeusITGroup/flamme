package io.github.amadeusitgroup.components;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;
import java.util.concurrent.CompletableFuture;

@Flamme(
    serviceName = "gateway",
    consumes = {},
    produces = {"order-received"})
public interface Gateway {
  CompletableFuture<Any> execute(Any arguments);
}
