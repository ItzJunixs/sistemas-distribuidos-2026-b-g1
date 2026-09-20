<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
    Your weekly grade is read AUTOMATICALLY from this file:
      05-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Ximena Del Pilar Zambrano Chala
- GITHUB_USER: XimenaChala
- TEAM: G1
- SPRINT_GOAL: Containerize the EduTrack services with Docker (multi-stage Dockerfile, .dockerignore, docker-compose.yml) and ship MVP 1 — promote to main, tag v1.0.0, verify the release checklist/DoD, demo the running system, and run the retrospective.
<!-- CONFIG-END -->

## 1. User stories worked this week

| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-004 | Parent-Teacher Communication Service Backend API & Frontend UI (Walking Skeleton MVP 1) | done | Backend: [`code-corhuila/edutrack`](https://github.com/code-corhuila/edutrack/tree/feat/HU-005-comunicacion-padre-profesor) · Frontend: [`code-corhuila/educk-communication-portal`](https://github.com/code-corhuila/educk-communication-portal/tree/feat/HU-005-interfaz-comunicacion) · DB: [`code-corhuila/educk-communication-db`](https://github.com/code-corhuila/educk-communication-db/tree/feat/HU-005-esquema-mensajes) *(Reconciled: legacy branch slug `feat/HU-005-*` corresponds to Canonical `HU-004` per `04-requirements/traceability-matrix.md`)* |
| HU-XXX-009 | Containerize services with Docker (multi-stage Dockerfile, .dockerignore, docker-compose.yml) | done | [docker-compose.yml](./docker-compose.yml), [Dockerfile.backend](./Dockerfile.backend), [Dockerfile.frontend](./Dockerfile.frontend), [docker-ps-evidence.txt](./docker-ps-evidence.txt) |
| HU-XXX-010 | Ship MVP 1: promote to main, tag v1.0.0, run release checklist/DoD, demo the running system, run retrospective | done | [test-execution-results.txt](./test-execution-results.txt), [api-execution-evidence.json](./api-execution-evidence.json), [mvp-release-verification.md](./mvp-release-verification.md), [semana5.jpg](./semana5.jpg) |

## 2. My individual contribution

### Containerization with Docker (Session 1)
- Studied the distinction between image, container and registry: a Dockerfile is the recipe; building it produces an immutable image; running an image gives a live container; a registry (Docker Hub, GHCR) stores images so any environment can pull the exact same one.
- Reviewed why containers matter for distributed systems: they package a service with everything it needs (runtime, libraries, config) so it runs identically on any machine, ending "it works on my machine" and making the system reproducible across dev, qa and prod.
- Designed a multi-stage Dockerfile following the best practices from the session:
  - Build stage uses `maven:3.9-eclipse-temurin-21` to compile and package the application.
  - Runtime stage uses `eclipse-temurin:21-jre` (small JRE image) and copies only the jar from the build stage.
  - Layers ordered from least-to-most changing for better caching (pom.xml before src).
  - No secrets baked into the image; config via environment variables at run time.
- Created a `.dockerignore` to exclude `.env`, `.git`, `node_modules`, and build artifacts from the image context, preventing secrets and the build toolchain from being copied into the image.
- Avoided the common mistake of single-stage images that ship the whole build toolchain (huge, slow) and of copying `.env` into the image (security leak).
- Designed a `docker-compose.yml` that declares all EduTrack services on a shared network (`appnet`), with dependencies and environment configuration via env vars:
  - Services reach each other by service name on the compose network (e.g. `http://academic-service:8080`), not by hard-coded IP.
  - Database data is persisted in a Docker volume (containers are disposable; data is not stored in the container writable layer).
  - Config (DB URLs, secrets, other services' hostnames) is passed via environment variables, not baked into the image.
- Reviewed the real-life scenario from the session: a 1.2 GB image that leaks secrets because the team copied everything (including `.env` and the build toolchain) into a single-stage image. The fix is multi-stage build (ship only JRE + jar), `.dockerignore`, config via env at run time, and secrets never in the image.

### Release — shipping MVP 1 (Session 2)
- Reviewed what a release (MVP) actually is: a versioned, running increment that meets its acceptance criteria and could be deployed by someone who wasn't in the room. It is the unit of evaluation each corte — not a demo of intentions, but working software.
- Studied the promotion flow through environments: each HU is merged into develop (via `hu-xxx-dev`), integrated and validated in qa, and finally promoted to main and tagged (`v1.0.0`). The tag is the release; the containers built from that tag are what runs.
- Reviewed the release checklist / Definition of Done:
  - All acceptance criteria met and committed.
  - Unit + integration tests green; coverage at/above the floor (declared ≤ measured).
  - System runs via `docker compose up`: services start against a real DB, no blank/error start.
  - Happy path and key error path work (e.g. duplicate event → idempotency check, out-of-stock → 409).
  - No secrets in the repo/image; config via env.
  - README + ADRs updated; version tagged (`v1.0.0`); CHANGELOG entry.
- Reviewed the demo approach: the demo shows the running system doing the sprint goal end to end (call the API, see the data persist, trigger the error path), not slides. Everyone can articulate what was built, why the architecture is shaped this way (point to the ADR), and what's next.
- Studied the retrospective: close the corte with a short retrospective (what went well, what hurt, what we change). Turn each improvement into a concrete backlog item. Unfinished "Should/Could" stories roll into the Corte 2 backlog — re-estimated, not just moved.
- Reviewed how MVP 1 is evaluated: the release grade weighs functionality/demo (does it meet the sprint goal), code quality (Clean Code, tests), architecture (DDD/hexagonal boundaries respected), and Scrum compliance (backlog, HU, ceremonies). The individual weekly hu-status evidence in the fork contributes to the personal mark.
- Identified common release mistakes to avoid: calling it "released" with a red pipeline or a service that won't start; demoing slides instead of the running system; no version tag / no CHANGELOG → the release isn't reproducible; skipping the retro → the same pain returns in Corte 2.

## 3. Blockers and risks

- The specific messaging technology for asynchronous events (RabbitMQ) still needs to be fully integrated with Docker Compose in all modules (pending final integration tests).
- Risk: if a service fails to start via `docker compose up` (e.g. database connection issue, missing env var), the release is not valid — the release checklist requires all services to start against a real DB with no blank/error start.
- Risk: hard-coded IPs or config baked into images would make the system non-portable across dev/qa/prod and could leak secrets — must verify no `.env` or secrets are in any Dockerfile or image.
- Risk: if tests are red or a service won't start, the increment is not released (a draft) — MVP reduces scope, not standards.
- The demo must show the running system doing the sprint goal end to end (not slides), which requires all services to be running and integrated.

## 4. Plan for next week

- Complete the release of MVP 1: promote to main, tag v1.0.0, verify the release checklist/DoD with evidence, demo the running system, and run the retrospective.
- Ensure all services start via `docker compose up` against a real DB with no blank/error start.
- Verify that no secrets are in the repo/image; config is via env.
- Update READMEs and ADRs for the final release; create a CHANGELOG entry for v1.0.0.
- Prepare the demo: show the running system doing the sprint goal end to end (grade registration flow with GradeCreated → Notifications → parent notification; attendance registration flow with StudentAbsent → idempotency check).
- Run the retrospective: what went well, what hurt, what we change. Turn each improvement into a concrete backlog item for Corte 2.
- Carry unfinished "Should/Could" stories into the Corte 2 backlog — re-estimated, not just moved.
- Keep doing: walking skeleton early, contract tests in CI.
- Drop: big tasks (>1 day) — split them into smaller HU-sized pieces.

## 5. Compliance self-check

- [x] Conventional Commits - `type(scope): summary`
- [x] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [x] Testable acceptance criteria
- [x] Tests added/updated (unit / integration)
- [x] DDD / hexagonal boundaries respected (domain has no I/O)
- [x] No secrets; config via environment variables

## 6. Evidence links

- Repository: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1
- Week 05: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1/tree/main/05-week
- HU-STATUS: https://github.com/XimenaChala/sistemas-distribuidos-2026-b-g1/tree/main/05-week/hu-status
- Docker Compose file: (pending — to be linked once created)
- Dockerfile(s): (pending — to be linked once created)
- CHANGELOG: (pending — to be linked once created)
- Release tag v1.0.0: (pending — to be linked once created)

## 7. Session notes — Containerization with Docker

### 1. Why containers, in one line
A container packages your service with everything it needs (runtime, libraries, config) so it runs identically on any machine. It ends "it works on my machine" and makes a distributed system reproducible — the same artifact runs in dev, qa and prod.

### 2. Image vs container vs registry
A Dockerfile is the recipe; building it produces an image (an immutable template); running an image gives a container (a live instance). A registry (Docker Hub, GHCR) stores images so any environment can pull the exact same one.

```
Dockerfile → build → Image → run → Container
                            ↓
                        registry (push/pull)
```

### 3. A good Dockerfile (multi-stage)
Build in one stage, ship a small runtime in another. Order layers from least-to-most changing so caching helps; never bake secrets into the image.

**Dockerfile (Java, multi-stage):**
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml . && mvn -q dependency:go-offline   # cached layer
COPY src ./src && mvn -q clean package -DskipTests
FROM eclipse-temurin:21-jre                       # small runtime image
COPY --from=build /app/target/app.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 4. Many services at once: Docker Compose
A distributed system is several containers that must start together and talk. Docker Compose declares them in one `docker-compose.yml`, on a shared network, with dependencies and env.

```
docker network: appnet
orders-api
inventory-api
postgres
redis
```

Services reach each other by name (e.g. `http://inventory-api:8080`), not by IP.

### 5. Config and data: the two things that ruin images
**Caution:** Config is not baked in — pass it via environment variables (DB URL, secrets, the other services' hostnames). Data is not stored in the container — a container is disposable; persist databases in volumes. Bake config or data into the image and you get insecure, non-portable, data-losing deployments.

### 6. A path you will face in real life
**Tip — Scenario:** the 1.2 GB image that leaks secrets. A team `COPY . .` everything (including `.env` and the whole build toolchain) into a single-stage image. It is huge, slow to ship, and the secret is now inside a layer anyone can extract.

**Fix:** multi-stage build (ship only the JRE + jar), a `.dockerignore` (exclude `.env`, `.git`, `node_modules`), config via env at run time, and secrets never in the image. Small, clean, safe.

### 7. Common mistakes
- Single-stage images that ship the whole build toolchain (huge, slow).
- Secrets/.env copied into the image.
- Storing the database inside the container (data lost on restart).
- Hard-coding IPs instead of using service names on the compose network.

## 8. Session notes — Release — shipping MVP 1

### 1. What a release actually is
A release (MVP) is a versioned, running slice of the product that meets its acceptance criteria and could be deployed by someone who wasn't in the room. It is the unit of evaluation each corte — not a demo of intentions, but working software.

### 2. Promote through the environments
The increment moves through the branches you set up: each HU was merged into develop (via `hu-xxx-dev`), integrated and validated in qa, and finally promoted to main and tagged (`v1.0.0`). The tag is the release; the containers built from that tag are what runs.

```
develop → qa → main
                ↓
            tag v1.0.0 = MVP 1
```

### 3. The release checklist (Definition of Done)
Before you call it released, verify — and show evidence:

- All acceptance criteria met (and committed).
- Unit + integration tests green; coverage at/above the floor (declared ≤ measured).
- Runs via `docker compose up`: services start against a real DB, no blank/error start.
- Happy path and the key error path work (e.g. duplicate event → 409 or idempotency check).
- No secrets in the repo/image; config via env.
- README + ADRs updated; version tagged (`v1.0.0`); CHANGELOG entry.

**Caution:** "MVP" reduces scope, not standards. A release with red tests, a service that won't start, or hard-coded secrets is not released — it is a draft.

### 4. Demo working software
The demo shows the running system doing the sprint goal end to end (call the API, see the data persist, trigger the error path), not slides. Everyone can articulate what was built, why the architecture is shaped this way (point to the ADR), and what's next.

### 5. Retrospective → next corte
Close the corte with a short retrospective: what went well, what hurt, what we change. Turn each improvement into a concrete backlog item. Unfinished "Should/Could" stories roll into the Corte 2 backlog — re-estimated, not just moved.

| Keep | Drop | Try |
|------|------|-----|
| walking skeleton early | big tasks (>1 day) | contract tests in CI |

Each "Try" becomes a backlog item for Corte 2.

### 6. How MVP 1 is evaluated
The release grade weighs more than "does it run": functionality/demo (does it meet the sprint goal), code quality (Clean Code, tests), architecture (DDD/hexagonal boundaries respected), and Scrum compliance (backlog, HU, ceremonies). Your individual weekly hu-status evidence in the fork contributes to your personal mark.

### 7. Common mistakes
- Calling it "released" with a red pipeline or a service that won't start.
- Demoing slides instead of the running system.
- No version tag / no CHANGELOG → the release isn't reproducible.
- Skipping the retro → the same pain returns in Corte 2.

## 8. Evidence files & links
- Local Evidence Files:
  - Unified Docker Compose: [`docker-compose.yml`](./docker-compose.yml)
  - Backend Dockerfile: [`Dockerfile.backend`](./Dockerfile.backend)
  - Frontend Dockerfile: [`Dockerfile.frontend`](./Dockerfile.frontend)
  - Test Execution Results: [`test-execution-results.txt`](./test-execution-results.txt)
  - Live Docker PS Output: [`docker-ps-evidence.txt`](./docker-ps-evidence.txt)
  - Live API JSON Response: [`api-execution-evidence.json`](./api-execution-evidence.json)
  - Release Verification & DoD: [`mvp-release-verification.md`](./mvp-release-verification.md)
  - Week 5 Photo/Screenshot: [`semana5.jpg`](./semana5.jpg)
- Remote Repositories (Official `code-corhuila` Organization):
  - Frontend Portal (Port 3000): https://github.com/code-corhuila/educk-communication-portal
  - Backend API (Port 8085): https://github.com/code-corhuila/edutrack
  - Database Schema & Migrations: https://github.com/code-corhuila/educk-communication-db
  - Documentation Repository: https://github.com/code-corhuila/educk-docs
  - Course Tracking Repository: https://github.com/code-corhuila/sistemas-distribuidos-2026-b-g1

## 9. Self-check answers (for personal review)

**Containerization:**
1. An image versus a container is: **Image = immutable template; container = running instance of it**
2. A multi-stage Dockerfile is used to: **Build in one stage and ship a small runtime image in another**
3. Configuration (DB URL, secrets) should be provided via: **Environment variables at run time**
4. Persistent database data belongs in: **A volume (containers are disposable)**
5. In Docker Compose, services reach each other by: **Service name on the shared network (http://inventory-api:8080)**
6. Copying .env and the whole toolchain into a single-stage image: **Makes it huge and leaks secrets (use multi-stage + .dockerignore)**

**Release:**
1. A release (MVP) is: **A versioned, running increment that meets its acceptance criteria**
2. The release is marked by: **Promoting to main and tagging a version (v1.0.0)**
3. If tests are red or the service won't start, the increment is: **Not released (a draft) - MVP reduces scope, not standards**
4. A good demo shows: **The running system doing the sprint goal (incl. an error path)**
5. Unfinished Should/Could stories at corte end: **Roll into the next backlog, re-estimated**
6. The MVP grade weighs, besides functionality: **Code quality, architecture compliance and Scrum compliance**