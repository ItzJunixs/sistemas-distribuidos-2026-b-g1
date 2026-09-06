<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       03-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 03

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Design and structure the centralized software documentation repository (SSOT) across folders 00–12, establish governance rules, and define domain models for EduTrack.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-005 | Setup documentation repository architecture (00-12 folders) | done | [domain-map.md](./domain-map.md), [definition-of-done.md](./definition-of-done.md) |
| HU-XXX-006 | Document domain models, entities, and Architecture Decision Records (ADR-001 to ADR-004) | done | [ADR-001](./ADR-001-documentation-language.md), [ADR-002](./ADR-002-backend-stack.md), [ADR-003](./ADR-003-database-per-service.md), [ADR-004](./ADR-004-message-broker.md), [entities-and-rules.md](./entities-and-rules.md) |

## 2. My individual contribution
- Created and organized the `educk-docs` documentation repository adhering to the 00–15 standard (Single Source of Truth).
- Established `00-governance/documentation-rules.md`, `definition-of-done.md`, and `git-conventions.md`.
- Formulated Architecture Decision Records:
  - `ADR-001`: Documentation language standard (100% English).
  - `ADR-002`: Backend stack selection (Java 21, Spring Boot 3, Hexagonal Architecture).
  - `ADR-003`: Persistence strategy (Database per Service with PostgreSQL 16).
  - `ADR-004`: Message broker selection (RabbitMQ Topic Exchange `edutrack.events`).
- Defined bounded contexts and domain invariants in `02-domain/`:
  - Identity & Accounts (`identity-service` / `identity_db`)
  - Academic Records (`academic-service` / `academic_db`)
  - Attendance (`attendance-service` / `attendance_db`)
  - Notifications (`notifications-service` / `notifications_db`)
  - Communication (`communication-service` / `communication_db`)
- Modeled the system data dictionary, schema constraints, and Flyway migration strategy in `06-data/`.

## 3. Blockers and risks
- Coordinating strict separation of database boundaries across the team to prevent unintentional foreign-key joins.
- Maintaining 100% English across all documentation files as mandated by ADR-001.

## 4. Plan for next week
- Formulate functional User Stories (`HU-001` through `HU-005`) with Given-When-Then acceptance criteria.
- Define OpenAPI contracts and RabbitMQ event catalog envelopes.
- Construct UML C4 container diagrams and design high-fidelity Figma UI wireframes.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence files & links
- Local Evidence Files:
  - ADR-001 Documentation Language: [`ADR-001-documentation-language.md`](./ADR-001-documentation-language.md)
  - ADR-002 Backend Stack: [`ADR-002-backend-stack.md`](./ADR-002-backend-stack.md)
  - ADR-003 Database Per Service: [`ADR-003-database-per-service.md`](./ADR-003-database-per-service.md)
  - ADR-004 Message Broker: [`ADR-004-message-broker.md`](./ADR-004-message-broker.md)
  - Bounded Contexts Map: [`domain-map.md`](./domain-map.md)
  - Domain Entities & Rules: [`entities-and-rules.md`](./entities-and-rules.md)
  - Definition of Done: [`definition-of-done.md`](./definition-of-done.md)
- Remote Repositories:
  - Documentation Repository: https://github.com/XimenaChala/educk-docs
  - Course Tracking Repository: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1
