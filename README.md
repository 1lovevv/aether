**中文** | [English](README_EN.md)

# Aether（以太）

[![Maven Central](https://img.shields.io/maven-central/v/io.github.1lovevv/aether-spring-boot-starter.svg)](https://search.maven.org/search?q=g:io.github.1lovevv)
[![JitPack](https://jitpack.io/v/1lovevv/aether.svg)](https://jitpack.io/#1lovevv/aether)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)

> 基于 Java 虚拟线程的轻量级 Actor 模型框架

Aether 是一个为现代 Java 应用设计的轻量级 Actor 模型框架，充分利用 Java 21+ 虚拟线程（Virtual Threads）的高并发能力，提供直观的 Actor 编程模型、强大的监督树机制以及开箱即用的 Spring Boot 集成。

## ✨ 特性

- **🚀 虚拟线程 Actor** — 每个 Actor 运行在独立虚拟线程上，轻松实现百万级并发
- **🌲 监督树** — 分层监督策略（OneForOne / AllForOne / RestForOne），自动故障恢复
- **🔧 Spring Boot 集成** — `@ActorBean` 和 `@OnMessage` 注解，零样板代码
- **📊 Micrometer 监控** — Actor 级别和系统级别指标，无缝接入 Prometheus/Grafana
- **🔌 SPI 解耦** — 核心零 Spring 依赖，通过 SPI 插件机制实现框架扩展

## 📦 安装

### Maven Central（推荐）

```xml
<dependency>
    <groupId>io.github.1lovevv</groupId>
    <artifactId>aether-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### JitPack（最新开发版）

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

### 为什么选择 Aether？

| 特性 | Aether | Akka |
|------|--------|------|
| **并发模型** | 虚拟线程（Project Loom） | 传统线程池 |
| **代码风格** | 同步、阻塞 | 异步、回调 |
| **Spring Boot** | 原生集成 | 需适配 |
| **学习曲线** | 低 | 高 |
| **内存/Actor** | ~1-2KB | ~300B + 线程池 |
| **Actor 数量** | 百万级 | 千级 |
| **许可证** | MIT（完全免费） | BSL（商业需付费） |

### 虚拟线程 vs 传统线程

**Aether — 虚拟线程（推荐）**：

```java
@ActorBean
public class OrderProcessor {
    @OnMessage
    public void onCreateOrder(CreateOrderMsg msg) {
        // ✅ 同步代码，性能无损！
        Order order = database.save(msg.toOrder());  // 阻塞OK！
        Thread.sleep(1000);                            // 阻塞OK！
        httpClient.post(order);                         // 阻塞OK！
    }
}
```

**Akka — 传统线程池**：

```java
// ❌ 必须使用异步模式
public class OrderActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateOrderMsg.class, msg -> {
                // 回调地狱
                database.saveAsync(msg.toOrder())
                    .thenCompose(order -> httpClient.postAsync(order))
                    .thenAccept(result -> sender().tell(result, self()));
            })
            .build();
    }
}
```

### 关键优势

1. **🚀 虚拟线程原生** — 阻塞代码性能无损，告别回调地狱
2. **🌲 简单监督** — 注解式配置，无需复杂层级
3. **📊 Spring Boot 优先** — 零配置自动发现
4. **📦 轻量级** — 200KB vs 5MB+
5. **💰 MIT 许可证** — 完全免费，商业友好

> **完整对比**：查看 [AETHER_VS_AKKA.md](AETHER_VS_AKKA.md)

## 🚀 快速开始

### 1. 定义 Actor

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
        // 同步、阻塞代码，运行在虚拟线程上
        Order order = orderRepository.save(msg.toOrder());
        System.out.println("Order created: " + order.id());
    }

    @OnMessage
    public void onCancelOrder(CancelOrderMsg msg) {
        orderRepository.cancel(msg.orderId());
    }
}
```

### 2. 发送消息

```java
@Service
public class OrderService {

    @Autowired
    @Qualifier("order-processor")
    private ActorRef orderProcessor;

    public void createOrder(CreateOrderMsg msg) {
        // 异步发送
        orderProcessor.tell(msg);
    }

    public Order getOrder(UUID orderId) {
        // 同步等待回复（超时 5 秒）
        CompletableFuture<OrderResult> future =
            orderProcessor.ask(new QueryOrderMsg(orderId), Duration.ofSeconds(5));
        return future.get();
    }
}
```

### 3. 配置 application.yml

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

## 🏗️ 架构

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

## 📊 监控指标

### Actor 级别指标

| Metric Name | Type | Tags | Description |
|-------------|------|------|-------------|
| `aether.actor.messages.received` | Counter | `actor`, `message_type` | 接收消息总数 |
| `aether.actor.messages.processed` | Counter | `actor`, `message_type` | 成功处理消息数 |
| `aether.actor.messages.failed` | Counter | `actor`, `message_type`, `exception` | 处理失败消息数 |
| `aether.actor.mailbox.size` | Gauge | `actor` | 当前邮箱积压量 |
| `aether.actor.processing.time` | Timer | `actor`, `message_type` | 消息处理耗时 |
| `aether.actor.state` | Gauge | `actor`, `state` | Actor 当前状态 |

### 系统级别指标

| Metric Name | Type | Description |
|-------------|------|-------------|
| `aether.system.actors.total` | Gauge | 总 Actor 数 |
| `aether.system.actors.active` | Gauge | 活跃 Actor 数 |
| `aether.system.virtual_threads.pinned` | Gauge | 被 pinned 的虚拟线程数（Java 24） |
| `aether.system.throughput` | Gauge | 全局消息吞吐量（msg/s） |

### Prometheus 输出示例

```
# HELP aether_actor_mailbox_size Current mailbox size
# TYPE aether_actor_mailbox_size gauge
aether_actor_mailbox_size{actor="order-processor"} 42

# HELP aether_actor_messages_processed_total Total messages processed
# TYPE aether_actor_messages_processed_total counter
aether_actor_messages_processed_total{actor="order-processor",message_type="CreateOrderMsg"} 1024
```

## 🔧 配置详解

### 完整配置示例

```yaml
aether:
  virtual-threads:
    enabled: true                    # 启用虚拟线程（默认 true）
    pinning-monitoring: true         # 检测虚拟线程 pinned 到平台线程（Java 24）
  actor:
    default-mailbox-capacity: 1000 # 默认邮箱容量
    default-delivery-semantics: at-most-once  # 默认投递语义
  supervision:
    default-strategy: one-for-one   # 默认监督策略
    max-restarts: 10                # 最大重启次数
    restart-window: 10s             # 重启窗口时间
  metrics:
    enabled: true                   # 启用指标收集
    export:
      prometheus:
        enabled: true
        path: /actuator/prometheus
```

## 🌲 监督策略

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

## 📚 示例项目

查看 [aether-examples](aether-examples/) 目录获取完整示例：

- **Order Service** — 订单处理服务，演示 Actor 定义、消息传递、监督树

## 🛠️ 构建

```bash
# 克隆仓库
git clone https://github.com/1lovevv/aether.git
cd aether

# 构建
mvn clean install

# 运行测试
mvn test

# 构建并发布到 Maven Central（需要配置）
mvn clean deploy -P release
```

## 📖 文档

- [设计文档](docs/superpowers/specs/2026-06-05-aether-design.md)
- [实现计划](docs/superpowers/plans/2026-06-05-aether-implementation.md)
- [贡献指南](CONTRIBUTING.md)

## 🗺️ 路线图

### v1.0（已完成）
- [x] 核心 Actor 模型（生命周期、邮箱、消息传递）
- [x] 分层监督树 + 可插拔策略
- [x] Spring Boot Starter（自动配置、注解支持）
- [x] Micrometer 集成（Actor + 系统级别指标）
- [x] SPI 接口预留（ActorFactory、ThreadScheduler、ActorRegistry）

### v1.1（计划中）
- [ ] 监督树级别监控指标
- [ ] 有界邮箱与背压策略
- [ ] Actor 热升级（不停机替换行为）
- [ ] Java 24 虚拟线程特性全面支持

### v2.0（未来愿景）
- [ ] 透明分布式（Actor 远程调用、位置透明）
- [ ] 基于 gRPC 的 Actor 迁移与序列化
- [ ] Saga 模式长事务协调
- [ ] Redis 注册中心支持（分布式 Actor 发现）
- [ ] 集群分片与负载均衡

## 🤝 贡献

欢迎贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详情。

## 📄 许可证

[MIT License](LICENSE)

## 🙏 致谢

- 灵感来自 [Akka](https://akka.io/) 和 [Erlang/OTP](https://www.erlang.org/)
- 基于 Java 21 [虚拟线程](https://openjdk.org/jeps/444) 构建
- Spring Boot 集成参考了 [Spring Boot Starter](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using.build-systems.starters)

---

> **Aether** — 以太，古希腊人假想的传播媒介。正如以太承载光波，Aether 框架承载消息传递，让并发编程变得简单、直观、高效。
