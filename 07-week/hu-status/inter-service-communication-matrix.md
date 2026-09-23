# EduTrack Inter-Service Communication & Architecture Protocol Matrix

> **Scope:** Sprint 2 (Cut 2) Distributed Architecture Protocol Specification  
> **Course:** Distributed Systems 2026-B — Group 1  
> **Author:** Ximena Del Pilar Zambrano Chala (`@XimenaChala`)  
> **Governing ADRs:** ADR-003 (Database per Service), ADR-004 (Event-Driven Architecture), ADR-007 (RabbitMQ Event Bus), ADR-008 (Perimeter Gateway Filter), ADR-010 (Contract-First OpenAPI)

---

## 1. Architectural Overview & Boundary Definitions

The **EduTrack** distributed system uses a hybrid communication pattern:
- **Synchronous REST / HTTP (Perimeter & Client Ingress):** Handled through `educk-api-gateway` (:8080) for client-to-service interactions and synchronous state queries.
- **Asynchronous Event-Driven Messaging (Inter-Domain Choreography):** Mediated by **RabbitMQ** (:5672) topic exchange `edutrack.events` for cross-service state propagation and notifications, avoiding temporal coupling and synchronous cascade failures.

```
       [ Client / Browser Microfrontends ]
                       │
             (HTTPS / REST Synchronous)
                       ▼
          ┌──────────────────────────┐
          │  educk-api-gateway:8080  │ ───► [ Redis:6379 Token Blacklist ]
          └──────────────────────────┘
                       │
       ┌───────────────┼───────────────┬────────────────┐
       │ (REST)        │ (REST)        │ (REST)         │ (REST)
       ▼               ▼               ▼                ▼
┌──────────────┐┌──────────────┐┌──────────────┐ ┌──────────────┐
│  identity-   ││  academic-   ││ attendance-  │ │communication-│
│   api:8081   ││   api:8082   ││   api:8083   │ │   api:8085   │
└──────────────┘└──────────────┘└──────────────┘ └──────────────┘
       │               │ (Outbox)      │ (Async Event)          │
       ▼               ▼               ▼                        ▼
[identity_db]   [academic_db]   [attendance_db]         [communication_db]
(:5431)         (:5432)         (:5433)                 (:5435)
                       │               │
                       ▼               ▼
          ┌──────────────────────────────────────────────┐
          │      RabbitMQ Topic Exchange: edutrack.events│
          └──────────────────────────────────────────────┘
                                  │
                       (AMQP Routing Key)
                                  ▼
                     ┌──────────────────────────┐
                     │    notifications.queue   │
                     └──────────────────────────┘
                                  │
                                  ▼
                     ┌──────────────────────────┐
                     │       educk-worker       │ ───► [ Redis Dedup Cache ]
                     └──────────────────────────┘
                                  │ (Failed retries)
                                  ▼
                     ┌──────────────────────────┐
                     │    notifications.dlq     │
                     └──────────────────────────┘
```

---

## 2. Comprehensive Inter-Service Communication Matrix

