package com.amadeus.components.validateorder;

import com.amadeus.flamme.runtime.annotations.FlammeImpl;
import com.amadeus.order.Order;
import com.amadeus.utils.Keys;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;

@Unremovable
@FlammeImpl
@ApplicationScoped
public class ValidateOrderImpl implements ValidateOrder {
  @Override
  public Map<String, Message> validateOrder(Map<String, Message> args) {
    Order order = (Order) args.get(Keys.ORDER);
    String id = order.getId();
    // We consider the order validated if it is not empty
    if (order.getItemsList().isEmpty()) {
      throw new RuntimeException("order is empty!");
    }

    System.out.println("[ValidateOrder] order with id " + id + " was validated.");

    // add the order id to the multipayload map.
    args.put(Keys.ORDER_ID, StringValue.of(order.getId()));

    // Forward the order
    return args;
  }
}
