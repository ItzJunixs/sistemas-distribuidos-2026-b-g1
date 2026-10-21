package co.edu.corhuila.opti.workflow.observability;

import java.io.PrintStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Writes one JSON line per event in the format of 02-log-contract.md. */
public final class StructuredLogger {

    /** Fields from the "never log" list. If someone passes them, the value is masked. */
    private static final Set<String> NEVER_LOG = new HashSet<>(Arrays.asList(
            "full_name", "document_number", "formula", "phone", "email", "authorization", "password"));

    private final String service;
    private final String version;
    private final String environment;
    private volatile PrintStream out;

    public StructuredLogger(String service, String version, String environment, PrintStream out) {
        this.service = service;
        this.version = version;
        this.environment = environment;
        this.out = out;
    }

    public static StructuredLogger fromEnv(PrintStream out) {
        return new StructuredLogger("workflow-api", env("APP_VERSION", "1.0.0"), env("APP_ENV", "local"), out);
    }

    public void info(String event, String message, Map<String, Object> fields) { write("INFO", event, message, fields); }
    public void warn(String event, String message, Map<String, Object> fields) { write("WARN", event, message, fields); }
    public void error(String event, String message, Map<String, Object> fields) { write("ERROR", event, message, fields); }

    void write(String level, String event, String message, Map<String, Object> fields) {
        TraceContext ctx = TraceContext.current();
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("timestamp", Instant.now().truncatedTo(ChronoUnit.MILLIS).toString());
        line.put("level", level);
        line.put("service", service);
        line.put("version", version);
        line.put("environment", environment);
        line.put("trace_id", ctx == null ? null : ctx.traceId());
        line.put("span_id", ctx == null ? null : ctx.spanId());
        line.put("correlation_id", ctx == null ? null : ctx.correlationId());
        line.put("event", event);
        line.put("message", message);
        if (fields != null) {
            fields.forEach((k, v) -> line.put(k, NEVER_LOG.contains(k) ? "***" : v));
        }
        out.println(toJson(line));
    }

    static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
        }
        return sb.append('}').toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return v == null || v.isBlank() ? fallback : v;
    }
}
