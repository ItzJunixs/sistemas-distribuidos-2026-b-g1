# CP2 · Structured log contract

Applies to every OptiView backend in the place-order journey: `opti-workflow`,
`opti-customers-api`, `opti-products-api`, `opti-sales-api` and `opti-worker`.

Starting point: every `-api` already writes JSON with `LogstashEncoder`
(`*-app/src/main/resources/logback-spring.xml`) with `timestamp`, `level`, `message`,
a fixed `service` (`workflow-api`, `customers-api`, ...) and `correlationId` from the
MDC. This contract keeps those names for `service` and adds what is missing: trace
ids, version, environment, a stable `event` name and the domain fields.

## 1. Mandatory fields

| Field | Type | Required | Example | Why it exists |
|---|---|---|---|---|
| `timestamp` | string, ISO-8601 UTC with ms | yes | `2026-10-20T23:14:03.120Z` | Order the lines of all services on one timeline |
| `level` | enum `ERROR` `WARN` `INFO` `DEBUG` | yes | `WARN` | Filter what needs attention |
| `service` | string | yes | `workflow-api` | Know which container wrote it; same value as today's `customFields` |
| `version` | string (semver of the image) | yes | `1.0.0` | Tell if a problem started with a new release |
| `environment` | enum `local` `qa` `prod` | yes | `qa` | Do not mix test traffic with real sales |
| `trace_id` | string, 32 hex (W3C) | yes | `7f3c9a1e5b2d48c6a0e4f9b3d1c27e58` | Join every line of one request across the 5 services |
| `span_id` | string, 16 hex (W3C) | yes | `a41f0c9d2e7b3f16` | Know which hop/step wrote the line |
| `correlation_id` | string | yes | `place-order-7c1e` | Business id the gateway already sends as `X-Correlation-Id`; for the saga we use the `Idempotency-Key` so support can search by what the portal sent |
| `event` | string, `domain.action[.result]` | yes | `saga.step.timeout` | Stable name to count and alert on; `message` can change, `event` cannot |
| `message` | string | yes | `products-api did not answer in 5000 ms` | Human explanation, never personal data |
| `duration_ms` | integer | yes on the end of an operation, else omitted | `5003` | Latency of the step without opening the trace UI |

## 2. Domain fields (OptiView)

| Field | Type | Required | Example | Why it exists |
|---|---|---|---|---|
| `saga_id` | UUID | yes in `opti-workflow`, optional in participants | `3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11` | One saga can be resumed later by `resumeStuck()`, so the saga id ties the first attempt and the resume together |
| `saga_step` | enum `check-patient` `reserve-stock` `open-order` `compensate-reserve-stock` | when inside a step | `reserve-stock` | Tells which step of ADR-004 failed without reading code |
| `failure_reason` | enum of `FailureReason` | on failures | `INSUFFICIENT_STOCK` | Same values the API already returns in `SagaResponse`, so logs and API speak the same language |
| `product_type` | enum `FRAME` `LENS` `ACCESSORY` `LIQUID` | in reserve/release | `FRAME` | HU-25 made items polymorphic; a problem may affect only lenses |
| `product_id` | UUID | in reserve/release | `aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa` | Find the catalog row in `products.frame` |
| `reservation_id` | UUID | after reserve | `5e2b...` | Needed to check the compensation released the same reservation |
| `work_order_id` | UUID | after open-order | `c9d4...` | Row in `sales.work_order` |
| `order_code` | string | after open-order | `OT-0142` | The code the store staff actually read on screen |
| `seller_id` | UUID | in `opti-workflow` and `opti-sales-api` | `22222222-2222-4222-8222-222222222222` | Seller attribution (ADR-006 reports); a UUID, not a name |
| `patient_id` | UUID | in check-patient | `11111111-1111-4111-8111-111111111111` | Internal id only, it does not identify the person by itself |
| `attempt` | integer | in `opti-workflow` | `2` | Shows retries / resumes of a saga |

## 3. Never log (OptiView domain)

| Data | Where it appears | How we mask it |
|---|---|---|
| Patient full name (`fullName` returned by `GET /api/v1/patients/{id}`) | `PatientsHttpClient` | Never logged. Only `patient_id` |
| Document number (`documentNumber`, e.g. CC `1082...`) and document type | customers-api, `PatientRegistered` event | Never logged. If support needs it: last 3 digits, `***...417` |
| Optical formula (sphere, cylinder, axis, add) | `FormulaAdded`, work order | Health data. Never logged, only `formula_id` |
| Phone, email, address of the patient | customers-api | Never logged |
| JWT (`Authorization: Bearer ...`) and the RS256 keys used by `Rs256Verifier` | every `AuthFilter` | Header is removed before logging; log only `caller_role` (`SELLER`, `ADMIN`) |
| Passwords, BCrypt hashes | opti-auth-api | Never logged |
| Card / payment data from Wompi (`WompiPaymentGateway`) | opti-sales-api | Never logged; only `payment_id` and status |
| Raw request bodies | all | Not logged; log the fields of this contract instead |

