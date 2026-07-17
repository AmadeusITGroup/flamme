package com.amadeus.components.chargepayment;

import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.amadeus.order.Order;
import com.amadeus.payment.PaymentProtos.Payment;
import com.amadeus.utils.Keys;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.UUID;

@FlammeImpl
@Unremovable
@ApplicationScoped
public class ChargePaymentImpl implements ChargePayment {
  @Override
  public Map<String, Message> chargePayment(Map<String, Message> args) throws InterruptedException {

    Order order = (Order) args.get(Keys.ORDER);

    Payment payment = (Payment) args.get(Keys.PAYMENT);

    String id = order.getId();
    // simulate an external call to make the payment
    // this is blocking code, but is not really blocking, since flamme uses virtual threads.
    Thread.sleep(1000);

    System.out.println(
        "[Charge Payment] processed payment for order "
            + id
            + " via "
            + payment.getPaymentMethod());

    String receiptId = UUID.randomUUID().toString();

    // add receiptId to the multipayload
    args.put(Keys.RECEIPT_ID, StringValue.of(receiptId));
    return args;
  }
}
