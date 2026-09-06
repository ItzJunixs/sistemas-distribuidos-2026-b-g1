# ADR-003 — Persistence Strategy: Database per Service with PostgreSQL

- **ID:** ADR-003
- **Date:** 2026-08-21
- **Status:** Accepted
- **Authors:** Team G1 — eduTrack

---

## Context

Each microservice in eduTrack represents a distinct bounded context (`Identity`, `Academic`, `Attendance`, `Notifications`, `Communication`). A shared database model creates tight coupling, prevents independent schema migrations, and introduces a single point of failure. The persistence strategy must guarantee bounded context independence while maintaining data integrity.

**Known constraints:**
- Invariants within a service must be ACID-compliant.
- No direct database joins across microservices.
- Relational structure needed for academic grades, user links, and session attendance.

---

## Decision

**We decided:** Implement the **Database per Service** pattern using **PostgreSQL 16** with a dedicated logical database/schema per microservice (`identity_db`, `academic_db`, `attendance_db`, `notifications_db`, `communication_db`) and manage all schema evolutions with **Flyway** migrations.

**Justification:**
PostgreSQL offers robust ACID guarantees, JSONB support for semi-structured metadata (such as notification payload snapshots), and mature integration with Spring Data JPA. Isolating databases ensures team members can evolve schemas independently without risking cross-service breakage.

---

## Evaluated alternatives

| Alternative | Pros | Cons | Reason for discarding |
|------------|------|------|-----------------------|
| **PostgreSQL per Service (Chosen)** | ACID compliance, mature tooling, Flyway migrations, consistent operational model across all services | Requires distributed consistency patterns (events) instead of SQL foreign keys | — (Chosen) |
| **Shared PostgreSQL Database** | Easy cross-table SQL joins | Destroys microservice autonomy, high blast radius | Violates core microservice and SDD principles |
| **Polyglot Persistence (MongoDB + PG)** | Flexible schema for notifications/logs | Added operational overhead of maintaining multiple DB engines | eduTrack data structures are predominantly relational; PostgreSQL handles JSON adequately |

---

## Consequences

**Positive:**
- Microservices can be deployed and migrated independently.
- Failure of one database schema does not immediately bring down unrelated domains.
- Clear data ownership boundaries per service.

**Negative / Trade-offs:**
- Cross-service data consistency must rely on eventual consistency via domain events (`GradeCreated`, `StudentAbsent`).
- Read aggregations require API composition at the API Gateway or client level.

**Impact on the system:**
- Affected services: All 5 backend services.
- Documents that must be updated: `06-data/models.md`, `10-devops/local-setup.md`.

---

## References

- Data Modeling Specification → `06-data/models.md`
- Pattern Guide: Database per Service → `05-architecture/pattern-guide.md`
