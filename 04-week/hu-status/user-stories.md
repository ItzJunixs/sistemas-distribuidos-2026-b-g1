# User Stories — Backlog

> MVP 1 backlog from EduTrack PDR / Week 02 board. Five HUs mapped to five modules.

---

## Backlog status

| Cut | Sprint | Total HUs | Delivered Walking Skeleton (Cut 1) | Progressive Scope (Next Cuts) | Status |
|-----|--------|-----------|-----------------------------------|-------------------------------|--------|
| **Cut 1** | **MVP 1 / Sprint 1** | 5 | **HU-004** (Parent-Teacher Communication) | **HU-001** (Grades), **HU-002** (Absence Alerts), **HU-003** (Multi-child), **HU-005** | **Cut 1 Delivered** (HU-004 Walking Skeleton implemented & verified) |

> **Note on Backlog Prioritization:**  
> - **Cut 1 Delivered Walking Skeleton:** `HU-004` (Parent-Teacher direct messaging with hexagonal architecture, integration/concurrency tests, dedicated DB, and web portal).  
> - **Progressive Scope (Next Cuts):** `HU-001` (Grade publishing in academic service), `HU-002` (Absence alerts in attendance service), `HU-003` (Identity and parent-student linkages), and `HU-005`.  

---

## Epics

| ID | Epic | Description |
|----|------|-------------|
| EP-001 | Parent academic visibility | Grades near real time and multi-child progress |
| EP-002 | Attendance alerts | Absence registration, causal order, deduped alerts |
| EP-003 | Parent–teacher communication | Direct messaging linked to subjects |

---

## User Stories

### HU-001 — View grades in near real time {#HU-001}

**Epic:** EP-001  
**Module:** Academic Records (+ Notifications for alert)

> **As** a parent  
> **I want** to be notified and able to query when a teacher publishes a grade  
> **so that** I see my child’s academic results without waiting for delayed messages

**Acceptance Criteria:**

```gherkin
Scenario 1: Grade published notifies parent
  Given a student linked to a parent
  And a teacher is authorized for the subject
  When the teacher registers a grade
  Then Academic Records stores the grade
  And a GradeCreated event with eventId is emitted
  And Notifications creates a parent notification
  And the parent can query the grade by student

Scenario 2: Notifications temporarily unavailable
  Given Academic Records stored the grade
  When Notifications is down
  Then the grade remains queryable
  And the notification can be processed later via retry
```

**Definition of Done:**
- [ ] Grade entity + register/query endpoints
- [ ] `GradeCreated` emitted with contract fields
- [ ] Unit tests for grade rules
- [ ] Module README
- [ ] PR evidence on code repo

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Must Have |
| Target sprint | MVP 1 |
| Assigned to | Member 2 — Academic Records (PDR) |
| Status | Ready |
| Dependencies | Identity student exists |
| Affected service(s) | academic-service, notifications-service |

---

### HU-002 — Absence alert without duplicates {#HU-002}

**Epic:** EP-002  
**Module:** Notifications (consumes Attendance)

> **As** a parent  
> **I want** a single alert when my child is marked absent  
> **so that** I am informed without duplicate notifications for the same event

**Acceptance Criteria:**

```gherkin
Scenario 1: First StudentAbsent creates notification
  Given a valid StudentAbsent event with eventId
  When Notifications processes it the first time
  Then a parent notification is created

Scenario 2: Duplicate event ignored
  Given eventId was already processed
  When the same StudentAbsent is delivered again
  Then no additional notification is created
```

**Definition of Done:**
- [ ] Consumer for `StudentAbsent`
- [ ] Idempotency via eventId
- [ ] Basic retry on failure
- [ ] Tests + README

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Must Have |
| Target sprint | MVP 1 |
| Assigned to | Member 4 — Notifications |
| Status | Ready |
| Dependencies | HU-005 emits `StudentAbsent` |
| Affected service(s) | notifications-service |

---

### HU-003 — View progress of multiple children {#HU-003}

**Epic:** EP-001  
**Module:** Identity & Accounts

> **As** a parent  
> **I want** to link and list multiple children under my account  
> **so that** I can follow each child’s progress from one place

**Acceptance Criteria:**

```gherkin
Scenario 1: Link student to parent
  Given an existing parent and student
  When the parent–student link is created
  Then the link is stored
  And GET children by parent returns the student

Scenario 2: Duplicate link rejected
  Given the parent–student link already exists
  When the same link is created again
  Then the operation is rejected without creating a duplicate
```

**Definition of Done:**
- [ ] User, Parent, Student, School entities
- [ ] Create + link endpoints
- [ ] GET children by parent
- [ ] Duplicate-link validation + tests + README

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Must Have |
| Target sprint | MVP 1 |
| Assigned to | Member 1 — Identity & Accounts |
| Status | Ready |
| Dependencies | None |
| Affected service(s) | identity-service |

---

### HU-004 — Direct communication with teachers {#HU-004}

**Epic:** EP-003  
**Module:** Communication

> **As** a parent  
> **I want** to send and read messages with a teacher about a subject  
> **so that** I can clarify academic topics without leaving the platform

**Acceptance Criteria:**

```gherkin
Scenario 1: Send message
  Given a parent and a teacher user
  When the parent sends a message linked to a subject
  Then the message is stored
  And it appears when querying the conversation

Scenario 2: Query conversations
  Given existing messages between parent and teacher
  When either party queries conversations
  Then messages are returned in order
```

**Definition of Done:**
- [ ] Message entity
- [ ] Send + query endpoints
- [ ] Subject association + basic teacher availability
- [ ] Tests + README

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Must Have |
| Target sprint | MVP 1 |
| Assigned to | Member 5 — Communication |
| Status | Ready |
| Dependencies | Identity users exist |
| Affected service(s) | communication-service |

---

### HU-005 — Attendance registration with causal ordering {#HU-005}

**Epic:** EP-002  
**Module:** Attendance

> **As** a teacher  
> **I want** to register attendance/absences without duplicates and with correct event order  
> **so that** parents and downstream services see a consistent attendance history

**Acceptance Criteria:**

```gherkin
Scenario 1: Register absence
  Given an enrolled student
  When the teacher registers ABSENT for a date
  Then Attendance stores the record
  And StudentAbsent is emitted with eventId
  And duplicate studentId+date is rejected

Scenario 2: Causal order preserved
  Given multiple attendance-related events for a student
  When they are recorded
  Then consumers can observe happens-before / ordering guarantees defined by the module
```

**Definition of Done:**
- [ ] Attendance entity + register/query endpoints
- [ ] Duplicate validation
- [ ] `StudentAbsent` event
- [ ] Causal order handling + tests + README

| Field | Value |
|-------|-------|
| Story Points | 5 |
| Priority | Must Have |
| Target sprint | MVP 1 |
| Assigned to | Member 3 — Attendance |
| Status | Ready |
| Dependencies | Identity student exists |
| Affected service(s) | attendance-service |

---

## Shared setup stories (not product HUs)

Documented on Week 02 board for the whole team: repo branches, protection rules, event contracts, ADR for notification delivery trade-off, E2E demo prep. Tracked in code repo / Project board — not duplicated as product HUs here.

---

## Correlations

- Vision → `03-product/vision.md`
- Domain events → `02-domain/domain-events.md`
- NFRs → `04-requirements/non-functional.md`
