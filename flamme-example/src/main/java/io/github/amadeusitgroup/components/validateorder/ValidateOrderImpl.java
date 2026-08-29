package io.github.amadeusitgroup.components.validateorder;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.amadeusitgroup.context.OrderProcessingContext;
import io.github.amadeusitgroup.flamme.runtime.annotations.FlammeImpl;
import io.github.amadeusitgroup.order.Order;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

@Unremovable
@FlammeImpl
@ApplicationScoped
public class ValidateOrderImpl implements ValidateOrder {
  @Override
  public Any validateOrder(Any args) {
    OrderProcessingContext context;
    try {
      context = args.unpack(OrderProcessingContext.class);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException("failed to unpack order processing context", e);
    }
    Order order = context.getOrder();
    String id = order.getId();
    // We consider the order validated if it is not empty
    if (order.getItemsList().isEmpty()) {
      throw new RuntimeException("order is empty!");
    }

    System.out.println("[ValidateOrder] order with id " + id + " was validated.");

    OrderProcessingContext updatedContext = context.toBuilder().setOrderId(order.getId()).build();
    return Any.pack(updatedContext);
  }
}
