# EduTrack MVP 1 - Release Verification & Definition of Done (DoD)

## 1. System Architecture & Components
- **Frontend Service:** Container `edutrack-frontend` running Nginx Alpine serving Vite web application on port 3000.
  - Implements Figma design system (Deep Navy `#0f172a`, Royal Blue `#3b82f6`, Inter font).
  - Connects dynamically to backend API at `http://localhost:8085/api/v1/messages`.
- **Backend Service:** Container `edutrack-communication` running Java 21 LTS + Spring Boot 3.3.3 on port 8085.
  - Hexagonal architecture (Domain, Application, Infrastructure).
  - Endpoints exposed: `POST /api/v1/messages` (with validation and persistence).
- **Database:** Container `edutrack-postgres` running PostgreSQL 16 Alpine on internal port 5432 / host port 5433.
  - Database `communication_db`, user `edutrack_admin`.
  - Flyway migrations automatically run on startup.

## 2. Definition of Done (DoD) Verification
- [x] **Source Code & Branching:**
  - Feature branches: `feat/HU-005-comunicacion-padre-profesor` and `feat/HU-005-interfaz-comunicacion`.
  - Merged into `develop`. Ruleset `protect-develop-main` configured on GitHub (bypass enabled for Ximena Zambrano, automated/professor evaluation).
- [x] **Zero Secrets:**
  - Database credentials managed via `.env` / `.env.example`.
  - GitGuardian automated security scan: PASSED (Green).
- [x] **Automated Tests:**
  - JUnit 5 domain and application tests passing (4/4 passed, 0 failures, 0 errors).
- [x] **Containerization:**
  - Multi-stage Dockerfiles for frontend and backend.
  - Orchestrated via Docker Compose with healthchecks and isolated network.
- [x] **Live System Execution:**
  - Frontend accessible at `http://localhost:3000`.
  - Backend API accessible at `http://localhost:8085`.
  - Message sent from frontend UI is persisted in PostgreSQL and returned with status 201 Created.
