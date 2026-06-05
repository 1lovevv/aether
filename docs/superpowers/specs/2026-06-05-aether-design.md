# Aether（以太）框架设计文档

> **版本**: v1.0  
> **日期**: 2026-06-05  
> **状态**: 待实现  

---

## 1. 概述

### 1.1 项目定位

Aether 是一个基于 Java 虚拟线程的轻量级 Actor 模型框架，深度集成 Spring Boot 生态，面向高并发、高容错、易调试的分布式应用开发。

**核心理念**：用虚拟线程重新发明轻量级 Actor —— 一个 Actor 就是一个虚拟线程，内部代码完全同步、阻塞、易调试，却拥有数百万 Actor 的并发能力。

### 1.2 目标用户

- 熟悉 Spring Boot 生态的 Java 开发者
- 需要处理高并发但不想引入响应式编程复杂性的团队
- 希望获得 Actor 模型监督与容错能力，同时保持同步代码心智负担的开发者

### 1.3 技术栈

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21+ | 虚拟线程基础，预留 Java 24 特性 |
| Spring Boot | 3.x | 核心依赖注入与自动配置 |
| Micrometer | 1.12+ | 监控指标采集 |
| Maven/Gradle | - | 构建工具 |

---

## 2. 架构设计

### 2.1 总体架构

```
┌─────────────────────────────────────────────┐
│         用户应用层 (User Application)        │
│    @ActorBean 类, @OnMessage 方法,          │
│    @Autowired 依赖, application.yml 配置     │
├─────────────────────────────────────────────┤
│     aether-spring-boot-starter (模块)        │
│  ┌─────────────────────────────────────┐   │
│  │ SpringActorFactory implements       │   │
│  │ ActorFactory (SPI)                  │   │
│  │ - 从 Spring 容器创建 Actor Bean      │   │
│  │ - 处理 @ActorBean, @OnMessage 扫描   │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │ SpringActorRegistry implements      │   │
│  │ ActorRegistry (SPI)                 │   │
│  │ - ConcurrentHashMap 存储本地 Actor   │   │
│  │ - 支持按 ID / 类型查找               │   │
│  └─────────────────────────────────────┘   │
├─────────────────────────────────────────────┤
│         aether-core (模块)                  │
│  ┌─────────────────────────────────────┐   │
│  │ VirtualThreadScheduler implements   │   │
│  │ ThreadScheduler (SPI)               │   │
│  │ - Thread.ofVirtual().start()        │   │
│  │ - Java 24 特性探测与优化             │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────┐ ┌─────────┐ ┌────────────┐  │
│  │ Actor   │ │ Mailbox │ │ Supervisor │  │
│  │ Runtime │ │ (Blocking│ │ Tree       │  │
│  │         │ │ Queue)  │ │            │  │
│  └─────────┘ └─────────┘ └────────────┘  │
│  ┌─────────┐ ┌─────────┐ ┌────────────┐  │
│  │ ActorRef│ │ Message │ │ Metrics    │  │
│  │ (接口)   │ │ Router  │ │ (Micrometer)│  │
│  └─────────┘ └─────────┘ └────────────┘  │
└─────────────────────────────────────────────┘
```

### 2.2 核心原则

1. **aether-core 零 Spring 依赖**：Core 层只依赖 SPI 接口，不依赖 Spring 具体类
2. **SPI 解耦种子**：预留三个关键 SPI 接口（ActorFactory、ThreadScheduler、ActorRegistry），MVP 阶段各有一个实现
3. **Spring 原生集成**：Starter 层提供 Spring 注解支持和自动配置
4. **面向接口的核心隔离**：确保未来扩展不被框架锁定

### 2.3 模块依赖关系

```
aether-spring-boot-starter → aether-core
         ↑                        ↑
    用户应用层                (零外部依赖)
```

---

## 3. 核心 Actor 模型

### 3.1 Actor 定义与生命周期

```java
@ActorBean(name = "order-processor")  // 可选命名，默认类名小写
public class OrderProcessor {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // 同步、阻塞代码，运行在虚拟线程上
        Order order = orderRepository.save(msg.toOrder());
        sender().tell(new OrderCreated(order.id()));
    }
    
    @OnMessage
    public void onCancelOrder(CancelOrderMsg msg) {
        orderRepository.cancel(msg.orderId());
    }
}
```

