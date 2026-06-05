# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned

- Distributed actor support (remote actors via gRPC)
- Saga pattern for long-running transactions
- Redis-based actor registry for clustering
- Bounded mailbox with backpressure strategies
- Actor hot-reloading without restart

## [1.0.0] - 2026-06-05

### Added

#### Core Features
- **Virtual Thread Actor** — Each actor runs on an independent virtual thread, enabling millions of concurrent actors
- **Mailbox System** — BlockingQueue-based mailbox with configurable capacity (bounded/unbounded)
- **Message Passing** — Asynchronous `tell()` and synchronous `ask()` patterns with timeout support
- **Actor Lifecycle** — Full lifecycle management: CREATED → STARTING → RUNNING → STOPPING → STOPPED → TERMINATED
- **ActorRef Abstraction** — Location-transparent actor references, ready for distributed extensions

#### Supervision Tree
- **Hierarchical Supervision** — Parent-child actor relationships with automatic failure propagation
- **Supervisor Strategies**
  - OneForOne — Restart only the failed child
  - AllForOne — Restart all children when any fails
  - RestForOne — Restart the failed child and all subsequent children
- **Failure Handling** — RESTART, STOP, ESCALATE, TERMINATE directives
- **Restart Policies** — Configurable max restarts, restart window, and exponential backoff

#### Spring Boot Integration
- **@ActorBean** — Annotation for declaring Spring-managed actors
- **@OnMessage** — Annotation for message handler methods
- **@OnFailure** — Annotation for failure handler methods
- **Auto-Configuration** — Automatic bean creation and lifecycle management
- **Dependency Injection** — Full Spring DI support within actors

#### Monitoring & Metrics
- **Micrometer Integration** — Actor-level and system-level metrics
- **Prometheus Support** — Built-in Prometheus-compatible metrics export
- **Key Metrics**
  - `aether.actor.messages.received` — Messages received count
  - `aether.actor.messages.processed` — Messages processed count
  - `aether.actor.messages.failed` — Messages failed count
  - `aether.actor.mailbox.size` — Current mailbox size
  - `aether.system.actors.total` — Total actor count
  - `aether.system.actors.active` — Active actor count

#### SPI Architecture
- **ActorFactory** — Pluggable actor creation strategy
- **ThreadScheduler** — Pluggable thread scheduling (default: virtual threads)
- **ActorRegistry** — Pluggable actor registry (default: in-memory, future: Redis)
- **Zero Spring Dependencies** in core module

#### Message Serialization
- **MessageSerializer SPI** — Pluggable serialization framework
- **JsonMessageSerializer** — Default Java serialization with Base64 encoding
- **SerializerRegistry** — Content-type based serializer selection

#### Testing
- **69 Tests** — Comprehensive unit and integration test coverage
- **JUnit 5** — Modern testing framework
- **Spring Boot Test** — Integration tests for auto-configuration
- **Test Coverage**
  - Actor lifecycle and message processing
  - Mailbox behavior (bounded/unbounded)
  - Supervision strategies and failure handling
  - Spring Boot auto-configuration
  - Metrics collection and reporting

#### Documentation
- **Comprehensive README** — Features, quick start, configuration, examples
- **Design Document** — Architecture and design decisions
- **Implementation Plan** — Step-by-step implementation guide
- **Contributing Guide** — Guidelines for contributors
- **Javadoc** — Complete API documentation

### Technical Details

#### Dependencies
- Java 21+ (Virtual Threads)
- Spring Boot 3.x
- Micrometer 1.12.x
- SLF4J 2.x
- JUnit 5.10.x

#### Build System
- Maven multi-module project
- Maven Central publishing support
- GPG signing for artifacts
- Source and Javadoc jar generation

### Notes

This is the first stable release of Aether Framework. The API is considered stable, but may evolve based on community feedback.

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-06-05 | Initial stable release |

---

## Contributing to Changelog

When adding a new entry, please follow this format:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Now removed features

### Fixed
- Bug fixes

### Security
- Security improvements
```
