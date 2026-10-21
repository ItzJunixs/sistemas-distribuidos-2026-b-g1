package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Step reserve-stock: POST /api/v1/frames/{frameId}/reservations on opti-products-api. The call
 * runs in a child span and copies traceparent and X-Correlation-Id to the outbound request.
 */
public final class StockHttpClient {

    /** Result of the step, using the FailureReason names of opti-workflow. */
    public enum Result { RESERVED, INSUFFICIENT_STOCK, PARTICIPANT_UNAVAILABLE }

    private final HttpClient http;
    private final String baseUrl;
    private final Duration timeout;
    private final StructuredLogger log;

    public StockHttpClient(String baseUrl, Duration timeout, StructuredLogger log) {
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.log = log;
    }

    public Result reserveFrame(String sagaId, String frameId, int quantity) {
        TraceContext parent = TraceContext.current();
        TraceContext span = parent.child();
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/frames/" + frameId + "/reservations"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header(TraceContext.TRACEPARENT, span.toTraceparent())
                .header(TraceContext.CORRELATION_HEADER, parent.correlationId())
                .header("Idempotency-Key", sagaId + ":reserve-stock")
                .POST(HttpRequest.BodyPublishers.ofString("{\"quantity\":" + quantity + "}"))
                .build();

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("saga_id", sagaId);
        fields.put("saga_step", "reserve-stock");
        fields.put("product_type", "FRAME");
        fields.put("product_id", frameId);
        long started = System.nanoTime();
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            fields.put("http_status", response.statusCode());
            fields.put("duration_ms", (System.nanoTime() - started) / 1_000_000);
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                log.info("saga.step.completed", "step reserve-stock completed", fields);
                return Result.RESERVED;
            }
            if (response.statusCode() == 422 || response.statusCode() == 409) {
                fields.put("failure_reason", "INSUFFICIENT_STOCK");
                log.warn("stock.reservation.rejected", "products-api refused the reservation", fields);
                return Result.INSUFFICIENT_STOCK;
            }
            fields.put("failure_reason", "PARTICIPANT_UNAVAILABLE");
            log.error("saga.step.failed", "products-api answered " + response.statusCode(), fields);
            return Result.PARTICIPANT_UNAVAILABLE;
        } catch (HttpTimeoutException e) {
            fields.put("failure_reason", "PARTICIPANT_UNAVAILABLE");
            fields.put("duration_ms", (System.nanoTime() - started) / 1_000_000);
            log.error("saga.step.timeout", "products-api did not answer in " + timeout.toMillis() + " ms", fields);
            return Result.PARTICIPANT_UNAVAILABLE;
        } catch (IOException e) {
            fields.put("failure_reason", "PARTICIPANT_UNAVAILABLE");
            log.error("saga.step.failed", "products-api unreachable: " + e.getClass().getSimpleName(), fields);
            return Result.PARTICIPANT_UNAVAILABLE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.PARTICIPANT_UNAVAILABLE;
        }
    }
}