**生命周期状态**：

```
CREATED → STARTING → RUNNING → STOPPING → STOPPED → TERMINATED
   ↑__________|__________|         |
   └__________重启时回到 CREATED____┘
```

### 3.2 消息类型系统

```java
// 消息基类 — 密封类约束协议
public sealed interface Message permits 
    CreateOrderMsg, CancelOrderMsg, OrderCreated, ... {}

// 不可变消息 — Java Record
public record CreateOrderMsg(
    UUID customerId,
    List<OrderItem> items,
    BigDecimal totalAmount
) implements Message {}
```

**消息投递语义配置**：

```java
@ActorBean(
    deliverySemantics = DeliverySemantics.AT_MOST_ONCE  // 默认
    // 可选: AT_LEAST_ONCE, EXACTLY_ONCE
)
```

### 3.3 邮箱（Mailbox）设计

每个 Actor 绑定一个 `LinkedBlockingQueue`（或 `ArrayBlockingQueue`），运行在独立虚拟线程上：

```java
void actorLoop() {
    while (state == RUNNING) {
        Message msg = mailbox.take();  // 阻塞，但只阻塞虚拟线程
        try {
            dispatchToHandler(msg);    // 调用 @OnMessage 方法
        } catch (Exception e) {
            supervisor.handleFailure(this, e);
        }
    }
}
```

**关键设计决策**：

- 邮箱容量默认无界（`LinkedBlockingQueue`），可配置为有界
- 有界邮箱满时策略：阻塞发送者（背压）或丢弃最旧消息（TTL）
- 消息按到达顺序处理（单线程语义），保证 Actor 内部无并发竞争

### 3.4 ActorRef 与消息发送

```java
public interface ActorRef {
    String path();           // 如 "/user/order-processor"
    void tell(Message msg);  // 异步发送，无返回值
    <T extends Message> CompletableFuture<T> ask(Message msg, Duration timeout);
}
```

**使用示例**：

```java
@Service
public class OrderService {
    @Autowired
    private ActorRef orderProcessor;
    
    public Order createOrder(CreateOrderMsg msg) {
        OrderCreated result = orderProcessor.ask(msg, Duration.ofSeconds(5));
        return result.order();
    }
}
```

---

## 4. 监督树与自愈机制

### 4.1 监督者定义

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

### 4.2 监督策略

| 策略 | 行为 | 适用场景 |
|------|------|----------|
| **OneForOne** | 只重启失败的子 Actor | 子 Actor 之间无依赖 |
| **AllForOne** | 一个失败，全部重启 | 子 Actor 强耦合 |
| **RestForOne** | 重启失败者及其后续创建的子 Actor | 流水线式依赖 |

### 4.3 失败处理流程

```
子 Actor 处理消息时抛出异常
        ↓
异常被捕获，Actor 状态设为 FAILED
        ↓
通知父 Supervisor（通过 mailbox）
        ↓
Supervisor 根据策略决策：
    ├─ 重启 (Restart) → 创建新实例，恢复 RUNNING
    ├─ 停用 (Stop)    → 状态设为 STOPPED
    ├─ 升级 (Escalate)→ 向上级 Supervisor 上报
    └─ 终止 (Terminate)→ 状态设为 TERMINATED
        ↓
若重启次数超过阈值 → Supervisor 自身失败，向上级上报
```

### 4.4 用户自定义失败处理

```java
@ActorBean
public class OrderProcessor {
    
    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // 业务逻辑...
    }
    
    @OnFailure
    public void onFailure(FailureEvent event) {
        logger.error("Actor {} failed: {}", 
            event.actorRef().path(), 
            event.cause().getMessage());
        
        if (event.cause() instanceof DatabaseException) {
            event.recover();  // 标记已恢复，不通知 Supervisor
        }
    }
}
```

### 4.5 监督树结构示例

