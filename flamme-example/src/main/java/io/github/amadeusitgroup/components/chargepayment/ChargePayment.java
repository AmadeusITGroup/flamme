package io.github.amadeusitgroup.components.chargepayment;

import com.google.protobuf.Any;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;

@Flamme(
    serviceName = "charge-payment",
    consumes = {"order-validated"},
    produces = {})
public interface ChargePayment {
  Any chargePayment(Any args) throws InterruptedException;
}
