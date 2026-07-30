package io.github.amadeusitgroup.components.chargepayment;

import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme;
import io.github.amadeusitgroup.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import io.github.amadeusitgroup.order.Order;
import io.github.amadeusitgroup.payment.PaymentProtos.Payment;
import io.github.amadeusitgroup.utils.Keys;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import java.util.Map;

@Flamme(
    serviceName = "charge-payment",
    consumes = {"order-validated"},
    produces = {},
    multiPayloadKeys = {
      @MultiPayloadKey(id = Keys.ORDER, type = Order.class),
      @MultiPayloadKey(id = Keys.PAYMENT, type = Payment.class),
      @MultiPayloadKey(id = Keys.ORDER_ID, type = StringValue.class)
    })
public interface ChargePayment {
  Map<String, Message> chargePayment(Map<String, Message> args) throws InterruptedException;
}
