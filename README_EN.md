[中文](README.md) | **English**

# Aether

[![Maven Central](https://img.shields.io/maven-central/v/io.github.1lovevv/aether-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.1lovevv)
[![JitPack](https://jitpack.io/v/1lovevv/aether.svg)](https://jitpack.io/#1lovevv/aether)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)

> A Lightweight Actor Model Framework Powered by Java Virtual Threads

Aether is a lightweight Actor model framework designed for modern Java applications. It fully leverages the high-concurrency capabilities of Java 21+ Virtual Threads, providing an intuitive Actor programming model, a powerful supervision tree mechanism, and out-of-the-box Spring Boot integration.

## ✨ Features

- **🚀 Virtual Thread Actors** — Each Actor runs on its own virtual thread, enabling millions of concurrent actors
- **🌲 Supervision Tree** — Hierarchical supervision strategies (OneForOne / AllForOne / RestForOne) with automatic failure recovery
- **🔧 Spring Boot Integration** — `@ActorBean` and `@OnMessage` annotations, zero boilerplate code
- **📊 Micrometer Metrics** — Actor-level and system-level metrics, seamless Prometheus/Grafana integration
- **🔌 SPI Decoupling** — Zero Spring dependency in core, extensible via SPI plugin mechanism

## 📦 Installation

### Maven Central (Recommended)

```xml
<dependency>
    <groupId>io.github.1lovevv</groupId>
    <artifactId>aether-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### JitPack (Latest Dev Build)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.1lovevv</groupId>
    <artifactId>aether</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    implementation 'io.github.1lovevv:aether-spring-boot-starter:1.0.0'
}
```

## 🆚 Aether vs Akka

### Why Choose Aether?

| Feature | Aether | Akka |
|---------|--------|------|
| **Concurrency Model** | Virtual Threads (Project Loom) | Traditional Thread Pools |
| **Code Style** | Synchronous, blocking | Asynchronous, callback-based |
| **Spring Boot** | Native integration | Requires adaptation |
| **Learning Curve** | Low | High |
| **Memory/Actor** | ~1-2KB | ~300B + thread pool |
| **Actor Count** | Millions | Thousands |
| **License** | MIT (completely free) | BSL (commercial license required) |

### Virtual Threads vs Traditional Threads

**Aether — Virtual Threads (Recommended)**:

```java
@ActorBean
public class OrderProcessor {
    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // ✅ Synchronous code, zero performance loss!
        Order order = database.save(msg.toOrder());  // Blocking OK!
        Thread.sleep(1000);                            // Blocking OK!
        httpClient.post(order);                         // Blocking OK!
    }
}
```

**Akka — Traditional Thread Pools**:

```java
// ❌ Must use async patterns
public class OrderActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateOrderMsg.class, msg -> {
                // Callback hell
                database.saveAsync(msg.toOrder())
                    .thenCompose(order -> httpClient.postAsync(order))
                    .thenAccept(result -> sender().tell(result, self()));
            })
            .build();
    }
}
```

### Key Advantages

1. **🚀 Virtual Thread Native** — Blocking code without performance penalty, say goodbye to callback hell
2. **🌲 Simple Supervision** — Annotation-based configuration, no complex hierarchies
3. **📊 Spring Boot First** — Zero-config auto-discovery
4. **📦 Lightweight** — 200KB vs 5MB+
5. **💰 MIT Licensed** — Completely free, commercial-friendly

> **Full Comparison**: See [AETHER_VS_AKKA.md](AETHER_VS_AKKA.md)

## 🚀 Quick Start

### 1. Define an Actor

```java
import com.aether.spring.annotation.ActorBean;
import com.aether.spring.annotation.OnMessage;
import com.aether.spi.ActorRef;

@ActorBean(name = "order-processor")
public class OrderProcessor {

    @Autowired
    private OrderRepository orderRepository;

    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // Synchronous, blocking code running on a virtual thread
        Order order = orderRepository.save(msg.toOrder());
        System.out.println("Order created: " + order.id());
    }

    @OnMessage
    public void onCancelOrder(CancelOrderMsg msg) {
        orderRepository.cancel(msg.orderId());
    }
}
```

### 2. Send Messages

```java
@Service
public class OrderService {

    @Autowired
    @Qualifier("order-processor")
    private ActorRef orderProcessor;

    public void createOrder(CreateOrderMsg msg) {
        // Async fire-and-forget
        orderProcessor.tell(msg);
    }

    public Order getOrder(UUID orderId) {
        // Sync request-reply (5 second timeout)
        CompletableFuture<OrderResult> future =
            orderProcessor.ask(new QueryOrderMsg(orderId), Duration.ofSeconds(5));
        return future.get();
    }
}
```

### 3. Configure application.yml

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

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│              aether-spring-boot-starter                 │
│         (Spring Boot Auto-Configuration)              │
├─────────────────────────────────────────────────────────┤
│                    aether-core                          │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │   Actor     │  │  Supervisor │  │    Mailbox     │  │
│  │  (Virtual   │  │   (Super-   │  │  (Blocking     │  │
│  │   Thread)   │  │   vision    │  │   Queue)       │  │
│  └─────────────┘  │   Tree)     │  └────────────────┘  │
│                   └─────────────┘                       │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │   SPI       │  │   Metrics   │  │   Scheduler    │  │
│  │ Interface   │  │ (Micrometer)│  │ (Virtual Thrd) │  │
│  └─────────────┘  └─────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 📊 Metrics

### Actor-Level Metrics

| Metric Name | Type | Tags | Description |
|-------------|------|------|-------------|
| `aether.actor.messages.received` | Counter | `actor`, `message_type` | Total messages received |
| `aether.actor.messages.processed` | Counter | `actor`, `message_type` | Successfully processed messages |
| `aether.actor.messages.failed` | Counter | `actor`, `message_type`, `exception` | Failed message count |
| `aether.actor.mailbox.size` | Gauge | `actor` | Current mailbox backlog |
| `aether.actor.processing.time` | Timer | `actor`, `message_type` | Message processing duration |
| `aether.actor.state` | Gauge | `actor`, `state` | Current actor state |

### System-Level Metrics

| Metric Name | Type | Description |
|-------------|------|-------------|
| `aether.system.actors.total` | Gauge | Total actor count |
| `aether.system.actors.active` | Gauge | Active actor count |
| `aether.system.virtual_threads.pinned` | Gauge | Pinned virtual threads (Java 24) |
| `aether.system.throughput` | Gauge | Global message throughput (msg/s) |

### Prometheus Output Example

```
# HELP aether_actor_mailbox_size Current mailbox size
# TYPE aether_actor_mailbox_size gauge
aether_actor_mailbox_size{actor="order-processor"} 42

# HELP aether_actor_messages_processed_total Total messages processed
# TYPE aether_actor_messages_processed_total counter
aether_actor_messages_processed_total{actor="order-processor",message_type="CreateOrderMsg"} 1024
```

## 🔧 Configuration

### Full Configuration Example

```yaml
aether:
  virtual-threads:
    enabled: true                    # Enable virtual threads (default: true)
    pinning-monitoring: true         # Detect virtual thread pinning to platform threads (Java 24)
  actor:
    default-mailbox-capacity: 1000 # Default mailbox capacity
    default-delivery-semantics: at-most-once  # Default delivery semantics
  supervision:
    default-strategy: one-for-one   # Default supervision strategy
    max-restarts: 10                # Maximum restart count
    restart-window: 10s             # Restart window duration
  metrics:
    enabled: true                   # Enable metrics collection
    export:
      prometheus:
        enabled: true
        path: /actuator/prometheus
```

## 🌲 Supervision Strategies

```java
@ActorBean
public class OrderSupervisor extends SupervisorActor {

    @Override
    protected SupervisorStrategy createStrategy() {
        return SupervisorStrategy.builder()
            .strategyType(SupervisorStrategy.Type.ONE_FOR_ONE)
            .maxRestarts(10)
            .withinDuration(Duration.ofSeconds(10))
            .backoff(Duration.ofMillis(100))
            .build();
    }

    @Override
    protected void initChildren() {
        supervise(OrderProcessor.class, "order-processor");
        supervise(PaymentActor.class, "payment-actor");
        supervise(InventoryActor.class, "inventory-actor");
    }
}
```

## 📚 Examples

See the [aether-examples](aether-examples/) directory for complete examples:

- **Order Service** — Order processing service demonstrating Actor definition, message passing, and supervision trees

## 🛠️ Build

```bash
# Clone the repository
git clone https://github.com/1lovevv/aether.git
cd aether

# Build
mvn clean install

# Run tests
mvn test

# Build and publish to Maven Central (requires configuration)
mvn clean deploy -P release
```

## 📖 Documentation

- [Design Document](docs/superpowers/specs/2026-06-05-aether-design.md)
- [Implementation Plan](docs/superpowers/plans/2026-06-05-aether-implementation.md)
- [Contributing Guide](CONTRIBUTING.md)

## 🗺️ Roadmap

### v1.0 (Completed)
- [x] Core Actor model (lifecycle, mailbox, message passing)
- [x] Hierarchical supervision tree + pluggable strategies
- [x] Spring Boot Starter (auto-configuration, annotation support)
- [x] Micrometer integration (Actor + system-level metrics)
- [x] SPI interfaces (ActorFactory, ThreadScheduler, ActorRegistry)

### v1.1 (Planned)
- [ ] Supervision-tree-level monitoring metrics
- [ ] Bounded mailboxes and backpressure strategies
- [ ] Hot Actor reloading (zero-downtime behavior replacement)
- [ ] Full Java 24 virtual thread feature support

### v2.0 (Vision)
- [ ] Transparent distribution (remote Actor invocation, location transparency)
- [ ] gRPC-based Actor migration and serialization
- [ ] Saga pattern for long-running transaction coordination
- [ ] Redis registry support (distributed Actor discovery)
- [ ] Cluster sharding and load balancing

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## 📄 License

[MIT License](LICENSE)

## 🙏 Acknowledgments

- Inspired by [Akka](https://akka.io/) and [Erlang/OTP](https://www.erlang.org/)
- Built on Java 21 [Virtual Threads](https://openjdk.org/jeps/444)
- Spring Boot integration inspired by [Spring Boot Starter](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.build-systems.starters)

---

> **Aether** — Named after the classical element believed to be the medium through which light waves propagate. Just as the aether carries light, the Aether framework carries messages, making concurrent programming simple, intuitive, and efficient.
