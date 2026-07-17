package com.amadeus.components.validateorder;

import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import com.amadeus.order.Order;
import com.amadeus.payment.PaymentProtos.Payment;
import com.amadeus.utils.Keys;
import com.google.protobuf.Message;
import java.util.Map;

@Flamme(
    serviceName = "validate-order",
    consumes = {"order-received"},
    produces = {"order-validated"},
    multiPayloadKeys = {
      @MultiPayloadKey(id = Keys.ORDER, type = Order.class),
      @MultiPayloadKey(id = Keys.PAYMENT, type = Payment.class),
    })
public interface ValidateOrder {
  Map<String, Message> validateOrder(Map<String, Message> args);
}
