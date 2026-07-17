package com.amadeus.components.chargepayment;

import com.amadeus.flamme.runtime.annotations.Flamme;
import com.amadeus.flamme.runtime.annotations.Flamme.MultiPayloadKey;
import com.amadeus.order.Order;
import com.amadeus.payment.PaymentProtos.Payment;
import com.amadeus.utils.Keys;
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