```
Root Supervisor (系统级)
├── User Guardian
│   ├── OrderSupervisor
│   │   ├── order-processor
│   │   ├── payment-actor
│   │   └── inventory-actor
│   └── UserSupervisor
│       ├── user-service
│       └── auth-service
└── System Guardian
    ├── metrics-collector
    └── health-checker
```

---

## 5. Spring Boot 集成

### 5.1 自动配置

```java
@Configuration
@ConditionalOnClass(AetherActor.class)
@EnableConfigurationProperties(AetherProperties.class)
public class AetherAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public ActorFactory actorFactory(ApplicationContext context) {
        return new SpringActorFactory(context);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ActorRegistry actorRegistry() {
        return new SpringActorRegistry();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ThreadScheduler threadScheduler(AetherProperties props) {
        return new VirtualThreadScheduler(props);
    }
}
```

### 5.2 配置项（application.yml）

```yaml
aether:
  virtual-threads:
    enabled: true
    pinning-monitoring: true  # Java 24 特性
  actor:
    default-mailbox-capacity: 1000
    default-delivery-semantics: at-most-once
  supervision:
    default-strategy: one-for-one
    max-restarts: 10
    restart-window: 10s
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        path: /actuator/prometheus
```

### 5.3 依赖注入

```java
@Service
public class OrderService {
    // 方式 1: 按名称注入
    @Autowired
    @Qualifier("order-processor")
    private ActorRef orderProcessor;
    
    // 方式 2: 按类型注入
    @Autowired
    private OrderProcessor orderProcessorActor;
    
    // 方式 3: 注入 ActorRegistry 手动查找
    @Autowired
    private ActorRegistry registry;
}
```

---

## 6. 监控指标（Micrometer）

### 6.1 Actor 级别指标

| Metric Name | Type | Tags | Description |
|-------------|------|------|-------------|
| `aether.actor.messages.received` | Counter | `actor`, `message_type` | 接收消息总数 |
| `aether.actor.messages.processed` | Counter | `actor`, `message_type` | 成功处理消息数 |
| `aether.actor.messages.failed` | Counter | `actor`, `message_type`, `exception` | 处理失败消息数 |
| `aether.actor.mailbox.size` | Gauge | `actor` | 当前邮箱积压量 |
| `aether.actor.processing.time` | Timer | `actor`, `message_type` | 消息处理耗时 |
| `aether.actor.state` | Gauge | `actor`, `state` | Actor 当前状态 |

### 6.2 系统级别指标

| Metric Name | Type | Description |
|-------------|------|-------------|
| `aether.system.actors.total` | Gauge | 总 Actor 数 |
| `aether.system.actors.active` | Gauge | 活跃 Actor 数 |
| `aether.system.virtual_threads.pinned` | Gauge | 被 pinned 的虚拟线程数（Java 24） |
| `aether.system.throughput` | Gauge | 全局消息吞吐量（msg/s） |

### 6.3 Prometheus 输出示例

```
# HELP aether_actor_mailbox_size Current mailbox size
# TYPE aether_actor_mailbox_size gauge
aether_actor_mailbox_size{actor="order-processor"} 42

# HELP aether_actor_messages_processed_total Total messages processed
# TYPE aether_actor_messages_processed_total counter
aether_actor_messages_processed_total{actor="order-processor",message_type="CreateOrderMsg"} 1024
```

---

## 7. SPI 接口定义

### 7.1 ActorFactory

```java
public interface ActorFactory {
    <T> T createActor(Class<T> actorClass, String name);
    void destroyActor(String name);
}
```

### 7.2 ThreadScheduler

```java
public interface ThreadScheduler {
    void schedule(Runnable task);
    void shutdown();
}
```

### 7.3 ActorRegistry

```java
public interface ActorRegistry {
    void register(String id, ActorRef ref);
    Optional<ActorRef> findActor(String id);
    Collection<ActorRef> findActorsByType(Class<?> actorClass);
}
```

---

## 8. 项目结构

