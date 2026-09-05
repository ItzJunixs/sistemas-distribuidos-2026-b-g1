# Product Brief — EduTrack (educk)

project_key: PRJ-EDUTRACK-MVP1

## Declared Tech Stack

- backend: Java 21 LTS + Spring Boot 3 (Hexagonal Architecture)
- database: PostgreSQL 16 (Database per Service)
- messaging: RabbitMQ 3.12+ (Topic Exchange edutrack.events)
- frontend: Responsive Web / Mobile (Design System & Figma Prototype)
- infrastructure: Docker & Docker Compose (appnet)

## Human Context

### Initial Context

EduTrack is a distributed school-tracking platform designed for parents, guardians, teachers, and school administrators. Before EduTrack, parents learned about grades and student absences late through fragmented channels (WhatsApp groups, paper notes, inconsistent school portals). 

The platform connects five autonomous modules through synchronous REST APIs and asynchronous domain events (`GradeCreated`, `StudentAbsent`) with event deduplication and retry policies, providing parents with near real-time visibility into their children's school progress.

### Needs and Problems

- Enable parents to track academic progress and daily attendance of multiple children from a single unified account.
- Deliver automated, instant notifications to parents when teachers publish a grade or record an absence.
- Ensure event deduplication so network retries do not produce duplicate notifications to guardians (idempotency key based on `eventId`).
- Preserve causal ordering of attendance records so that absence events are recorded and observed in the correct order.
- Provide direct, asynchronous communication threads between parents and teachers linked to specific subjects.
- Maintain system resilience and partial failure tolerance: if the notification service is temporarily down, grades and attendance records remain queryable and events are reprocessed via retries.

### Current Processes & MVP Scope

1. **Teacher registers grade:** Academic Records persists the grade in `academic_db`, emits `GradeCreated`. Notifications consumes the event and alerts the parent.
2. **Teacher registers absence:** Attendance records the absence in `attendance_db`, emits `StudentAbsent`. Notifications verifies eventId deduplication and dispatches the alert.
3. **Parent queries dashboard:** Parent queries grades and attendance for all linked children via the API Gateway.
4. **Parent contacts teacher:** Parent initiates a messaging thread with the course teacher linked to a subject.

### Business Glossary

- **Parent / Guardian:** Legal guardian user linked to one or more enrolled students.
- **Teacher:** Academic instructor authorized to register grades and session attendance.
- **Student:** Enrolled learner associated with one or more guardian accounts.
- **Grade:** Numerical evaluation score (1.0–5.0) associated with a student, subject, and assignment.
- **Attendance Record:** Session attendance entry (`PRESENT`, `ABSENT`, `LATE`) tagged with date and sequence number.
- **GradeCreated:** Domain event emitted when a grade is recorded.
- **StudentAbsent:** Domain event emitted when an absence is recorded.
- **Idempotency Key (`eventId`):** Unique UUID attached to every domain event to guarantee at-least-once delivery without duplicate processing.

