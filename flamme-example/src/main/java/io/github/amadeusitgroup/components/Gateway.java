package io.github.amadeusitgroup.components;

import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import io.github.amadeusitgroup.order.Order;
import io.github.amadeusitgroup.payment.PaymentProtos.Payment;
import io.github.amadeusitgroup.utils.Keys;
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