| Source / Caller | Destination / Callee | Interaction Type | Protocol | Port / Transport | Routing Key / Endpoint | Delivery Semantics & Idempotency | Failure Mitigation & Resilience |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Microfrontends** | `educk-api-gateway` | Ingress Route | REST / JSON | TCP `:8080` (HTTP) | `/api/v1/**` | Synchronous Request-Response | Gateway Timeout (2500ms), Rate Limiter, CORS Filter |
| `educk-api-gateway` | `educk-identity-api` | Auth & Verification | REST / JSON | Internal `:8081` | `/api/v1/auth/verify` | Synchronous; RS256 Public Key Verification | Gateway Route Fallback, Health Check Probing |
| `educk-api-gateway` | `educk-redis` | Token Revocation Check | Redis RESP | Internal `:6379` | `EXISTS blacklist:<token_jti>` | Synchronous; Sub-millisecond lookup | Fail-open / Local TTL Cache fallback |
| `educk-api-gateway` | `educk-academic-api` | Ingress Proxy | REST / JSON | Internal `:8082` | `/api/v1/grades/**` | Synchronous; Context Header Enrichment (`X-User-Id`) | Circuit Breaker, Gateway Timeout (3000ms) |
| `educk-api-gateway` | `educk-attendance-api`| Ingress Proxy | REST / JSON | Internal `:8083` | `/api/v1/attendance/**`| Synchronous; Context Header Enrichment (`X-User-Id`) | Circuit Breaker, Gateway Timeout (3000ms) |
| `educk-api-gateway` | `educk-communication`| Ingress Proxy | REST / JSON | Internal `:8085` | `/api/v1/messages/**` | Synchronous; Mandatory `Idempotency-Key` header | Relational unique constraint `(sender_id, idempotency_key)` |
| `educk-academic-api` | `academic_db` | Transactional Outbox | PostgreSQL Wire | Internal `:5432` | Local Table `outbox_events` | ACID Transaction Atomicity (Local DB) | PostgreSQL WAL persistence, Zero 2PC overhead |
| `educk-academic-api` | `educk-rabbitmq` | Event Publishing | AMQP 0-9-1 | Internal `:5672` | Exchange: `edutrack.events`<br/>Key: `academic.grade.created` | At-least-once delivery; Outbox status update on ACK | Exponential backoff retry on broker reconnect |
| `educk-attendance-api`| `educk-rabbitmq` | Event Publishing | AMQP 0-9-1 | Internal `:5672` | Exchange: `edutrack.events`<br/>Key: `attendance.student.absent`| At-least-once delivery; Monotonic Lamport timestamp | Reconnection publisher retry loop |
| `educk-rabbitmq` | `educk-worker` | Event Consumption | AMQP 0-9-1 | Internal `:5672` | Queue: `notifications.queue` | At-least-once delivery; Manual ACK upon processing | Idempotent check via Redis `SETNX eventId 86400` |
| `educk-worker` | `educk-redis` | Deduplication Guard | Redis RESP | Internal `:6379` | Key: `dedup:event:<eventId>` | Atomic `SETNX`; returns 0 if already processed | If duplicate: ACK and discard immediately |
| `educk-worker` | `educk-rabbitmq` | Poison Message Routing | AMQP 0-9-1 | Internal `:5672` | Exchange: `edutrack.dlx`<br/>Queue: `notifications.dlq` | Dead Letter Exchange forwarding | Max 3 retry attempts before routing to DLQ |

---

## 3. Asynchronous Event Payloads & Schema Definitions

### 3.1 Event: `GradeCreated`
- **Producer:** `educk-academic-api`
- **Exchange:** `edutrack.events` (Topic)
- **Routing Key:** `academic.grade.created`
- **Consumer:** `educk-worker`
- **Payload Schema:**
  ```json
  {
    "eventId": "e9b28a47-5e63-4c91-a67b-118822993344",
    "eventType": "GradeCreated",
    "aggregateId": "8f3e5b72-0011-4433-8899-aabbccddeeff",
    "occurredOn": "2026-09-22T19:40:00.000Z",
    "version": 1,
    "payload": {
      "gradeId": "8f3e5b72-0011-4433-8899-aabbccddeeff",
      "studentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "teacherId": "7ca85f64-5717-4562-b3fc-2c963f66aba1",
      "courseId": "1ba85f64-5717-4562-b3fc-2c963f66acc9",
      "term": "2026-B",
      "value": 4.5,
      "feedback": "Outstanding performance in distributed consensus lab"
    }
  }
  ```

### 3.2 Event: `StudentAbsent` (with Causal Ordering)
- **Producer:** `educk-attendance-api`
- **Exchange:** `edutrack.events` (Topic)
- **Routing Key:** `attendance.student.absent`
- **Consumer:** `educk-worker`
- **Causal Guarantee:** Monotonic Lamport sequence integer + UTC session timestamp.
- **Payload Schema:**
  ```json
  {
    "eventId": "f1c48a90-3412-4ee1-b765-998877665544",
    "eventType": "StudentAbsent",
    "aggregateId": "4da85f64-5717-4562-b3fc-2c963f66aee2",
    "occurredOn": "2026-09-22T07:15:00.000Z",
    "version": 1,
    "lamportClock": 104,
    "payload": {
      "sessionId": "4da85f64-5717-4562-b3fc-2c963f66aee2",
      "studentId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "courseId": "1ba85f64-5717-4562-b3fc-2c963f66acc9",
      "status": "ABSENT",
      "sessionDate": "2026-09-22"
    }
  }
  ```

---

## 4. Error Handling & Standardized Error Envelope

All synchronous REST endpoints across the system strictly adhere to the unified error response schema per ADR-010:

```json
{
  "error": {
    "code": "IDEMPOTENCY_CONFLICT",
    "message": "A message with the provided Idempotency-Key has already been processed",
    "details": [
      "idempotency_key=8f3e5b72-0011-4433-8899-aabbccddeeff",
      "original_status=SENT"
    ],
    "trace_id": "b7909af0-0f28-4471-b3bf-8de6bed9fe10"
  }
}
```
