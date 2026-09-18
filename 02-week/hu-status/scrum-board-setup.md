# Scrum Board Setup & Configuration

## 1. Board Columns
- **Backlog:** Prioritized candidate user stories and requirements.
- **To Do:** Sprint-committed items ready for development.
- **In Progress:** Actively being coded on a `feat/HU-XXX` branch.
- **In Review:** Open Pull Request awaiting CI checks, automated quality gates, and mandatory peer review (no author bypasses allowed).
- **Done:** Merged to `develop`, acceptance criteria satisfied, unit tests green.

## 2. Custom Project Fields
- `Sprint`: Current sprint iteration (MVP 1 / Sprint 1).
- `Modulo`: Target microservice (Identity, Academic, Attendance, Notifications, Communication).
- `Historia`: User story identifier (HU-001 through HU-005).
- `Estimate`: Story points (Fibonacci sequence: 1, 2, 3, 5, 8).

## 3. Workflow Automation Rules
- When a PR is created referencing `#<issue-id>`, the card automatically moves to **In Review**.
- When the PR is merged into `develop`, the card automatically moves to **Done**.
