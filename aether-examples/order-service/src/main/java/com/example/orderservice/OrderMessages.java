package com.example.orderservice;

import com.aether.message.Message;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Messages for the Order Service example.
 */
public class OrderMessages {

    private OrderMessages() {
        // Utility class
    }

    /**
     * Command to create a new order.
     */
    public record CreateOrderMsg(
            UUID customerId,
            List<String> items,
            BigDecimal totalAmount
    ) implements Message {}

    /**
     * Command to cancel an existing order.
     */
    public record CancelOrderMsg(
            UUID orderId
    ) implements Message {}

    /**
     * Query to retrieve order details.
     */
    public record QueryOrderMsg(
            UUID orderId
    ) implements Message {}

    /**
     * Event fired when an order is created.
     */
    public record OrderCreatedEvent(
            UUID orderId,
            UUID customerId,
            BigDecimal totalAmount
    ) implements Message {}

    /**
     * Event fired when an order is cancelled.
     */
    public record OrderCancelledEvent(
            UUID orderId,
            String reason
    ) implements Message {}
}
