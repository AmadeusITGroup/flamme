package io.github.amadeusitgroup.components.chargepayment;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.amadeusitgroup.context.OrderProcessingContext;
import io.github.amadeusitgroup.flamme.runtime.annotations.FlammeImpl;
import io.github.amadeusitgroup.order.Order;
import io.github.amadeusitgroup.payment.Payment;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@FlammeImpl
@Unremovable
@ApplicationScoped
public class ChargePaymentImpl implements ChargePayment {
  @Override
  public Any chargePayment(Any args) throws InterruptedException {
    OrderProcessingContext context;
    try {
      context = args.unpack(OrderProcessingContext.class);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException("failed to unpack order processing context", e);
    }

    Order order = context.getOrder();
    Payment payment = context.getPayment();

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

    OrderProcessingContext updatedContext = context.toBuilder().setReceiptId(receiptId).build();
    return Any.pack(updatedContext);
  }
}
