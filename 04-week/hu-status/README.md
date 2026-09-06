<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       04-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 04

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Specify technical requirements, API contracts (OpenAPI), event catalogs, UML architecture diagrams, and high-fidelity UX/UI wireframes for EduTrack.
<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-XXX-007 | Specify OpenAPI contracts, event catalog, and data ownership matrix | done | [communication-service.yaml](./communication-service.yaml), [api-gateway.yaml](./api-gateway.yaml), [traceability-matrix.md](./traceability-matrix.md) |
| HU-XXX-008 | Produce UML C4 architecture diagrams and Figma UX/UI design specifications | done | [c1-system-context.mmd](./c1-system-context.mmd), [c2-container-microservices.mmd](./c2-container-microservices.mmd), [seq-grade-recorded.mmd](./seq-grade-recorded.mmd), [user-stories.md](./user-stories.md) |

## 2. My individual contribution
- Formulated User Stories `HU-001` to `HU-005` with testable Given-When-Then scenarios and constructed the complete Requirements Traceability Matrix (`04-requirements/traceability-matrix.md`).
- Authored OpenAPI 3.0 specifications for backend microservices:
  - `api-gateway.yaml` (Edge routing on port 8080)
  - `auth-service.yaml` / `identity-service.yaml` (Authentication and user management on port 8081)
  - Contracts for `academic-service` (8082), `attendance-service` (8083), `notifications-service` (8084), `communication-service` (8085).
- Created the asynchronous Event Catalog (`09-microservices/event-catalog.md`) documenting event schemas, versioning policies, and deduplication logic for `GradeCreated` and `StudentAbsent`.
- Authored the Data Ownership Matrix (`09-microservices/data-ownership-matrix.md`) and Inter-Service Communication Patterns (`09-microservices/communication-patterns.md`).
- Designed UML C4 architecture diagrams in Mermaid:
  - C1 System Context (`c1-context.mmd`)
  - C2 Container Microservices diagram (`c2-container-microservices.mmd`)
  - Sequence diagrams for grade recording (`seq-grade-recording.mmd`) and absence alerts (`seq-attendance-alert.mmd`).
- Created high-fidelity UX/UI wireframes and interactive prototype in Figma (`EduTrack-Mockups-MVP1.fig`) covering the 7 core screens and complete Design System (`12-ux-ui/`).

## 3. Blockers and risks
- Ensuring consistency across OpenAPI YAML contracts and Spring Boot controller request/response models.
- Handling race conditions and at-least-once message delivery in the notifications service via idempotency keys.

## 4. Plan for next week
- Containerize all services with Docker (multi-stage Dockerfile, .dockerignore, docker-compose.yml).
- Prepare and ship MVP 1 release: promote branches to main, tag `v1.0.0`, verify release checklist / DoD, and conduct system demo and retrospective.

## 5. Compliance self-check
- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence files & links
- Local Evidence Files:
  - OpenAPI API Gateway: [`api-gateway.yaml`](./api-gateway.yaml)
  - OpenAPI Communication Service: [`communication-service.yaml`](./communication-service.yaml)
  - Traceability Matrix: [`traceability-matrix.md`](./traceability-matrix.md)
  - User Stories Specification: [`user-stories.md`](./user-stories.md)
  - C1 System Context Diagram: [`c1-system-context.mmd`](./c1-system-context.mmd)
  - C2 Container Diagram: [`c2-container-microservices.mmd`](./c2-container-microservices.mmd)
  - Grade Recorded Sequence Diagram: [`seq-grade-recorded.mmd`](./seq-grade-recorded.mmd)
- Remote Repositories & Prototypes:
  - Interactive Figma Prototype: https://www.figma.com/proto/qp63u70WB8yFjlzbfZcLR9/EduTrack-Mockups-MVP-1
  - Documentation Repository: https://github.com/XimenaChala/educk-docs
  - Course Tracking Repository: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1
