package com.amadeus;

import com.amadeus.components.Gateway;
import com.amadeus.order.Order;
import com.amadeus.order.OrderItem;
import com.amadeus.payment.PaymentProtos.Payment;
import com.amadeus.utils.Keys;
import com.google.protobuf.StringValue;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Path("/order")
public class OrderResource {

  public static class CreateOrderRequest {
    public String customerEmail;
    public double totalAmount;
    public List<OrderItemRequest> orderItems;
    public PaymentRequest payment;
  }

  public record OrderItemRequest(String sku, int quantity) {}

  public record PaymentRequest(String paymentMethod) {}

  public record CreateOrderResponse(String orderId, String receiptId) {}

  @Inject Gateway gateway;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletableFuture<CreateOrderResponse> order(CreateOrderRequest request) {
    String id = UUID.randomUUID().toString();
    List<OrderItem> items =
        request.orderItems == null
            ? List.of()
            : request.orderItems.stream()
                .map(
                    item ->
                        OrderItem.newBuilder()
                            .setSku(item.sku())
                            .setQuantity(item.quantity())
                            .build())
                .toList();

    Order order =
        Order.newBuilder()
            .setId(id)
            .setCustomerEmail(request.customerEmail)
            .setTotalAmount(request.totalAmount)
            .addAllItems(items)
            .build();

    Payment payment =
        Payment.newBuilder()
            .setPaymentMethod(
                request.payment == null || request.payment.paymentMethod() == null
                    ? ""
                    : request.payment.paymentMethod())
            .build();

    Map<String, com.google.protobuf.Message> payload =
        new HashMap<>(Map.of(Keys.ORDER, order, Keys.PAYMENT, payment));

    return gateway
        .execute(payload)
        .thenApply(
            multipayload -> {
              String orderId = ((StringValue) multipayload.get(Keys.ORDER_ID)).getValue();
              String receiptId = ((StringValue) multipayload.get(Keys.RECEIPT_ID)).getValue();
              return new CreateOrderResponse(orderId, receiptId);
            });
  }
}
