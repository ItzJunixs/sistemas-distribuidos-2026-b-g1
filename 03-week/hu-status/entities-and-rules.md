# Entities, Value Objects, and Business Rules

> Tactical DDD for **eduTrack** — aligned with PDR MVP 1 entities and invariants.

---

## System entities

### Entity: User

**Context:** Identity & Accounts  
**Description:** Authenticatable account (parent, teacher, or admin role).

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| email | Email VO | Yes | Unique, valid format |
| role | enum | Yes | SUPER_ADMIN / ADMIN / TEACHER / PARENT / STUDENT |
| status | enum | Yes | ACTIVE / DISABLED |
| createdAt | DateTime | Yes | Immutable |

**Invariants:**
```
INV-USR-001: email unique
INV-USR-002: DISABLED users cannot authenticate
```

---

### Entity: Parent

**Context:** Identity & Accounts  
**Description:** Guardian profile linked to a User; may have multiple students.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| userId | UUID | Yes | References User |
| fullName | string | Yes | Non-empty |

**Invariants:**
```
INV-PAR-001: A parent–student link cannot be duplicated (same parentId+studentId)
INV-PAR-002: A parent may link multiple students
```

---

### Entity: Student

**Context:** Identity & Accounts (profile) / referenced by Academic & Attendance  
**Description:** Child whose academic and attendance data are tracked.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| schoolId | UUID | Yes | Must reference School |
| fullName | string | Yes | Non-empty |
| studentCode | string | Yes | Unique within school |

**Invariants:**
```
INV-STU-001: studentCode unique per school
INV-STU-002: Student must belong to a registered School
```

---

### Entity: School

**Context:** Identity & Accounts  
**Description:** Institution that owns students and staff accounts.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| name | string | Yes | Non-empty |
| createdAt | DateTime | Yes | Immutable |

---

### Entity: Grade

**Context:** Academic Records  
**Description:** Score assigned to a student for a subject/assessment.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| studentId | UUID | Yes | Must exist |
| subjectId | UUID | Yes | Must exist |
| value | number | Yes | Within allowed scale |
| createdAt | DateTime | Yes | Set on create |
| createdBy | UUID | Yes | Teacher user id |

**Invariants:**
```
INV-GRD-001: grade value within configured scale
INV-GRD-002: Publishing/creating a grade emits GradeCreated with eventId
```

---

### Entity: Assignment

**Context:** Academic Records  
**Description:** Task registered for students in a subject.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| subjectId | UUID | Yes | Required |
| title | string | Yes | Non-empty |
| dueDate | Date | No | If set, >= today on create (policy) |
| status | enum | Yes | PENDING / DONE |

---

### Entity: Attendance

**Context:** Attendance  
**Description:** Presence/absence record for a student on a date.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| studentId | UUID | Yes | Required |
| date | Date | Yes | Required |
| status | enum | Yes | PRESENT / ABSENT / LATE / EXCUSED |
| recordedAt | DateTime | Yes | For causal ordering |
| eventId | UUID | Yes | Dedup key when emitting events |

**Invariants:**
```
INV-ATT-001: No duplicate attendance for same studentId + date
INV-ATT-002: Absence registration emits StudentAbsent with eventId
INV-ATT-003: Event order must respect happens-before / causal order (HU-005)
```

---

### Entity: Message

**Context:** Communication  
**Description:** Message between parent and teacher, optionally linked to a subject.

| Attribute | Type | Required | Rules |
|-----------|------|----------|-------|
| id | UUID | Yes | Unique |
| fromUserId | UUID | Yes | Parent or teacher |
| toUserId | UUID | Yes | Counterpart |
| subjectId | UUID | No | Link to subject when applicable |
| body | string | Yes | Non-empty |
| sentAt | DateTime | Yes | Immutable |

**Invariants:**
```
INV-MSG-001: sender and receiver must be identifiable users
INV-MSG-002: Conversations are queryable by participant pair (+ optional subject)
```

---

## Value Objects

| VO | Rules |
|----|-------|
| Email | Valid format; stored lowercase |
| GradeValue | Numeric within scale min/max |
| EventId | UUID; required on published domain events |

---

## Aggregates (summary)

| Aggregate root | Internals | Context |
|----------------|-----------|---------|
| Parent | Parent–Student links | Identity & Accounts |
| Grade | GradeValue | Academic Records |
| Attendance | status + eventId | Attendance |
| Message | body + subject link | Communication |

---

## Summary table

| Name | Type | Bounded Context | Aggregate Root? |
|------|------|----------------|----------------|
| User | Entity | Identity & Accounts | Yes |
| Parent | Entity | Identity & Accounts | Yes |
| Student | Entity | Identity & Accounts | Yes |
| School | Entity | Identity & Accounts | Yes |
| Grade | Entity | Academic Records | Yes |
| Assignment | Entity | Academic Records | Yes |
| Attendance | Entity | Attendance | Yes |
| Message | Entity | Communication | Yes |

---

## Correlation with code

Each module follows PDR hexagonal layout: `domain/` · `application/` · `adapters/`.
Domain packages must not perform I/O.
