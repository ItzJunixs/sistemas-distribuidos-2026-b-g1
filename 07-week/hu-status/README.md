<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       07-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 07

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Design and implement inter-service communication across EduTrack bounded contexts — establish REST contracts (OpenAPI), asynchronous event choreography via RabbitMQ (Topic Exchange, DLQ), consumer idempotency, and versioned contract evolution (ADR-004, ADR-007, ADR-008) for Sprint 2.
<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-001 | Academic Gradebook Management, Transactional Outbox Pattern & `GradeCreated` Domain Event | done | Core API: [`code-corhuila/educk-academic-api`](https://github.com/code-corhuila/educk-academic-api) (commit `fe87ce4`) · DB: [`code-corhuila/educk-academic-db`](https://github.com/code-corhuila/educk-academic-db) (commit `a252edd`) · UI Portal: [`code-corhuila/educk-academic-portal`](https://github.com/code-corhuila/educk-academic-portal) |
| HU-002 | Asynchronous Notifications Worker, RabbitMQ Topic Consumer (`edutrack.events`), Redis-Backed Idempotent Deduplication, and Dead Letter Queue (`notifications.dlq`) | done | Worker: [`code-corhuila/educk-worker`](https://github.com/code-corhuila/educk-worker) (commit `2bef42d`) · DB: `educk-notifications-db` (:5434) · Architecture: [`ADR-007-rabbitmq-event-bus`](https://github.com/code-corhuila/educk-docs/blob/main/05-architecture/decisions/records/ADR-007-rabbitmq-event-bus.md) |
| HU-003 | Identity & Access Management (IAM), RS256 Asymmetric JWT Issuance, Redis Token Blacklist, and Perimeter Security API Gateway Routing | done | Auth API: [`code-corhuila/educk-identity-api`](https://github.com/code-corhuila/educk-identity-api) (commit `478eb30`) · Gateway: [`code-corhuila/educk-api-gateway`](https://github.com/code-corhuila/educk-api-gateway) (commit `1e6e6d6`) · DB: [`code-corhuila/educk-identity-db`](https://github.com/code-corhuila/educk-identity-db) (commit `a6287a3`) · UI: [`code-corhuila/educk-identity-front`](https://github.com/code-corhuila/educk-identity-front) (PR #3) |
| HU-004 | Parent-Teacher Communication Service Walking Skeleton Defense, Hexagonal Inbound Port `GetConversationUseCase`, and Database Idempotency Guard | done | Core API: [`code-corhuila/edutrack`](https://github.com/code-corhuila/edutrack) (PR #7, commit `d2576da`) · DB Schema: [`code-corhuila/educk-communication-db`](https://github.com/code-corhuila/educk-communication-db) (commit `25e0dae`) · UI Portal: [`code-corhuila/educk-communication-portal`](https://github.com/code-corhuila/educk-communication-portal) |
| HU-005 | Class Attendance Registration, Monotonic Lamport Timestamps for Causal Ordering, and Asynchronous `StudentAbsent` Event Dispatch | done | API: [`code-corhuila/educk-attendance-api`](https://github.com/code-corhuila/educk-attendance-api) (commit `4ef61d8`) · DB: [`code-corhuila/educk-attendance-db`](https://github.com/code-corhuila/educk-attendance-db) (commit `0ada646`) · UI: [`code-corhuila/educk-attendance-portal`](https://github.com/code-corhuila/educk-attendance-portal) |
| HU-INFRA-S2 | Multi-Container Docker Compose Unified Infrastructure Stack on Private Bridge Network `edutrack-net` (5 Postgres Instances, Redis Blacklist, RabbitMQ Broker) | done | Infra Stack: [`code-corhuila/educk-infra`](https://github.com/code-corhuila/educk-infra) (PR #4 commit `18df8d9`, PR #6) · Compose Config: [`docker-compose.yml`](https://github.com/code-corhuila/educk-infra/blob/main/docker-compose.yml) |
| HU-CONTRACTS-V1 | Contract-First API Governance: Machine-Readable OpenAPI 3.0 Specs, AsyncAPI Event Schemas, RS256 Standardization, and Backward-Compatibility Policy | done | Contracts: [`07-api/contracts/openapi/`](https://github.com/code-corhuila/educk-docs/tree/main/07-api/contracts/openapi) · Governance: [`educk-docs` (commit `ca55309`, `1608af8`)](https://github.com/code-corhuila/educk-docs) · Catalog: [`09-microservices/service-catalog.md`](https://github.com/code-corhuila/educk-docs/blob/main/09-microservices/service-catalog.md) |

---

## 2. My individual contribution

### Inter-Service Communication Strategy: Synchronous REST vs. Asynchronous Messaging (Session 1)
- Formulated and implemented the architectural decision matrix for inter-service communication across the 18 repositories:
  - **Synchronous REST/JSON over HTTP:** Reserved strictly for external ingress from browser microfrontends to `educk-api-gateway` (:8080) and immediate read queries where the caller must receive the immediate state (e.g. user login, conversation thread retrieval, attendance report viewing).
  - **Asynchronous Event-Driven Messaging:** Engineered for all cross-domain state mutations and side effects (e.g. `GradeCreated`, `StudentAbsent`). Eliminates temporal coupling between bounded contexts: if the notification email/SMS provider is unreachable, the teacher's grading transaction succeeds instantly with zero latency penalty.
- Proactively prevented synchronous cascade anti-patterns (e.g. `Academic -> Notification -> Worker -> External SMS` chained calls) that exhaust thread pools and cause cascading timeouts across the cluster.

### Asynchronous Resilience & Transactional Outbox Pattern (`educk-academic-api` & `educk-worker`)
- Implemented the **Transactional Outbox Pattern** (ADR-004, ADR-007) in `educk-academic-api` to guarantee eventual consistency between PostgreSQL persistence (`:5432`) and RabbitMQ message dispatch (`:5672`):
  - Grade persistence and outbox event staging execute inside a single local ACID transaction in PostgreSQL.
  - Completely avoided distributed Two-Phase Commit (2PC / XA), eliminating synchronous blocking coordinators and single points of failure.
  - Outbox publisher reads unprocessed rows (`status = 'PENDING'`) and flushes them to RabbitMQ topic exchange `edutrack.events` with routing key `academic.grade.created`.
- Engineered **Consumer Idempotency** in `educk-worker` to survive network duplicates under at-least-once broker delivery:
  - Incoming messages carry a unique UUID `eventId`.
  - The worker performs atomic deduplication using Redis (`SETNX key value EX 86400`). Duplicate deliveries are acknowledged and discarded without re-sending parent notifications.
- Configured a **Dead Letter Queue (DLQ)** topology:
  - Exchange: `edutrack.events` (Topic Exchange).
  - Primary Queue: `notifications.queue` (bound to `academic.*` and `attendance.*`).
  - Dead Letter Exchange: `edutrack.dlx` routing to `notifications.dlq`.
  - Unprocessable or poison messages exceeding max retry thresholds (3 attempts with exponential backoff) are routed to `notifications.dlq` for manual operator inspection without stalling the pipeline.

### Perimeter Security Gateway & Token Blacklist (ADR-008)
- Configured Spring Cloud Gateway (`educk-api-gateway` on `:8080`) as the single entry point:
  - Centralized authentication filter verifying RS256 asymmetric JWT signatures against public keys issued by `educk-identity-api` (:8081).
  - Enforces Redis token blacklist check on every authenticated request to guarantee immediate session invalidation upon user logout (`NFR-003`).
  - Enriches downstream requests with trusted context headers (`X-User-Id`, `X-User-Role`, `X-Correlation-Id`). Downstream services reside inside private Docker network `edutrack-net`, eliminating redundant cryptographic JWT validation overhead across internal microservices.

### Distributed Attendance Causal Ordering (HU-005 & `educk-attendance-api`)
- Solved the out-of-order event arrival hazard inherent in distributed networks:
  - Integrated monotonic Lamport timestamps and UTC session sequence numbers into the `StudentAbsent` event payload.
  - Ensured that if a student is marked absent at 07:05 AM and corrected to excused at 07:20 AM, out-of-order network arrival at `educk-worker` will not overwrite the later excused status with a stale absence notification.

### Versioned Contract Governance & Compatibility Rules (Session 2)
- Consolidated and verified machine-readable OpenAPI 3.0 contracts in `educk-docs/07-api/contracts/openapi/`:
  - `identity-service.yaml`, `academic-service.yaml`, `attendance-service.yaml`, and `communication-service.yaml`.
  - Enforced strict backward-compatibility rules: adding optional fields is non-breaking within the same minor version; removing, renaming, or changing field types requires bumping the major API path (e.g. `/api/v2`) and emitting a `Sunset` HTTP header for deprecation.
  - Standardized the global error response envelope:
    ```json
    {
      "error": {
        "code": "RESOURCE_NOT_FOUND",
        "message": "Student enrollment record not found",
        "details": ["student_id=550e8400-e29b-41d4-a716-446655440000"],
        "trace_id": "c843226-7a71-41d4"
      }
    }
    ```

---

## 3. Blockers and risks

- **Risk — Synchronous Service Cascading:** If frontend portals make direct REST calls to multiple backend services simultaneously, network blips or slow database queries can trigger cascading connection pool exhaustion.
  - *Mitigation:* Ingress traffic is strictly mediated by `educk-api-gateway` with configured request timeouts (2500ms), circuit breaker fallbacks, and internal state changes choreographed asynchronously via RabbitMQ.
- **Risk — Message Duplication & Out-of-Order Delivery:** Broker retries and network partitions inherently cause message duplication (at-least-once delivery).
  - *Mitigation:* Engineered mandatory idempotency checks in `educk-worker` via Redis key caching (`eventId` with 24h TTL) and monotonic Lamport sequence validation for attendance status updates.
- **Risk — Contract Drift Across Repositories:** Independent teams modifying DTOs could silently break inter-service communication or microfrontend consumer portals.
  - *Mitigation:* Enforced contract-first OpenAPI specifications stored in `educk-docs` (Single Source of Truth) with automated CI validation before pull request merges.

---

## 4. Plan for next week

- Transition to **Week 08: Agile & DevOps for distributed teams**:
  - Deploy automated CI/CD quality gate pipelines across all 18 repositories with strict `< 400 lines` diff ceiling and 80% unit test coverage thresholds.
  - Implement Consumer-Driven Contract Testing (Pact / Spring Cloud Contract) between API Gateway and microservice endpoints to detect breaking changes in pull requests automatically.
  - Execute the **Cut 2 Checkpoint Review Defense** and oral examination with evaluator `@ariel5253`.
  - Validate zero-downtime container promotion across `develop`, `qa`, and `main` environments.

---

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

---

## 6. Evidence links

- Repository: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1
- Week 07 Deliverable: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1/tree/main/07-week/hu-status
- Checkpoint Review Defense Cut 2: https://github.com/XimenaChala/educk-docs/blob/main/15-project-control/checkpoint-review-defense-corte-2.md
- Inter-Service Communication Matrix: [inter-service-communication-matrix.md](./inter-service-communication-matrix.md)
- Unified Infrastructure Stack: [docker-compose.unified.yml](./docker-compose.unified.yml)
- Infrastructure Repository (`educk-infra`): https://github.com/code-corhuila/educk-infra
- Identity API Repository (`educk-identity-api`): https://github.com/code-corhuila/educk-identity-api
- Academic API Repository (`educk-academic-api`): https://github.com/code-corhuila/educk-academic-api
- Attendance API Repository (`educk-attendance-api`): https://github.com/code-corhuila/educk-attendance-api
- Communication API Repository (`edutrack`): https://github.com/code-corhuila/edutrack
- Worker Notifications Repository (`educk-worker`): https://github.com/code-corhuila/educk-worker
- API Gateway Repository (`educk-api-gateway`): https://github.com/code-corhuila/educk-api-gateway
- Contract Catalog: https://github.com/code-corhuila/educk-docs/tree/main/07-api/contracts/openapi
- ADR-004 Event-Driven Architecture: https://github.com/code-corhuila/educk-docs/blob/main/05-architecture/decisions/records/ADR-004-event-driven-architecture-rabbitmq.md
- ADR-007 RabbitMQ Event Bus & DLQ: https://github.com/code-corhuila/educk-docs/blob/main/05-architecture/decisions/records/ADR-007-rabbitmq-event-bus.md
- ADR-008 API Gateway Authentication Filter: https://github.com/code-corhuila/educk-docs/blob/main/05-architecture/decisions/records/ADR-008-api-gateway-authentication-filter.md

---

## 7. Session notes — Inter-service communication & Versioned Contracts

### 1. Synchronous vs Asynchronous Tradeoffs (Session 1)
* **Synchronous (REST/HTTP, gRPC):** Caller thread blocks waiting for callee response. Simpler cognitive model, but tightly couples services in time. If a callee experiences latency or outages, the caller degrades. Suitable for query read models and client-facing gateways.
* **Asynchronous (RabbitMQ, Kafka):** Caller publishes an event or command message to a broker and immediately resumes execution. Services are completely decoupled in time, absorbing traffic spikes and surviving receiver downtime at the cost of eventual consistency.

### 2. Delivery Semantics & Idempotent Processing
* Over unreliable networks, "exactly-once delivery" is mathematically impossible due to the Two Generals Problem. Brokers provide **at-least-once delivery** (re-transmitting unacknowledged packets).
* Distributed systems must engineer **exactly-once processing**:
  $$\text{Exactly-Once Processing} = \text{At-Least-Once Delivery} + \text{Idempotency Key} + \text{Deduplication Cache}$$
* Consumers use atomic Redis keys (`SETNX eventId`) and database unique constraints to safely discard redundant messages.

### 3. The Contract as the Single Source of Truth (Session 2)
* An API or event specification is not code documentation; the **versioned, machine-readable contract** (`openapi.yaml`, `.proto`, JSON Schema) is the canonical source of truth from which clients, servers, and tests are derived.
* Every complete contract declares 5 foundational elements:
  1. **Method + Path / Event Routing Key**
  2. **Request Schema & Headers** (including `Idempotency-Key` and `X-Correlation-Id`)
  3. **Response Schema**
  4. **Standardized Error Envelope** (`code`, `message`, `details`, `trace_id`)
  5. **Explicit Version Identifier** (`/api/v1`)

### 4. Backward Compatibility & Evolution Rules
* Services deploy independently; consumers cannot be updated simultaneously.
* **Safe Changes (Non-breaking):** Adding optional fields, adding new endpoints, expanding enum values in responses.
* **Breaking Changes:** Renaming fields, deleting fields, changing data types, or adding mandatory request parameters.
* Breaking changes strictly demand a new API version (`/api/v2`) and deprecation of the older version with standard `Sunset: <date>` HTTP headers.

### 5. Consumer-Driven Contract Testing (Pact)
* Unit tests verify internal logic in isolation; they fail to catch inter-service integration drift.
* Contract testing allows the consumer to publish its expectations (a contract/pact). In CI, the producer's build validates against all consumer pacts before deployment, turning cross-service breakages into immediate red builds.
