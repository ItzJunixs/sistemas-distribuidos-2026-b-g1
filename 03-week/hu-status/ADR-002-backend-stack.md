# ADR-002 — Backend Stack: Java 21 and Spring Boot 3 with Hexagonal Architecture

- **ID:** ADR-002
- **Date:** 2026-08-21
- **Status:** Accepted
- **Authors:** Team G1 — eduTrack

---

## Context

eduTrack is composed of five modular microservices requiring strong typing, robust dependency injection, maintainability, testability, and standard enterprise integration patterns. The team needs a mature backend stack that supports Hexagonal Architecture (Ports and Adapters) to isolate business invariants from frameworks, web protocols, and persistence layers.

**Known constraints:**
- The domain core must remain independent of framework dependencies.
- Clear folder and package conventions must be shared across all services.
- Fast onboarding and comprehensive test double support (Unit tests without Spring context, Integration tests with Testcontainers).

---

## Decision

**We decided:** Use **Java 21 (LTS)** with **Spring Boot 3.x** and Maven as the standard backend stack across all eduTrack microservices, structuring each service following Hexagonal Architecture.

**Justification:**
Java 21 provides modern language features (Records for DTOs and value objects, Pattern Matching, Virtual Threads), while Spring Boot 3.x provides production-ready observability (Micrometer, Actuator), robust AMQP support (`spring-boot-starter-amqp`), and enterprise data access (`spring-boot-starter-data-jpa`). Hexagonal architecture guarantees that domain models can be tested with 100% pure Java unit tests without loading the Spring container.

---

## Evaluated alternatives

| Alternative | Pros | Cons | Reason for discarding |
|------------|------|------|-----------------------|
| **Java 21 + Spring Boot 3 (Chosen)** | Enterprise stability, rich ecosystem, native Hexagonal support, excellent AMQP/PostgreSQL integration | Higher memory footprint than Go | — (Chosen) |
| **Node.js + TypeScript (NestJS)** | Fast startup, single language across full-stack | Type safety is compile-time only, ecosystem fragmentation | Java offers stronger type invariants for academic records and attendance |
| **Python + FastAPI** | Rapid prototyping, concise syntax | Async ecosystem nuances, lower throughput under heavy synchronous workloads | Java provides stronger enterprise architecture patterns and tooling |
| **Go** | Minimal memory usage, very fast startup | Verbose error handling, manual dependency injection | Team familiarity and established Spring testing frameworks (Testcontainers) |

---

## Consequences

**Positive:**
- Strict separation of concerns via domain ports and infrastructure adapters.
- Fast unit test execution by avoiding Spring context instantiation in `domain/` and `application/` packages.
- Uniform tooling, linting, and build pipeline using Maven across all services.

**Negative / Trade-offs:**
- Requires discipline to prevent developers from importing `org.springframework.*` inside `domain/` packages.

**Impact on the system:**
- Affected services: `identity-service`, `academic-service`, `attendance-service`, `notifications-service`, `communication-service`.
- Documents that must be updated: `_stacks/java-spring.md`, `05-architecture/hexagonal-architecture.md`, `09-microservices/service-catalog.md`.

---

## References

- Hexagonal Architecture Guide → `05-architecture/hexagonal-architecture.md`
- Java Spring Stack Specification → `_stacks/java-spring.md`
