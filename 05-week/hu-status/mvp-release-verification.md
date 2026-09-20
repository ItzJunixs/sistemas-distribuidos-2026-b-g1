# EduTrack MVP 1 - Release Verification & Definition of Done (DoD)

## 1. System Architecture & Components (Standard: `<abbr>-<domain>-<piece>`)
- **Frontend Portal (`educk-comm-portal`):** Container running Nginx Alpine serving Vite web application on port 3000.
  - Repository: [`code-corhuila/educk-communication-portal`](https://github.com/code-corhuila/educk-communication-portal) (Branch: `feat/HU-005-interfaz-comunicacion`).
  - Implements Figma design system (Deep Navy `#0f172a`, Royal Blue `#3b82f6`, Inter font).
  - Connects dynamically to backend API at `http://localhost:8085/api/v1/messages`.
- **Backend Service API (`educk-communication-api`):** Container running Java 21 LTS + Spring Boot 3.3.3 on port 8085.
  - Repository: [`code-corhuila/edutrack`](https://github.com/code-corhuila/edutrack) (Branches: `feat/HU-005-comunicacion-padre-profesor`, `develop`, `qa`).
  - **Component Naming Harmonization (`<abbr>-<domain>-<piece>`):** The canonical component name is `educk-communication-api` (standardized across Docker container names and `09-microservices/service-catalog.md`). The university base template repository `edutrack` serves as the implementation host for this component (`edutrack == educk-communication-api`). Internally, source code is strictly scoped to the communication bounded context (`com.edutrack.communication.*`), housing zero academic or attendance logic, ensuring single-domain isolation and strict adherence to ADR-003.
  - Hexagonal architecture (Domain, Application, Infrastructure).
  - Endpoints exposed: `POST /api/v1/messages` and `GET /api/v1/messages/conversation`.
- **Database Engine (`educk-comm-db`):** Container running PostgreSQL 16 Alpine on internal port 5432 / host port 5433.
  - Dedicated database `communication_db`, schema user `edutrack_admin`.
  - Repository: [`code-corhuila/educk-communication-db`](https://github.com/code-corhuila/educk-communication-db) (Branch: `feat/HU-005-esquema-mensajes`).
  - **Schema Ownership (Course Standard `<abbr>-<domain>-db`):** Adheres strictly to ADR-003 (Database per Service) and course governance: schema DDL definitions and versioned migrations belong authoritatively to the database repository `educk-comm-db` (isolated from the API artifact `educk-comm-api`). For the local walking skeleton, initialization DDL scripts (`migrations/V1__create_messages_table.sql`) are mounted directly into PostgreSQL container startup (`/docker-entrypoint-initdb.d/`), decoupling schema control from the Spring Boot JAR.

## 2. Multi-Repository Architecture Clarification
> **Note on Multi-Repository Strategy (Polyrepo):**  
> - `code-corhuila/sistemas-distribuidos-2026-b-g1`: Acts as the **coursework portfolio & tracking umbrella** (weekly logs, rulesets, evidence snapshots, docker-compose wrappers).  
> - Application source code, hexagonal domain models, JUnit 5 test suites, and frontend components live in dedicated repositories within the official organization ([`code-corhuila/edutrack`](https://github.com/code-corhuila/edutrack), [`code-corhuila/educk-communication-portal`](https://github.com/code-corhuila/educk-communication-portal), and [`code-corhuila/educk-communication-db`](https://github.com/code-corhuila/educk-communication-db)) to ensure decoupled CI/CD pipelines, independent versioning, and zero cross-service contamination.

## 3. Definition of Done (DoD) Verification
- [x] **Source Code & Branching:**
  - Feature branches: `feat/HU-005-comunicacion-padre-profesor` and `feat/HU-005-interfaz-comunicacion`.
  - Protection ruleset `protect-develop-main` active on GitHub: enforces 1 peer approval, conversation resolution, stale review dismissal, and **zero author bypasses**.
- [x] **Zero Secrets in Repository & Compose:**
  - Docker Compose enforces strict variable presence via `${VARIABLE:?error}` syntax. No hardcoded default credentials exist in tracked YAML files.
  - All credentials injected at runtime via local `.env` (template committed as `.env.example`).
  - GitGuardian automated security scan: PASSED (Green, 0 secrets detected).
- [x] **Automated Tests & Traceability:**
  - JUnit 5 domain, application, concurrency, and integration tests passing (7/7 passed, 0 failures, 0 errors).
  - Test files in source tree:
    - Domain: `src/test/java/com/edutrack/communication/domain/MessageTest.java` (3 unit tests for invariants).
    - Application: `src/test/java/com/edutrack/communication/application/SendMessageServiceTest.java` (1 service workflow test).
    - Concurrency: `src/test/java/com/edutrack/communication/application/ConcurrentMessageSubmissionTest.java` (1 multi-threaded race condition test).
    - Integration: `src/test/java/com/edutrack/communication/infrastructure/web/MessageControllerIntegrationTest.java` (2 MockMvc REST endpoint integration tests).
  - Traceability: Snapshot results recorded in [`test-execution-results.txt`](./test-execution-results.txt) and verifiable directly in the source repository.
- [x] **Containerization:**
  - Multi-stage Dockerfiles (`maven:3.9-eclipse-temurin-21` -> `eclipse-temurin:21-jre`) with `.dockerignore`.
  - Orchestrated via Docker Compose with healthchecks (`pg_isready`) and bridge network isolation.
- [x] **Live System Execution:**
  - Frontend accessible at `http://localhost:3000`.
  - Backend API accessible at `http://localhost:8085`.
  - Message sent from frontend UI is persisted in PostgreSQL and returned with status 201 Created.