```
aether/
├── aether-core/                    # 核心框架（零 Spring 依赖）
│   ├── src/main/java/com/aether/
│   │   ├── actor/                  # Actor 运行时
│   │   ├── mailbox/                # 邮箱实现
│   │   ├── supervisor/             # 监督树
│   │   ├── message/                # 消息路由
│   │   ├── metrics/                # 指标采集接口
│   │   └── spi/                    # SPI 接口定义
│   │       ├── ActorFactory.java
│   │       ├── ThreadScheduler.java
│   │       └── ActorRegistry.java
│   └── pom.xml
├── aether-spring-boot-starter/     # Spring Boot 集成
│   ├── src/main/java/com/aether/spring/
│   │   ├── SpringActorFactory.java
│   │   ├── SpringActorRegistry.java
│   │   ├── VirtualThreadScheduler.java
│   │   ├── AetherAutoConfiguration.java
│   │   ├── AetherProperties.java
│   │   └── annotation/
│   │       ├── ActorBean.java
│   │       ├── OnMessage.java
│   │       └── OnFailure.java
│   └── pom.xml
├── aether-examples/                # 示例项目
│   └── order-service/
├── docs/
│   └── superpowers/
│       └── specs/
│           └── 2026-06-05-aether-design.md
├── README.md
├── LICENSE
└── pom.xml (parent)
```

---

## 9. 路线图（Roadmap）

### MVP（v1.0）

- [x] 核心 Actor 模型（生命周期、邮箱、消息传递）
- [x] 分层监督树 + 可插拔策略
- [x] Spring Boot Starter（自动配置、注解支持）
- [x] Micrometer 集成（Actor + 系统级别指标）
- [x] SPI 接口预留（ActorFactory、ThreadScheduler、ActorRegistry）

### v1.1（下一阶段）

- [ ] 监督树级别监控指标（重启次数、失败率、策略触发统计）
- [ ] 有界邮箱与背压策略
- [ ] Actor 热升级（不停机替换行为）
- [ ] Java 24 虚拟线程特性全面支持

### v2.0（未来愿景）

- [ ] 透明分布式（Actor 远程调用、位置透明）
- [ ] 基于 gRPC 的 Actor 迁移与序列化
- [ ] Saga 模式长事务协调
- [ ] Redis 注册中心支持（分布式 Actor 发现）
- [ ] 集群分片与负载均衡

---

## 10. 关键设计决策记录（ADR）

### ADR-001：选择 Spring 原生 Actor 架构

**决策**：采用方案 B（Spring 原生 Actor），而非方案 A（经典 Actor + Spring 适配层）或方案 C（模块化分层）。

**理由**：
- 目标用户是 Spring 生态用户，Spring 原生集成最自然
- MVP 阶段减少适配层，降低复杂度，更快交付
- 注解驱动 API 与 Spring 天然契合

**风险对冲**：在 Core 内部预留 SPI 接口层，未来可较容易地拆分。

### ADR-002：消息语义默认 At-most-once

**决策**：默认 At-most-once，用户可通过注解配置更严格的语义。

**理由**：
- 性能最优，实现最简单
- 大多数场景下消息丢失可接受（如日志、监控）
- 需要可靠投递的场景显式配置，避免默认开销

### ADR-003：邮箱使用 BlockingQueue 而非 Disruptor

**决策**：使用 `LinkedBlockingQueue` 作为默认邮箱。

**理由**：
- 虚拟线程下阻塞操作成本极低，无需无锁队列的复杂度
- `BlockingQueue` 是 Java 标准库，维护成本低
- 未来可按需替换为更高效的实现（通过 SPI）

---

## 11. 测试策略

### 11.1 单元测试

- 每个 Actor 类独立测试，`@OnMessage` 方法覆盖
- Mock `ActorRef` 和 `Mailbox`，验证消息路由逻辑
- 监督策略单元测试：模拟失败场景，验证重启/停止行为

### 11.2 集成测试

- Spring Boot Test 上下文加载验证
- Actor 生命周期完整流程测试（创建 → 运行 → 停止 → 重启）
- 监督树端到端测试（父子 Actor 失败传播）

### 11.3 性能测试

- 百万级 Actor 创建与消息吞吐量基准测试
- 虚拟线程 vs 平台线程对比测试
- 邮箱积压场景下的背压行为验证

---

*文档结束*
