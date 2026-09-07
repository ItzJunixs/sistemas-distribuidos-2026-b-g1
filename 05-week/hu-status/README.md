<!-- HU-STATUS TEMPLATE - do NOT remove the <!-- ... --> markers or the table headers.
     Your weekly grade is read AUTOMATICALLY from this file:
       05-week/hu-status/README.md  (inside YOUR fork). English. -->

# Weekly Status - Week 05

<!-- CONFIG-START - must match your profile repo (username/username) CONFIG -->
- FULL_NAME: Juan Diego Jiménez Horta
- GITHUB_USER: ItzJunixs
- TEAM: The illusionists
- SPRINT_GOAL: Move the tactical DDD model (entities, value objects, aggregates, invariants) for all four OptiView bounded contexts from placeholder to OptiView-specific content in `02-domain/entities-and-rules.md`, so `05-architecture` and `06-data` (in progress by the rest of the team) have a concrete domain model to build service boundaries and schemas against.

<!-- CONFIG-END -->

## 1. User stories worked this week
| HU ID | Title | Status (todo/doing/done) | Evidence (PR or commit URL) |
|---|---|---|---|
| HU-01 | Register a patient | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |
| HU-02 | Register an eye prescription | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |
| HU-05 | Register a frame in inventory | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |
| HU-09 | Track a work order through the lab | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |
| HU-10 | Generate an invoice from a work order | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |
| HU-11 | Register a payment against an invoice | doing | https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8 |

## 2. My individual contribution
- Rewrote `02-domain/entities-and-rules.md` (commit `195c0b9`) replacing the generic DDD placeholder template with OptiView-specific tactical models for all four bounded contexts.
- **Patients (`ms-pacientes`):** `Patient` aggregate with `EyePrescription` (OD/OI) value object and `PeriodicControl` lifecycle; invariants INV-PAT-001 (unique document), INV-PAT-002 (axis in [0, 180], HU-02), INV-PAT-003 (only one current formula per patient).
- **Inventory (`ms-inventario`):** `Frame` aggregate with `Sku`/`Money` value objects and an append-only `StockMovement` ledger; invariants INV-INV-001 (`sellPrice >= buyPrice`), INV-INV-002 (unique SKU, HU-05), INV-INV-003 (stock never negative), INV-INV-004 (every stock change produces a `StockMovement`).
- **Orders (`ms-ordenes`):** `WorkOrder` aggregate with its `QUOTATION → APPROVED → IN_LABORATORY → READY → DELIVERED` state machine and `OrderStatusHistory`; invariants INV-ORD-001 to INV-ORD-004 (no skipping states, cancellation reason required, full history, automatic `actualDelivery`).
- **Billing (`ms-facturacion`):** `Invoice` aggregate with `Payment` entities and `Money`/`InvoiceNumber` value objects; invariants INV-BIL-001 to INV-BIL-003 (server-computed total, payments never exceed total, status derived from payments).
- Added Java code examples (domain layer, no framework imports) for Patients/Inventory and Go code examples for Orders/Billing, matching the stacks confirmed in `_stacks/java-spring.md` and `_stacks/go.md`.

## 3. Blockers and risks
- `entities-and-rules.md` still references service names from the `00-governance` naming scheme (`ms-pacientes`, `ms-ordenes`, etc.); this needs to land consistently once the team resolves the naming mismatch with `02-domain/optiview/` before `05-architecture` locks in service boundaries.
- Orders/Billing invariants assume synchronous REST reads across bounded contexts (e.g. Orders checking Inventory stock at creation) — needs to be validated against whatever `05-architecture` decides for context integration (REST vs. events only).

## 4. Plan for next week
- Cross-check the new invariants (INV-PAT-*, INV-INV-*, INV-ORD-*, INV-BIL-*) against `04-requirements/traceability-matrix.md` and update the FR/HU coverage if any invariant surfaces a requirement gap.
- Support the team in aligning `05-architecture` service boundaries with the aggregates defined here (one aggregate root per bounded context, no cross-aggregate transactions).

## 5. Compliance self-check
- [ ] Conventional Commits - `type(scope): summary`
- [ ] Per-environment HU branch + PR to that environment (hu-xxx-dev -> develop, ...)
- [ ] Testable acceptance criteria
- [ ] Tests added/updated (unit / integration)
- [ ] DDD / hexagonal boundaries respected (domain has no I/O)
- [ ] No secrets; config via environment variables

## 6. Evidence links
- https://github.com/code-corhuila/opti-docs/commit/195c0b917ab15e425f982c32bb6d3a66de255ea8
- https://github.com/code-corhuila/opti-docs/blob/main/02-domain/entities-and-rules.md
- https://github.com/code-corhuila/opti-docs
