# Domain Map — Bounded Contexts

> Aligned with EduTrack PDR (MVP 1) — five modules / bounded contexts.

---

## 1. Domain overview

```
eduTrack helps parents and guardians follow their children’s school life in near
real time: identity links (parent–student–school), academic grades and assignments,
attendance/absences, notifications driven by events, and basic parent–teacher
messaging. Competitive focus for MVP 1 is reliable distributed tracking and alerts,
not a full LMS or AI analytics.
```

---

## 2. Identified Bounded Contexts

### Bounded Context: Identity & Accounts

| Field | Value |
|-------|-------|
| **Name** | Identity & Accounts |
| **Responsibility** | Users, parents, students, schools, and parent–student links |
| **Owning team** | Member 1 (PDR) |
| **Microservice(s)** | `identity-service` |
| **Database** | Owned DB (per module) |
| **Ubiquitous Language** | User, Parent, Student, School, Link |
| **Related HU** | HU-003 |

| Term | Meaning in THIS context | Different elsewhere? |
|------|------------------------|----------------------|
| User | Authenticatable account | Yes — Student is academic/school profile |
| Parent | Guardian account that can link many students | No |
| Student | Child profile linkable to parents | Yes — Academic uses studentId for grades |

---

### Bounded Context: Academic Records

| Field | Value |
|-------|-------|
| **Name** | Academic Records |
| **Responsibility** | Assignments and grades; emit `GradeCreated` |
| **Owning team** | Member 2 (PDR) |
| **Microservice(s)** | `academic-service` |
| **Database** | Owned DB |
| **Ubiquitous Language** | Grade, Assignment, Subject, StudentId |
| **Related HU** | HU-001 |

---

### Bounded Context: Attendance

| Field | Value |
|-------|-------|
| **Name** | Attendance |
| **Responsibility** | Attendance/absence registration and query; causal order; emit `StudentAbsent` |
| **Owning team** | Member 3 (PDR) |
| **Microservice(s)** | `attendance-service` |
| **Database** | Owned DB |
| **Ubiquitous Language** | Attendance, Absence, SessionDate, EventOrder |
| **Related HU** | HU-005 |

---

### Bounded Context: Notifications

| Field | Value |
|-------|-------|
| **Name** | Notifications |
| **Responsibility** | Consume academic/attendance events; notify parents; dedup + retry |
| **Owning team** | Member 4 (PDR) |
| **Microservice(s)** | `notifications-service` |
| **Database** | Owned DB (delivery / processed event store) |
| **Ubiquitous Language** | Notification, IdempotencyKey, Retry |
| **Related HU** | HU-001, HU-002 |

---

### Bounded Context: Communication

| Field | Value |
|-------|-------|
| **Name** | Communication |
| **Responsibility** | Messages between parents and teachers; subject link; basic availability |
| **Owning team** | Member 5 (PDR) |
| **Microservice(s)** | `communication-service` |
| **Database** | Owned DB |
| **Ubiquitous Language** | Message, Conversation, Subject, Availability |
| **Related HU** | HU-004 |

---

## 3. Context Map

```
Identity & Accounts
        │ userId / parentId / studentId (U→D)
        ▼
Academic Records ──GradeCreated (OHS/PL)──▶ Notifications ──▶ Parent
        │
Attendance ──StudentAbsent (OHS/PL)───────▶ Notifications

Identity & Accounts ◀── refs ──▶ Communication (parent/teacher messaging)
```

### Relationships table

| Context A | Relationship | Context B | Channel | Contract |
|-----------|-------------|-----------|---------|----------|
| Identity & Accounts | U → D | Academic Records | REST (studentId) | OpenAPI |
| Identity & Accounts | U → D | Attendance | REST (studentId) | OpenAPI |
| Identity & Accounts | U → D | Communication | REST (parent/teacher ids) | OpenAPI |
| Academic Records | U → D (OHS/PL) | Notifications | Event | `GradeCreated` |
| Attendance | U → D (OHS/PL) | Notifications | Event | `StudentAbsent` |
| Dashboard / API edge | C/S | Academic Records | REST | Query grades |

---

## 4. Core Domain, Supporting, Generic

| Bounded Context | Type | Justification |
|----------------|------|---------------|
| Academic Records | Core | Grades are central to parent tracking value |
| Attendance | Core | Absences are central alert/demo flow |
| Notifications | Supporting | Delivers value of events; not the source of truth |
| Identity & Accounts | Supporting / Generic | Needed links; auth patterns are largely generic |
| Communication | Supporting | Useful; secondary to grade/absence flows |

---

## 5. Modeling decisions

- **Source:** EduTrack PDR v1.0 — August 2026 (Week 01 hu-status)
- **Map iteration:** v2 — aligned docs to PDR module names and events (2026-08-21)

| Decision | Discarded alternative | Reason |
|----------|----------------------|--------|
| Keep Attendance separate from Academic | Single “records” module | Different invariants, causal order, and `StudentAbsent` flow |
| Notifications separate from Communication | One messaging module | System alerts ≠ human conversations |
| Events `GradeCreated` / `StudentAbsent` | Generic `*Updated` names | Explicit past-tense facts for contracts and demos |

---

## 6. How to update this map

1. New service must map to an existing bounded context or justify a new one.
2. Sync with `09-microservices/service-catalog.md` and `05-architecture/overview.md`.
3. Re-run Event Storming when major domain changes occur.
