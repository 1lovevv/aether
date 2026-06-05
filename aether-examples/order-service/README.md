# Aether Framework - Advanced Examples

This directory contains advanced examples demonstrating various features of the Aether framework.

## Order Service Example

The Order Service demonstrates:
- **Actor Definition**: Using `@ActorBean` and `@OnMessage`
- **Message Types**: Commands, queries, and events
- **State Management**: In-memory state within actors
- **Spring Integration**: Dependency injection in actors

### Running the Example

```bash
cd aether-examples/order-service
mvn spring-boot:run
```

### Message Types

```java
// Command to create an order
public record CreateOrderMsg(UUID customerId, List<String> items, BigDecimal totalAmount) implements Message {}

// Command to cancel an order
public record CancelOrderMsg(UUID orderId) implements Message {}

// Query to get order details
public record QueryOrderMsg(UUID orderId) implements Message {}
```

### Actor Implementation

```java
@ActorBean(name = "order-processor")
public class OrderProcessor {

    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // Process order creation
    }

    @OnMessage
    public void onCancelOrder(CancelOrderMsg msg) {
        // Process order cancellation
    }
}
```

## Features Demonstrated

### 1. Virtual Thread Actor

Each actor runs on its own virtual thread, allowing millions of concurrent actors:

```java
@ActorBean
public class MyActor {
    @OnMessage
    public void onMessage(MyMessage msg) {
        // This runs on a virtual thread
        // Blocking operations are fine!
        Thread.sleep(1000);
        database.save(msg);
    }
}
```

### 2. Supervision Tree

Actors are organized in a supervision tree for fault tolerance:

```java
@ActorBean
public class OrderSupervisor extends SupervisorActor {
    @Override
    protected void initChildren() {
        supervise(OrderProcessor.class, "order-processor");
        supervise(PaymentProcessor.class, "payment-processor");
    }
}
```

### 3. Ask Pattern

Request-reply pattern with timeout:

```java
@Service
public class OrderService {
    @Autowired
    private ActorRef orderProcessor;

    public Order getOrder(UUID orderId) {
        CompletableFuture<OrderResult> future =
            orderProcessor.ask(new QueryOrderMsg(orderId), Duration.ofSeconds(5));
        return future.get();
    }
}
```

### 4. Metrics

Built-in Micrometer metrics:

```java
// Actor-level metrics
aether.actor.messages.received
aether.actor.messages.processed
aether.actor.mailbox.size

// System-level metrics
aether.system.actors.total
aether.system.actors.active
aether.system.virtual_threads.pinned
```

### 5. Message Serialization

Pluggable serialization for remote actors:

```java
// Serialize to bytes
MessageSerializer serializer = new JsonMessageSerializer();
byte[] bytes = serializer.serialize(message);

// Deserialize back
MyMessage msg = serializer.deserialize(bytes, MyMessage.class);
```

## Configuration

```yaml
aether:
  virtual-threads:
    enabled: true
    pinning-monitoring: true
  actor:
    default-mailbox-capacity: 1000
  supervision:
    default-strategy: one-for-one
    max-restarts: 10
  metrics:
    enabled: true
```

## Next Steps

- Try creating your own actor
- Experiment with different supervision strategies
- Add custom metrics
- Implement a custom MessageSerializer
