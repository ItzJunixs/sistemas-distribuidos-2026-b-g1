# CP4 · Golden signals and metric catalog

One table per service of the place-order journey (CP1).

## Where the metrics come from in OptiView (checked in the repos)

| Piece | Today | What this catalog needs |
|---|---|---|
| `opti-infra/compose.yml` | `otel-collector` (OTLP 4317/4318, Prometheus exporter on `:8889`), `prometheus` (scrapes only `otel-collector:8889`), `grafana` | Nothing new: the pipeline exists, the services just do not send anything yet (comment in `otel-collector.yaml`: "once they add an OpenTelemetry SDK") |
| `opti-workflow`, `opti-customers-api`, `opti-products-api`, `opti-sales-api` | Spring Boot web + JDBC (HikariCP, pool 10) / Redis, JSON logs with `LogstashEncoder`, **no actuator, no Micrometer**, `management.endpoints.enabled-by-default: false` | Add `micrometer-registry-otlp` pointing to `http://otel-collector:4318/v1/metrics`, base time unit seconds, plus the custom meters below. No new port is opened, metrics are pushed to the collector |
| `opti-api-gateway` (nginx) | JSON access log with `method`, `uri`, `status`, `duration`, `upstreamStatus`, `correlationId`; no metrics endpoint | Collector reads the access log (`filelog` receiver) and turns it into the gateway metrics; `stub_status` for connections |
| CP3 module | Exposes `workflow_place_order_requests_total` and `workflow_place_order_duration_seconds` on `/metrics` | Same names when the code moves into `opti-workflow` |

Because everything arrives through the collector, Prometheus sees one `job="otel-collector"`.
To tell services apart we turn on `resource_to_telemetry_conversion` in the collector's
`prometheus` exporter, so the OTel resource `service.name` becomes the label
`service_name` (`workflow-api`, `customers-api`, `products-api`, `sales-api`, the same
names the logs already use).

Naming rules we follow: `snake_case`, prefix = service domain, unit suffix
(`_seconds`), `_total` for counters, base units only (seconds, not ms).
All metrics below are **planned** except `workflow_place_order_requests_total` and
`workflow_place_order_duration_seconds`, which already exist in the CP3 module.

## opti-api-gateway (nginx)

| Signal | Metric | Type | Labels | PromQL (dashboard) |
|---|---|---|---|---|
| Latency | `gateway_request_duration_seconds` | histogram | `route` (`auth`, `customers`, `products`, `sales`, `workflow`, from `nginx/routes-templates/`), `method` | `histogram_quantile(0.95, sum by (le, route) (rate(gateway_request_duration_seconds_bucket[5m])))` |
| Traffic | `gateway_requests_total` | counter | `route`, `method`, `status_class`, `status` | `sum by (route) (rate(gateway_requests_total[5m]))` |
| Errors | `gateway_requests_total` | counter | `status_class="5xx"` | `sum(rate(gateway_requests_total{status_class="5xx"}[5m])) / sum(rate(gateway_requests_total[5m]))` |
| Saturation | `nginx_connections_active` (`stub_status`) + 429 refusals of `limit_req zone=api` (30 r/s, `limit_req_status 429` in `10-security.conf.template`) | gauge / counter | `status` | `max(nginx_connections_active)` and `sum(rate(gateway_requests_total{status="429"}[5m]))` |

## opti-workflow (place-order saga)

| Signal | Metric | Type | Labels | PromQL (dashboard) |
|---|---|---|---|---|
| Latency | `workflow_place_order_duration_seconds` | histogram | none (one saga type per metric) | `histogram_quantile(0.95, sum by (le) (rate(workflow_place_order_duration_seconds_bucket[5m])))` |
| Traffic | `workflow_place_order_requests_total` | counter | `outcome` (`completed`, `compensated`, `running`, `failed`) | `sum(rate(workflow_place_order_requests_total[5m]))` |
| Errors | `workflow_place_order_requests_total` | counter | `outcome` | `sum(rate(workflow_place_order_requests_total{outcome=~"running\|failed"}[5m])) / sum(rate(workflow_place_order_requests_total[5m]))` |
| Errors (per step) | `workflow_saga_step_failures_total` | counter | `step` (`check-patient`, `reserve-stock`, `open-order`), `reason` (`FailureReason`) | `sum by (step, reason) (increase(workflow_saga_step_failures_total[15m]))` |
| Saturation | `workflow_sagas_in_progress` | gauge | none | `max(workflow_sagas_in_progress)` — sagas stuck `RUNNING` waiting for `resumeStuck()` |

`compensated` is **not** an error for the SLO: it is a correct business answer (no
stock, inactive patient). Errors are only `running` (a participant did not answer)
and `failed` (`COMPENSATION_FAILED`, a frame can stay reserved).

## opti-customers-api (step check-patient)

