package com.amadeus.components;

import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import com.amadeus.order.Order;
import com.amadeus.payment.PaymentProtos.Payment;
import com.amadeus.utils.Keys;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Flamme(
    serviceName = "gateway",
    consumes = {},
    produces = {"order-received"},
    multiPayloadKeys = {
      @MultiPayloadKey(id = Keys.ORDER, type = Order.class),
      @MultiPayloadKey(id = Keys.PAYMENT, type = Payment.class),
      @MultiPayloadKey(id = Keys.ORDER_ID, type = StringValue.class),
      @MultiPayloadKey(id = Keys.RECEIPT_ID, type = StringValue.class)
    })
public interface Gateway {
  CompletableFuture<Map<String, Message>> execute(Map<String, Message> arguments);
}
