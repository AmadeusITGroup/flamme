package io.github.amadeusitgroup.components.validateorder;

import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import io.github.amadeusitgroup.order.Order;
import io.github.amadeusitgroup.payment.PaymentProtos.Payment;
import io.github.amadeusitgroup.utils.Keys;
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