| Signal | Metric | Type | Labels | PromQL (dashboard) |
|---|---|---|---|---|
| Latency | `http_server_requests_seconds` | histogram | `uri` (template, `/api/v1/patients/{id}`), `method`, `status` | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{service_name="customers-api",uri="/api/v1/patients/{id}"}[5m])))` |
| Traffic | `http_server_requests_seconds_count` | counter | `uri`, `method` | `sum(rate(http_server_requests_seconds_count{service_name="customers-api"}[5m]))` |
| Errors | `http_server_requests_seconds_count` | counter | `status` (`5xx` only) | `sum(rate(http_server_requests_seconds_count{service_name="customers-api",status=~"5.."}[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{service_name="customers-api"})` |

## opti-products-api (step reserve-stock)

| Signal | Metric | Type | Labels | PromQL (dashboard) |
|---|---|---|---|---|
| Latency | `http_server_requests_seconds` | histogram | `uri` (`/api/v1/frames/{id}/reservations`, `/api/v1/lenses/{id}/reservations`, ...), `method`, `status` | `histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{service_name="products-api",method="POST"}[5m])))` |
| Traffic | `products_reservations_total` | counter | `product_type` (4 values), `result` (`reserved`, `insufficient_stock`, `released`) | `sum by (product_type) (rate(products_reservations_total[5m]))` |
| Errors | `http_server_requests_seconds_count` | counter | `status=~"5.."` | `sum(rate(http_server_requests_seconds_count{service_name="products-api",status=~"5.."}[5m]))` |
| Saturation | `hikaricp_connections_pending` + `products_frames_below_minimum` | gauge | `pool` / `product_type` | `max(hikaricp_connections_pending{service_name="products-api"})`, `sum(products_frames_below_minimum)` |

## opti-sales-api (step open-order)

| Signal | Metric | Type | Labels | PromQL (dashboard) |
|---|---|---|---|---|
| Latency | `http_server_requests_seconds` | histogram | `uri="/api/v1/work-orders"`, `method`, `status` | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{service_name="sales-api",uri="/api/v1/work-orders",method="POST"}[5m])))` |
| Traffic | `sales_work_orders_opened_total` | counter | `product_type` | `sum(rate(sales_work_orders_opened_total[1h]))` |
| Errors | `http_server_requests_seconds_count` | counter | `status=~"5.."` | `sum(rate(http_server_requests_seconds_count{service_name="sales-api",status=~"5.."}[5m]))` |
| Saturation | `hikaricp_connections_pending` | gauge | `pool` | `max(hikaricp_connections_pending{service_name="sales-api"})` |

## Cardinality of the labels

| Label | Values | Cardinality | Decision |
|---|---|---|---|
| `route` (gateway) | 5 route files: `auth`, `customers`, `products`, `sales`, `workflow` | 5 | allowed |
| `method` | GET, POST, PATCH, DELETE | 4 | allowed |
| `status_class` / `status` | 2xx…5xx / ~10 codes | ≤ 10 | allowed |
| `uri` (Spring) | route **templates** of each API | ~15 per service | allowed, only templates |
| `outcome` | 4 saga statuses | 4 | allowed |
| `step` | 3 steps + 1 compensation | 4 | allowed |
| `reason` | `FailureReason` enum | 9 | allowed |
| `product_type` | FRAME, LENS, ACCESSORY, LIQUID | 4 | allowed |
| `service_name`, `pool` | one per service | ~5 | allowed |

Worst series count for `workflow_saga_step_failures_total`: 4 steps × 9 reasons = 36.
For `http_server_requests_seconds` in one API: ~15 uris × 4 methods × 10 status × 7
buckets ≈ 4,200 in theory, much less in practice because most combinations never
happen.

**Forbidden labels** and what we use instead:

| Forbidden | Why | Replaced by |
|---|---|---|
| `patient_id`, `seller_id`, `saga_id`, `work_order_id`, `reservation_id` | Unbounded (one per patient / sale) and `patient_id` is personal | The log line or trace (CP2), searched by `trace_id`; seller totals come from the ADR-006 reports, not from Prometheus |
| `order_code` (`OT-0142`) | One per order | Logs |
| raw URL (`/api/v1/frames/aaaa.../reservations`) | One series per product | The route template `uri="/api/v1/frames/{id}/reservations"` |
| `sku` (`MNT-0231`) | Hundreds of frames in the catalog | `product_type` |
| email, document number, patient name | Personal data, unbounded | Never in metrics |

## Latency histogram buckets

`workflow_place_order_duration_seconds` uses `0.1, 0.25, 0.5, 1, 2, 5` (+Inf).

- The user is a seller at the counter with the patient in front. Under **1 s** the
  sale feels instant; 1–2 s is acceptable; more than 2 s the seller clicks again
  (the `Idempotency-Key` protects us from the double sale, but it is a bad sign).
- A normal saga is 3 sequential REST calls inside the Docker network, each ~20–80 ms,
  so a healthy p95 should sit in the `0.25`–`0.5` buckets. We need resolution there.
- `5` is the participant limit (`HTTP_TIMEOUT=PT5S` in `opti-workflow`), so a request
  that lands above 2 s and up to 5 s is almost always a timeout of one step. Values
  above 5 s should not exist.
- For the per-service Spring endpoints we set the Micrometer SLO buckets
  `0.05, 0.1, 0.25, 0.5, 1, 2.5` (`management.metrics.distribution.slo.http.server.requests`):
  one hop must be much faster than the whole saga.
