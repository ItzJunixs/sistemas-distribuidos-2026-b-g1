# ADR-004 — Asynchronous Messaging: RabbitMQ for Event-Driven Communication

- **ID:** ADR-004
- **Date:** 2026-08-21
- **Status:** Accepted
- **Authors:** Team G1 — eduTrack

---

## Context

When teachers record academic grades (`GradeCreated`) or register student absences (`StudentAbsent`), parents need to be alerted promptly. Doing this via synchronous HTTP calls from `academic-service` and `attendance-service` to `notifications-service` creates temporal coupling: if the notification service is slow or down, the teacher's grading action would fail or hang.

**Known constraints:**
- Low operational complexity for local development (Docker Compose).
- Reliable delivery with message acknowledgement and retry support.
- Decoupled publish/subscribe semantics for domain events.

---

## Decision

**We decided:** Adopt **RabbitMQ (AMQP 0-9-1)** as the event broker for asynchronous domain event propagation between publishing services (`academic-service`, `attendance-service`) and subscribing services (`notifications-service`).

**Justification:**
RabbitMQ is lightweight, easy to run in Docker with minimal resource usage, supports flexible topic exchange routing (`edutrack.events`), and provides out-of-the-box management UI and dead-letter exchanges (DLX) for retry handling. It integrates seamlessly with Spring Boot via `spring-boot-starter-amqp` without the clustering complexity of Apache Kafka.

---

## Evaluated alternatives

| Alternative | Pros | Cons | Reason for discarding |
|------------|------|------|-----------------------|
| **RabbitMQ (Chosen)** | Lightweight, topic routing, AMQP standard, simple local Docker setup, built-in management UI | Lower throughput compared to Kafka at massive scale | — (Chosen, perfect for eduTrack MVP scale) |
| **Apache Kafka** | Extreme throughput, long-term event retention | Complex configuration (Zookeeper/KRaft), high memory footprint for local dev | Over-engineering for eduTrack MVP scope |
| **Redis Pub/Sub** | In-memory speed, simple setup | No native persistence or consumer acknowledgment guarantees | Message loss risk on service failure |
| **Synchronous REST Calls** | No message broker needed | High temporal coupling, cascade failures, slow teacher UI responses | Violates event-driven decoupling principle |

---

## Consequences

**Positive:**
- Complete decoupling of teacher workflows from notification delivery.
- Reliable buffering of notification requests during traffic spikes.
- Clear event-driven extension points for future consumers (e.g. audit or analytics).

**Negative / Trade-offs:**
- Requires idempotent consumer logic in `notifications-service` using unique event IDs to prevent duplicate alerts.
- Eventual consistency: parent notifications arrive asynchronously within milliseconds rather than immediately on the same HTTP thread.

**Impact on the system:**
- Affected services: `academic-service`, `attendance-service`, `notifications-service`.
- Documents that must be updated: `02-domain/domain-events.md`, `05-architecture/overview.md`, `10-devops/local-setup.md`.

---

## References

- Domain Events Catalog → `02-domain/domain-events.md`
- Architecture Overview → `05-architecture/overview.md`