## 4. Seven lines of one failed place-order (same `trace_id`)

Story: a seller sells frame `MNT-0231`. `check-patient` works. The first
`reserve-stock` call times out (the participant limit is `HTTP_TIMEOUT=PT5S` in
`opti-workflow`), the saga stays `RUNNING`. The resume runs 30 s later with the same
trace context saved with the saga in Redis, and this time `opti-products-api` answers
that there is not enough stock, so the saga ends `COMPENSATED`.

```json
{"timestamp":"2026-10-20T23:14:03.120Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"a41f0c9d2e7b3f16","correlation_id":"place-order-7c1e","event":"saga.started","message":"place-order saga started","saga_id":"3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11","seller_id":"22222222-2222-4222-8222-222222222222","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","attempt":1}
{"timestamp":"2026-10-20T23:14:03.164Z","level":"INFO","service":"customers-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"0c2e8b5f19d4a7e3","correlation_id":"place-order-7c1e","event":"patient.read","message":"patient found and active","patient_id":"11111111-1111-4111-8111-111111111111","duration_ms":38}
{"timestamp":"2026-10-20T23:14:03.171Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"5d7a1c3e9f0b2486","correlation_id":"place-order-7c1e","event":"saga.step.completed","message":"step check-patient completed","saga_id":"3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11","saga_step":"check-patient","duration_ms":49}
{"timestamp":"2026-10-20T23:14:08.176Z","level":"ERROR","service":"workflow-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"e8b40f6a2c1d9735","correlation_id":"place-order-7c1e","event":"saga.step.timeout","message":"products-api did not answer in 5000 ms, saga left RUNNING for resume","saga_id":"3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11","saga_step":"reserve-stock","failure_reason":"PARTICIPANT_UNAVAILABLE","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","attempt":1,"duration_ms":5003}
{"timestamp":"2026-10-20T23:14:38.204Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"9a63d2f05e8c1b47","correlation_id":"place-order-7c1e","event":"saga.resumed","message":"stuck saga resumed from saved state","saga_id":"3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11","saga_step":"reserve-stock","attempt":2}
{"timestamp":"2026-10-20T23:14:38.262Z","level":"WARN","service":"products-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"3f1b7e9c04a2d658","correlation_id":"place-order-7c1e","event":"stock.reservation.rejected","message":"reservation refused: requested 1, available 0","failure_reason":"INSUFFICIENT_STOCK","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","duration_ms":21}
{"timestamp":"2026-10-20T23:14:38.281Z","level":"WARN","service":"workflow-api","version":"1.0.0","environment":"qa","trace_id":"7f3c9a1e5b2d48c6a0e4f9b3d1c27e58","span_id":"9a63d2f05e8c1b47","correlation_id":"place-order-7c1e","event":"saga.compensated","message":"saga ended COMPENSATED at reserve-stock, nothing to release","saga_id":"3b0d6f2a-91c4-4e7a-b8f5-0c6a2d9e4f11","saga_step":"reserve-stock","failure_reason":"INSUFFICIENT_STOCK","attempt":2,"duration_ms":77}
```

Filtered by `trace_id = 7f3c9a1e5b2d48c6a0e4f9b3d1c27e58`, the lines tell the whole
story: who sold, which step ran, where it waited 5 s, that it was resumed, why it was
refused and how the saga ended. No name, document or formula of the patient appears.

## 5. Log levels in OptiView

| Level | When we use it | Example in our project |
|---|---|---|
| `ERROR` | A technical failure that breaks the step and that someone may need to act on | `opti-workflow` gets a timeout from `opti-products-api` on `reserve-stock`, or a compensation fails (`COMPENSATION_FAILED`, a frame stays reserved) |
| `WARN` | Expected refusal of a business rule, or a problem that was handled | `opti-products-api` refuses a reservation with `INSUFFICIENT_STOCK`; `opti-sales-api` refuses to deliver an order whose invoice is not fully paid |
| `INFO` | Normal business milestones, one per step | `saga.started`, `saga.step.completed`, work order `OT-0142` opened in `sales.work_order` |
| `DEBUG` | Details only for local debugging, off in `qa` and `prod` | Full URL of the participant call built by `StockHttpClient`, Redis lease acquired for a saga |
