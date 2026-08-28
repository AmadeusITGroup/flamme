package io.github.amadeusitgroup;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.amadeusitgroup.components.Gateway;
import io.github.amadeusitgroup.context.OrderProcessingContext;
import io.github.amadeusitgroup.order.Order;
import io.github.amadeusitgroup.order.OrderItem;
import io.github.amadeusitgroup.payment.Payment;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
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

    OrderProcessingContext context =
        OrderProcessingContext.newBuilder().setOrder(order).setPayment(payment).build();

    return gateway
        .execute(Any.pack(context))
        .thenApply(
            payload -> {
              try {
                OrderProcessingContext result = payload.unpack(OrderProcessingContext.class);
                return new CreateOrderResponse(result.getOrderId(), result.getReceiptId());
              } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("failed to unpack order processing context", e);
              }
            });
  }
}
