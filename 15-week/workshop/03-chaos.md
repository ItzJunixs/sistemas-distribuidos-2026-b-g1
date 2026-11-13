# 3 · Chaos experiment: kill sales-api in the middle of place-order

Script: `chaos/kill-sales-api.sh` (runs on the laptop with the platform up, develop data).

## 3.1 Why sales-api

`place-order` (ADR-004) runs `check-patient` (customers) → `reserve-stock` (products) →
`open-order` (sales). Killing the participant of the **last** step is the worst case: two
steps are already done when it fails, and one of them (`reserve-stock`) holds stock.

## 3.2 What the code says should happen (read before the experiment)

From `opti-workflow` (`SagaOrchestrator`, `SagaInstance`, `application.yml`):

- A participant that does not answer is a **TECHNICAL** failure. The HTTP client retries
  inside the call (`httpAttempts`), then the saga stays `RUNNING` with `attempts + 1`.
- A resume loop picks `RUNNING` sagas again every `SAGA_RESUME_EVERY=PT30S` after
  `SAGA_RESUME_AFTER=PT30S`. The step that failed is re-executed with the same idempotency key.
- When `attempts` reaches `SAGA_MAX_ATTEMPTS=3`, `technicalFailure()` returns `FAILED` with
  `PARTICIPANT_UNAVAILABLE` **and the orchestrator stops there: it does not run the
  compensations**. Compensation only runs for a BUSINESS refusal (`handleFailure`).
- Nothing in `opti-worker` expires reservations (`products.stock_reservation.status` is
  `RESERVED`/`RELEASED`, no TTL).

## 3.3 Hypotheses

| # | Fault | Steady state we expect | Why it would be graceful |
|---|---|---|---|
| H1 | sales-api down **20 s** | the saga answers `RUNNING` at once, then `COMPLETED` after sales-api is back (≤ ~60 s); stock −1, one `RESERVED` reservation **with** a work order (`orderId` set) | retries + resume loop + idempotency keys (no double order) |
| H2 | sales-api down **150 s** (> 3 attempts × 30 s) | the saga ends `FAILED` / `PARTICIPANT_UNAVAILABLE` with `completedSteps = [check-patient, reserve-stock]`. **Prediction from the code: the reservation stays `RESERVED` with no work order (`orderId` null), so 1 unit of stock is leaked** (`FrameService.reserve` already did `frame.reserve(units)`), visible to an operator but not undone | it is not hidden (FAILED is never silent), but it is not graceful for stock: this is the week 11 SLO 3 risk "frame left reserved" |
| H3 | during both | `GET /patients` and `GET /frames` keep answering 200 through the gateway; `GET /work-orders` answers 503 with the error envelope; the front shows "No pudimos cargar la información" only in Órdenes | gateway isolates routes; `RemoteBoundary` per portal (`opti-front`) |

Blast radius: develop on the laptop only. Abort: `docker compose start sales-api`.

## 3.4 Result

Recorded by the script in `chaos/chaos-output.txt`.

| # | Observed | Matches? | Action |
|---|---|---|---|
| H1 | _pending run_ | | |
| H2 | _pending run_ | | if confirmed: story CHAOS-01 below |
| H3 | _pending run_ | | |

## 3.5 Story if H2 is confirmed

**CHAOS-01 · A saga that gives up compensates what it already did (P1)**

- Given a place-order saga with `reserve-stock` done and sales-api down longer than
  `SAGA_MAX_ATTEMPTS × SAGA_RESUME_EVERY`, when the last attempt fails, then the saga runs the
  compensations in reverse order (release the reservation) and ends `COMPENSATED` with
  `failureReason = PARTICIPANT_UNAVAILABLE`; only if an undo fails it ends `FAILED`.
- Given that case, `products.stock_reservation` has no `RESERVED` row for that saga's
  reference and the frame stock is back to its value before the saga.
- Unit test in `SagaServiceTest`: participant `Orders` always throws TECHNICAL → after
  `maxAttempts` the `Stock` fake shows `release` called once.
