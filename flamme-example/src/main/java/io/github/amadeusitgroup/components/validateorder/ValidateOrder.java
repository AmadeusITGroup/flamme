package io.github.amadeusitgroup.components.validateorder;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;

@Flamme(
    serviceName = "validate-order",
    consumes = {"order-received"},
    produces = {"order-validated"})
public interface ValidateOrder {
  Any validateOrder(Any args);
}
