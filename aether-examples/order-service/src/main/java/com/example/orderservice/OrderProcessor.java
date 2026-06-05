package com.example.orderservice;

import com.aether.spring.annotation.ActorBean;
import com.aether.spring.annotation.OnMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced Order Processor demonstrating:
 * <ul>
 *   <li>State management within an actor</li>
 *   <li>Processing different message types</li>
 *   <li>Business logic with Spring dependency injection</li>
 * </ul>
 */
@ActorBean(name = "order-processor")
public class OrderProcessor {

    // In-memory order store (in production, use a database)
    private final ConcurrentHashMap<UUID, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong orderCounter = new AtomicLong(0);

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Handles order creation requests.
     */
    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        System.out.println("[OrderProcessor] Creating order for customer: " + msg.customerId());

        Order order = new Order(
                UUID.randomUUID(),
                msg.customerId(),
                msg.items(),
                msg.totalAmount(),
                OrderStatus.CREATED
        );

        // Store in memory
        orders.put(order.id(), order);
        orderCounter.incrementAndGet();

        // Persist to database (simulated)
        if (orderRepository != null) {
            orderRepository.save(order);
        }

        System.out.println("[OrderProcessor] Order created: " + order.id() + ", Total orders: " + orderCounter.get());
    }

    /**
     * Handles order cancellation requests.
     */
    @OnMessage
    public void onCancelOrder(CancelOrderMsg msg) {
        System.out.println("[OrderProcessor] Cancelling order: " + msg.orderId());

        Order order = orders.get(msg.orderId());
        if (order != null) {
            order = new Order(order.id(), order.customerId(), order.items(), order.totalAmount(), OrderStatus.CANCELLED);
            orders.put(order.id(), order);
            System.out.println("[OrderProcessor] Order cancelled: " + order.id());
        } else {
            System.out.println("[OrderProcessor] Order not found: " + msg.orderId());
        }
    }

    /**
     * Handles order queries.
     */
    @OnMessage
    public void onQueryOrder(QueryOrderMsg msg) {
        System.out.println("[OrderProcessor] Querying order: " + msg.orderId());

        Order order = orders.get(msg.orderId());
        if (order != null) {
            System.out.println("[OrderProcessor] Order found: " + order);
        } else {
            System.out.println("[OrderProcessor] Order not found: " + msg.orderId());
        }
    }

    public long getOrderCount() {
        return orderCounter.get();
    }

    public Order getOrder(UUID orderId) {
        return orders.get(orderId);
    }

    // Order status enum
    public enum OrderStatus {
        CREATED, PROCESSING, SHIPPED, DELIVERED, CANCELLED
    }

    // Message records
    public record CreateOrderMsg(UUID customerId, List<String> items, BigDecimal totalAmount) {}
    public record CancelOrderMsg(UUID orderId) {}
    public record QueryOrderMsg(UUID orderId) {}

    // Order record
    public record Order(
            UUID id,
            UUID customerId,
            java.util.List<String> items,
            BigDecimal totalAmount,
            OrderStatus status
    ) {}

    // Simulated repository
    public interface OrderRepository {
        void save(Order order);
    }
}
