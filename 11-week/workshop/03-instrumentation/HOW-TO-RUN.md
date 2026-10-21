# CP3 · How to run `workflow-observability`

Minimal module of **opti-workflow** (Java, same package root `co.edu.corhuila.opti.workflow`)
with the instrumentation of the place-order journey:

| File | What it does |
|---|---|
| `TraceContext.java` | Parses / creates the W3C `traceparent`, creates child spans |
| `TraceContextFilter.java` | Reads or starts the trace, returns `traceparent` + `X-Correlation-Id` in the response, writes the `http.request` access line |
| `StructuredLogger.java` | JSON lines with the fields of `02-log-contract.md`; masks the "never log" keys |
| `StockHttpClient.java` | Step `reserve-stock`: `POST /api/v1/frames/{frameId}/reservations` on opti-products-api, copying `traceparent`, `X-Correlation-Id` and `Idempotency-Key` |
| `PlaceOrderMetrics.java` | `workflow_place_order_requests_total{outcome}` and `workflow_place_order_duration_seconds` histogram on `/metrics` |
| `WorkflowServer.java` | `POST /api/v1/sagas/place-order` and `GET /metrics` |
| `TracePropagationTest.java` | Proves the `trace_id` received = the `trace_id` sent downstream |

Only the JDK is used (`com.sun.net.httpserver`, `java.net.http`). The team services use
Spring Boot + Maven, but Maven Central is blocked on the network where this was built,
so the module has no dependencies. In the real `opti-workflow` the filter becomes a
`OncePerRequestFilter` (next to the existing `CorrelationFilter`) and the logger is the
existing `LogstashEncoder` with `trace_id` / `span_id` in the MDC.

## Requirements

- JDK 11 or newer (`javac` + `java`). Code is compiled with `--release 11`.

## Build and test

From `11-week/workshop/03-instrumentation` (sources in `src/`, test in `test/`; all files
are in package `co.edu.corhuila.opti.workflow.observability`):

```bash
# compile main + test
javac --release 11 -d out src/*.java test/*.java

# run the test (exit code 0 = pass, 1 = fail)
java -cp out co.edu.corhuila.opti.workflow.observability.TracePropagationTest
```

On Windows PowerShell:

```powershell
javac --release 11 -d out (Get-ChildItem src,test -Filter *.java).FullName
java -cp out co.edu.corhuila.opti.workflow.observability.TracePropagationTest
```

## Run the server by hand

```bash
PORT=8085 PRODUCTS_URL=http://127.0.0.1:8082 HTTP_TIMEOUT=PT5S APP_ENV=local \
  java -cp out co.edu.corhuila.opti.workflow.observability.WorkflowServer

curl -i -X POST http://127.0.0.1:8085/api/v1/sagas/place-order \
  -H "traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01" \
  -H "X-Correlation-Id: place-order-0001" \
  -d '{"productType":"FRAME","productId":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","quantity":1}'

curl http://127.0.0.1:8085/metrics
```

`PRODUCTS_URL` points to `opti-products-api`. If it is not running, the saga answers
`202 RUNNING` and the log shows `saga.step.failed` with `failure_reason=PARTICIPANT_UNAVAILABLE`.

## Real console output (my laptop, OpenJDK 11.0.32.1, 2026-10-21)

```text
$ java -cp out co.edu.corhuila.opti.workflow.observability.TracePropagationTest
PASS saga answers 201 COMPLETED
PASS products-api was called on the frame reservation path
PASS trace_id received by products-api == trace_id sent by the caller
PASS downstream span is a new span, not the caller span
PASS response returns the same trace_id
PASS every log line carries the trace_id
PASS counter by outcome is exposed
PASS latency histogram is exposed
--- logs written during the test ---
{"timestamp":"2026-10-21T19:07:09.580Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"3e8bff8516faf5be","correlation_id":"place-order-0001","event":"saga.started","message":"place-order saga started","saga_id":"656edeb4-2cbe-42e4-9034-0c9df3418080","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","attempt":1}
{"timestamp":"2026-10-21T19:07:09.665Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"3e8bff8516faf5be","correlation_id":"place-order-0001","event":"saga.step.completed","message":"step reserve-stock completed","saga_id":"656edeb4-2cbe-42e4-9034-0c9df3418080","saga_step":"reserve-stock","product_type":"FRAME","product_id":"aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa","http_status":201,"duration_ms":71}
{"timestamp":"2026-10-21T19:07:09.723Z","level":"INFO","service":"workflow-api","version":"1.0.0","environment":"test","trace_id":"4bf92f3577b34da6a3ce929d0e0e4736","span_id":"3e8bff8516faf5be","correlation_id":"place-order-0001","event":"http.request","message":"POST /api/v1/sagas/place-order -> 201","http_method":"POST","http_route":"/api/v1/sagas/place-order","http_status":201,"duration_ms":143}
ALL TESTS PASSED
```

The three lines follow the CP2 contract and all share the `trace_id` that the caller sent.
