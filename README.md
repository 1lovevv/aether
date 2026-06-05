# Aether（以太）

> 基于 Java 虚拟线程的轻量级 Actor 模型框架

Aether 是一个为现代 Java 应用设计的轻量级 Actor 模型框架，充分利用 Java 21+ 虚拟线程（Virtual Threads）的高并发能力，提供直观的 Actor 编程模型、强大的监督树机制以及开箱即用的 Spring Boot 集成。

## Features

- **🚀 虚拟线程 Actor** — 每个 Actor 运行在独立虚拟线程上，轻松实现百万级并发
- **🌲 监督树** — 分层监督策略（OneForOne / AllForOne / RestForOne），自动故障恢复
- **🔧 Spring Boot 集成** — `@ActorBean` 和 `@OnMessage` 注解，零样板代码
- **📊 Micrometer 监控** — Actor 级别和系统级别指标，无缝接入 Prometheus/Grafana
- **🔌 SPI 解耦** — 核心零 Spring 依赖，通过 SPI 插件机制实现框架扩展

## Quick Start

### Maven Dependency (via JitPack)

**Step 1**: 添加 JitPack 仓库

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**Step 2**: 添加 Aether 依赖

```xml
<dependency>
    <groupId>com.github.1lovevv</groupId>
    <artifactId>aether</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

> **Note**: 正式发布到 Maven Central 后，将使用 `com.aether` groupId。当前通过 JitPack 提供预览版本。

### 本地构建

```bash
git clone https://github.com/1lovevv/aether.git
cd aether
mvn clean install
```

### Define an Actor

```java
import com.aether.spring.annotation.ActorBean;
import com.aether.spring.annotation.OnMessage;
import com.aether.core.actor.ActorRef;

@ActorBean
public class OrderActor {

    @OnMessage
    public void onCreateOrder(CreateOrderCommand command) {
        // 处理订单创建逻辑
        System.out.println("Creating order: " + command.orderId());
    }

    @OnMessage
    public String onQueryStatus(QueryStatusQuery query) {
        return "PROCESSING";
    }
}
```

### Send Messages

```java
@Service
public class OrderService {

    @Autowired
    private ActorRef<OrderActor> orderActor;

    public void createOrder(String orderId) {
        orderActor.tell(new CreateOrderCommand(orderId));
    }

    public String getStatus() {
        return orderActor.ask(new QueryStatusQuery(), Duration.ofSeconds(5));
    }
}
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              aether-spring-boot-starter                 │
│         (Spring Boot Auto-Configuration)              │
├─────────────────────────────────────────────────────────┤
│                    aether-core                          │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │   Actor     │  │  Supervisor │  │    Mailbox     │  │
│  │  (Virtual   │  │   (Super-   │  │  (Bounded      │  │
│  │   Thread)   │  │   vision    │  │   Queue)       │  │
│  └─────────────┘  │   Tree)     │  └────────────────┘  │
│                   └─────────────┘                       │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────┐  │
│  │   SPI       │  │   Metrics   │  │   Scheduler    │  │
│  │ Interface   │  │ (Micrometer)│  │ (Virtual Thrd) │  │
│  └─────────────┘  └─────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### SPI Interfaces

| Interface | Description |
|-----------|-------------|
| `ActorFactory` | 创建 Actor 实例的工厂 |
| `MailboxFactory` | 创建和管理邮箱实例 |
| `SupervisorStrategy` | 自定义监督策略 |
| `ActorMetricsReporter` | 自定义指标上报 |
| `ThreadFactoryProvider` | 提供虚拟线程工厂 |

## Configuration

```yaml
aether:
  virtual-threads:
    enabled: true                    # 启用虚拟线程（默认 true）
  actor:
    default-mailbox-capacity: 10000  # 默认邮箱容量
  supervision:
    default-strategy: OneForOne      # 默认监督策略
  metrics:
    enabled: true                    # 启用指标收集
```

## Monitoring Metrics

| Metric Name | Type | Description |
|-------------|------|-------------|
| `aether.actor.messages.sent` | Counter | Actor 发送消息总数 |
| `aether.actor.messages.received` | Counter | Actor 接收消息总数 |
| `aether.actor.messages.processed` | Counter | Actor 成功处理消息数 |
| `aether.actor.messages.failed` | Counter | 消息处理失败数 |
| `aether.actor.mailbox.size` | Gauge | 当前邮箱队列长度 |
| `aether.actor.active.count` | Gauge | 活跃 Actor 数量 |
| `aether.system.actors.total` | Gauge | 系统中 Actor 总数 |
| `aether.supervisor.restarts` | Counter | 监督器触发重启次数 |

## Roadmap

- **MVP (v1.0)**: Core + Spring Boot + Monitoring
- **v1.1**: 监督树指标、有界邮箱、Actor 热重载
- **v2.0**: 分布式 Actor、Saga 事务模式、Redis 服务发现

## License

MIT License
