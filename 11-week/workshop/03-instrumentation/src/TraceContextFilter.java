package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Same job as the CorrelationFilter of opti-workflow, plus W3C trace context: reads the incoming
 * traceparent or starts a new trace, returns it in the response and writes one access line.
 */
public final class TraceContextFilter implements HttpHandler {

    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,64}$");

    private final HttpHandler next;
    private final StructuredLogger log;

    public TraceContextFilter(HttpHandler next, StructuredLogger log) {
        this.next = next;
        this.log = log;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String incomingCorrelation = exchange.getRequestHeaders().getFirst(TraceContext.CORRELATION_HEADER);
        String correlationId = incomingCorrelation != null && SAFE_ID.matcher(incomingCorrelation).matches()
                ? incomingCorrelation : UUID.randomUUID().toString();
        String traceparent = exchange.getRequestHeaders().getFirst(TraceContext.TRACEPARENT);
        TraceContext ctx = TraceContext.fromTraceparent(traceparent, correlationId)
                .orElseGet(() -> TraceContext.newRoot(correlationId));

        TraceContext.set(ctx);
        exchange.getResponseHeaders().set(TraceContext.TRACEPARENT, ctx.toTraceparent());
        exchange.getResponseHeaders().set(TraceContext.CORRELATION_HEADER, correlationId);
        long started = System.nanoTime();
        try {
            next.handle(exchange);
        } finally {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("http_method", exchange.getRequestMethod());
            fields.put("http_route", exchange.getHttpContext().getPath());
            fields.put("http_status", exchange.getResponseCode());
            fields.put("duration_ms", (System.nanoTime() - started) / 1_000_000);
            log.info("http.request", exchange.getRequestMethod() + " " + exchange.getHttpContext().getPath()
                    + " -> " + exchange.getResponseCode(), fields);
            TraceContext.clear();
        }
    }
}
