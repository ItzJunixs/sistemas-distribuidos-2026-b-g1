package co.edu.corhuila.opti.workflow.observability;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * W3C trace context of the request in flight in opti-workflow: trace id, own span id, parent
 * span id and the correlation id the gateway sends as X-Correlation-Id.
 */
public final class TraceContext {

    public static final String TRACEPARENT = "traceparent";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    private static final Pattern FORMAT = Pattern.compile("^00-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})$");
    private static final String ZERO_TRACE = "00000000000000000000000000000000";
    private static final String ZERO_SPAN = "0000000000000000";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private final String traceId;
    private final String spanId;
    private final String parentSpanId;
    private final String flags;
    private final String correlationId;

    private TraceContext(String traceId, String spanId, String parentSpanId, String flags, String correlationId) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.flags = flags;
        this.correlationId = correlationId;
    }

    /** First hop without a valid traceparent: a new trace starts here. */
    public static TraceContext newRoot(String correlationId) {
        return new TraceContext(randomHex(16), randomHex(8), null, "01", correlationId);
    }

    /** Reads an incoming traceparent. The span of the caller becomes the parent of our span. */
    public static Optional<TraceContext> fromTraceparent(String header, String correlationId) {
        if (header == null) {
            return Optional.empty();
        }
        Matcher m = FORMAT.matcher(header.trim().toLowerCase());
        if (!m.matches() || ZERO_TRACE.equals(m.group(1)) || ZERO_SPAN.equals(m.group(2))) {
            return Optional.empty();
        }
        return Optional.of(new TraceContext(m.group(1), randomHex(8), m.group(2), m.group(3), correlationId));
    }

    /** Span for an outbound call or a saga step: same trace, new span, this span as parent. */
    public TraceContext child() {
        return new TraceContext(traceId, randomHex(8), spanId, flags, correlationId);
    }

    public String toTraceparent() {
        return "00-" + traceId + "-" + spanId + "-" + flags;
    }

    public String traceId() { return traceId; }
    public String spanId() { return spanId; }
    public String parentSpanId() { return parentSpanId; }
    public String correlationId() { return correlationId; }

    public static TraceContext current() { return CURRENT.get(); }
    static void set(TraceContext ctx) { CURRENT.set(ctx); }
    static void clear() { CURRENT.remove(); }

    private static String randomHex(int bytes) {
        byte[] b = new byte[bytes];
        RANDOM.nextBytes(b);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }
}
