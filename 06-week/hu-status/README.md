<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       06-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 06

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Defend Cut 1 Walking Skeleton, resolve technical reviewer findings (@ariel5253), establish canonical requirement traceability (HU-004 vs HU-005), harden Hexagonal Architecture and Database Idempotency, and deploy 24/7 Autonomous Semantic Quality Gates across all microservices for Sprint 2.
<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-004 | Parent-Teacher Communication Service Walking Skeleton Defense, Hexagonal Decoupling (`GetConversationUseCase`), and Database Idempotency Guard | done | Core API: [`code-corhuila/edutrack` (PR #7, commit `d2576da`)](https://github.com/code-corhuila/edutrack) · DB Schema: [`code-corhuila/educk-communication-db` (Flyway V2)](https://github.com/code-corhuila/educk-communication-db) · UI Portal: [`code-corhuila/educk-communication-portal`](https://github.com/code-corhuila/educk-communication-portal) |
| HU-005 | Attendance Registration & Causal Ordering Domain Specification & Architecture Contract | doing | Architectural Spec: [`09-microservices/services/05-attendance/`](https://github.com/code-corhuila/educk-docs/tree/main/09-microservices/services/05-attendance) · Contract: [`07-api/contracts/openapi/attendance-service.yaml`](https://github.com/code-corhuila/educk-docs/tree/main/07-api/contracts/openapi/attendance-service.yaml) · Implementation Target: `educk-attendance-api` :8083 (Cut 2) |
| HU-DEFENSE-C1 | Cut 1 Checkpoint Review Defense & Full Resolution of Reviewer Findings (@ariel5253) | done | Master Defense Audit Log: [`15-project-control/checkpoint-review-defense-corte-1.md`](https://github.com/XimenaChala/educk-docs/blob/main/15-project-control/checkpoint-review-defense-corte-1.md) · PR #4 (`docs/corte-1-review-defense` merged to `main`) · PR Approval by `@ariel5253` |
| HU-TRACE-RECON | Traceability Matrix Reconciliation & HU ID Disambiguation (HU-004 vs HU-005) | done | [`04-requirements/traceability-matrix.md`](https://github.com/XimenaChala/educk-docs/blob/main/04-requirements/traceability-matrix.md) · Narrative alignment in [`04-requirements/user-stories.md`](https://github.com/XimenaChala/educk-docs/blob/main/04-requirements/user-stories.md) |
| HU-AUTO-C1 | Autonomous 24/7 Cloud Quality Gate Automation & Dynamic Architecture Spec Engine from `educk-docs` | done | [`.github/workflows/centinela-cloud.yml`](https://github.com/XimenaChala/educk-docs/blob/main/.github/workflows/centinela-cloud.yml) · [`.github/scripts/centinela_cloud.py`](https://github.com/XimenaChala/educk-docs/blob/main/.github/scripts/centinela_cloud.py) · [`architecture_spec.json`](https://github.com/XimenaChala/educk-docs/blob/main/.github/architecture_spec.json) |

---

## 2. My individual contribution

### Cut 1 Checkpoint Defense & Reviewer Findings Resolution (Prof. Ariel @ariel5253)
- Evaluated and addressed all four explicit written recommendations issued by Course Tech Lead `@ariel5253` upon officially approving the Cut 1 Pull Request in `educk-docs`:
  1. **Pull Request Size Cap Exception & Branching Model:** Documented the architectural bootstrap exception for the initial +7615 line PR (necessary to prevent circular link breakages across 15 folders simultaneously) and formally locked the strict `< 400 lines` threshold for all subsequent sprints. Standardized the single-main branching model for `-docs` repositories, ensuring changes are fed strictly by child branches (`docs/<topic-slug> -> main`). Demonstrated compliance by cutting child branch `docs/corte-1-review-defense` and merging via PR #4.
  2. **Security & Zero-Secrets Remediation:** Completely stripped exposed fallback Trello keys/tokens from `.github/scripts/centinela_cloud.py` and `.github/workflows/centinela-cloud.yml`. Re-architected scripts to ingest credentials strictly from GitHub Actions Secrets (`${{ secrets.TRELLO_KEY }}` / `${{ secrets.TRELLO_TOKEN }}`) with graceful runtime skipping if omitted.
  3. **Requirement Traceability Reconciliation (HU-004 vs HU-005):** Established a formal cross-reference and disambiguation section in `04-requirements/traceability-matrix.md`. Clarified that Canonical **`HU-004`** = Parent-Teacher Communication (`FR-005`, `communication-service` :8085, delivered in Cut 1) and Canonical **`HU-005`** = Attendance Tracking & Causal Ordering (`FR-003`, `attendance-service` :8083, scheduled for Cut 2). Formally linked legacy git branches named `feat/HU-005-*` as implementation evidence for Canonical `HU-004`.
  4. **ADR-001 English Documentation Compliance:** Translated `.github/PULL_REQUEST_TEMPLATE.md` and `.github/scripts/centinela_cloud.py` 100% into technical English, strictly aligning tooling and templates with the English Documentation Standard.
- Authored the comprehensive Checkpoint Review Defense and Oral Exam Playbook in `15-project-control/checkpoint-review-defense-corte-1.md` containing complete root cause analyses (RCAs) and the 8-question oral examination defense script for the team.

### Backend Hexagonal Architecture Refactoring (`edutrack`)
- Addressed Finding 12 from backend review: eliminated driving-adapter bypass where `MessageController` directly referenced `MessageRepository` for `GET /conversation`.
- Created inbound port `com.edutrack.communication.domain.port.in.GetConversationUseCase` in domain layer.
- Implemented `com.edutrack.communication.application.service.GetConversationService` encapsulating retrieval logic and pagination boundaries.
- Refactored `MessageController` to inject strictly inbound use cases (`SendMessageUseCase` and `GetConversationUseCase`), achieving 100% Hexagonal purity.
- Expanded unit and integration test coverage from 7 to 8 passing tests (`BUILD SUCCESS`) with Mockito mocks.
- Resolved database user fallback in `src/main/resources/application.yml` via composite property fallback:
  ```yaml
  username: ${DB_USER:${DB_USERNAME:${POSTGRES_USER:postgres}}}
  ```
- Fixed IntelliJ `.gitignore` rule from `out/` to `/out/` to prevent silent exclusion of `domain/port/out/MessageRepository.java`.

### Relational Database Idempotency Hardening (`educk-communication-db`)
- Created Flyway migration `migrations/V2__add_idempotency_and_constraints.sql` while preserving `V1__create_messages_table.sql` checksum immutability.
- Added `idempotency_key VARCHAR(64)` and `status VARCHAR(20) NOT NULL DEFAULT 'SENT'`.
- Created partial unique index `uq_messages_sender_idempotency` on `(sender_id, idempotency_key)` to guarantee database-level duplicate rejection upon HTTP retries.
- Added domain invariant check constraint `chk_messages_distinct_participants CHECK (sender_id <> receiver_id)`.
- Sanitized `.env.example` to enforce "Cero Secretos" policy by removing realistic password strings.
- Replaced grading seed single-row artifact with realistic multi-thread test fixtures in `seeds/01_seed_test_messages.sql`.

### Dynamic Architecture Specification Engine & 24/7 Cloud Guardian
- Architected and implemented `extractor_especificaciones_docs.py` which dynamically parses `09-microservices/service-catalog.md`, `data-model.md`, `events.md`, and OpenAPI contracts directly from `educk-docs` (Single Source of Truth), compiling them into `.github/architecture_spec.json`.
- Implemented `auditor_semantico_repositorio.py` which reads source code, Flyway migrations, and Spring Boot `application.yml` files in any repository to verify conformity against the architecture specification.
- Enhanced `centinela_cloud.py` across all 18 microservice repositories:
  - Validates PR line limits (<400 lines), commit `Why:`, and branching rules.
  - Automatically identifies domain leaks (e.g. academic service defining communication tables).
  - Validates port numbers and database names against the official catalog.
  - Posts actionable correction steps on Trello with interactive checkboxes (`🛠️ Correcciones Requeridas`).
  - Dispatches automated WhatsApp alerts with detailed bullet points to team members.

---

## 3. Blockers and risks

- **Risk:** Cross-domain entity coupling during Cut 2 IAM integration. As other microservices (`educk-academic-api`, `educk-attendance-api`) spin up, developers might attempt to create direct foreign keys to `identity.users` instead of maintaining logical UUID references, violating ADR-003 (Database per Service).
  - *Mitigation:* The newly deployed `auditor_semantico_repositorio.py` detects foreign key definitions that cross database boundaries and immediately rejects PRs with prescriptive guidance.
- **Risk:** External API rate limits on GitHub and Trello when 18 repositories run CI concurrently.
  - *Mitigation:* Ingested secrets via GitHub Actions Secrets and implemented resilient exception handling with exponential backoff and local cache fallbacks in `centinela_cloud.py`.
- **Risk:** Divergence of team members' local Git branches from the canonical `educk-docs` specification.
  - *Mitigation:* Configured the dynamic spec engine so that every push to `main` updates the shared `.github/architecture_spec.json`, giving developers instant feedback on non-conforming changes.

---

## 4. Plan for next week

- Kick off Sprint 2 (Corte 2) focusing on Identity & Access Management (`educk-identity-api`) and perimetral routing (`educk-api-gateway`).
- Implement `educk-identity-db` Flyway migrations for `users`, `roles`, `schools`, and `parent_child_link`.
- Build Spring Security JWT token issuance and RBAC claims filter (`SUPER_ADMIN`, `ADMIN`, `TEACHER`, `PARENT`, `STUDENT`).
- Configure Spring Cloud Gateway (:8080) reverse proxy routes forwarding `/api/v1/communication/**` to `:8085` and `/api/v1/auth/**` to `:8081`.
- Provision RabbitMQ message broker container in `educk-infra` with exchange `edutrack.events` to prepare for asynchronous messaging between Academic (`GradeCreated`) and Notifications.
- Maintain the strict `< 400 lines` PR cap and ensure all feature branches follow the per-environment promotion model (`develop -> release -> main`).

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
- Week 06 Deliverable: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1/tree/main/06-week/hu-status
- Master Defense Document: https://github.com/XimenaChala/educk-docs/blob/main/15-project-control/checkpoint-review-defense-corte-1.md
- Traceability Reconciliation Matrix: https://github.com/XimenaChala/educk-docs/blob/main/04-requirements/traceability-matrix.md
- Checkpoint PR #4 Approval (`@ariel5253`): https://github.com/XimenaChala/educk-docs/pull/4
- Governance PR #5 (Trello Checklist & PR Reviews): https://github.com/XimenaChala/educk-docs/pull/5
- Dynamic Spec Engine PR #6 (Semantic Auditor): https://github.com/XimenaChala/educk-docs/pull/6
- Core Backend Walking Skeleton: https://github.com/code-corhuila/edutrack
- Database Schema Repository (Flyway): https://github.com/code-corhuila/educk-communication-db
- Frontend Portal (Vite/React): https://github.com/code-corhuila/educk-communication-portal
- Official Documentation Catalog: https://github.com/code-corhuila/educk-docs/blob/main/09-microservices/service-catalog.md

---

## 7. Session notes — Cut 1 Defense & Technical Governance Playbook

### 1. Checkpoint Review Defense Golden Rule
Under the course evaluation policy, reviewer recommendations are not blockers but auditable quality trails. At the checkpoint defense, the team is asked what was done with each recommendation:
* *Applied and how:* Explain the code, migration, or documentation modification executed.
* *Did not apply and why:* Provide a sound, technical architectural justification (e.g. bootstrap baseline exception).
* *Unacceptable condition:* Not having considered or analyzed the reviewer's finding.

### 2. Pure Hexagonal Architecture Principles
A driving adapter (such as a Spring `@RestController`) must **never** hold a direct reference to a driven outbound port (`MessageRepository`). Doing so bypasses the application service layer, preventing domain invariant validation, authorization checks, and transaction management. The web controller must interact exclusively with inbound use case interfaces (`SendMessageUseCase`, `GetConversationUseCase`).

### 3. Database per Service & Idempotency Guarantees
In distributed systems, eventual consistency and network unreliability demand that message publishing and persistence be idempotent:
* Each client request carries an `Idempotency-Key` header.
* The database enforces a partial unique index `(sender_id, idempotency_key)` preventing duplicate inserts upon network timeouts and retries.
* Relational check constraints (`sender_id <> receiver_id`) enforce core domain invariants directly at the persistence tier as defense-in-depth.
