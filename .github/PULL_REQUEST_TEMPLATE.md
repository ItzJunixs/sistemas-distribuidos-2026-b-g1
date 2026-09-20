## Pull Request — Team G1 (EduTrack / educk)

> **Golden Rule:** Every PR must have a diff under **400 lines**, strictly follow `<abbr>-<domain>-<piece>`, and have passing automated tests.

### 1. General Information
- **User Story / Task:** HU-___
- **Type of Change:**
  - [ ] `feat`: New feature or capability
  - [ ] `fix`: Bug fix
  - [ ] `test`: New or updated unit/integration tests
  - [ ] `refactor`: Code refactoring without behavioral change
  - [ ] `docs`: Documentation or contract updates
  - [ ] `chore`: Build, tooling, Docker, dependencies

### 2. Summary of Changes
<!-- Briefly explain what was implemented and why (technical rationale) -->

---

### 3. Mandatory Quality Gates
*Mark each checkbox with [x]. All items are mandatory for PR approval:*

- [ ] **Size Cap:** PR diff is strictly under **400 lines of code**.
- [ ] **Branching Policy:** PR originates from a child branch (`feat/HU-XXX-...`, `fix/...`, `docs/...`) targeting `develop` (or `main` in single-main documentation repos). Direct pushes to permanent branches are strictly prohibited.
- [ ] **Conventional Commits:** All commit messages are in English and strictly follow `type(scope): summary` with mandatory `Why:` body rationale.
- [ ] **Zero Secrets:** No passwords, tokens, API keys, or live `.env` credentials are committed.
- [ ] **Automated Tests:** Code compiles cleanly and unit/integration tests pass with 0 failures (`mvn test` / `npm test`).
- [ ] **Hexagonal Architecture (Backend):** The `domain/` layer is pure Java (zero frameworks, zero SQL/JPA imports).
- [ ] **Database Sovereignty (ADR-003):** Schema migrations are owned exclusively in dedicated `-db` repositories. No physical cross-database foreign keys.
- [ ] **Component Naming:** Container names and repositories adhere to `<abbr>-<domain>-<piece>`. Health checks use genuine readiness signals (`condition: service_healthy` via `pg_isready`).
- [ ] **English Standard (ADR-001):** Code symbols, comments, commit messages, and technical documentation are 100% in English.
