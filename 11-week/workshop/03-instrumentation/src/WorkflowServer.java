package co.edu.corhuila.opti.workflow.observability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Minimal opti-workflow: POST /api/v1/sagas/place-order behind the trace-context filter.
 * Only the saga entry point of the place-order journey, to show the instrumentation.
 */
public final class WorkflowServer {

    private static final Pattern PRODUCT_ID = Pattern.compile("\"productId\"\\s*:\\s*\"([0-9a-fA-F-]{36})\"");

    private final HttpServer server;
    private final StructuredLogger log;

    public WorkflowServer(int port, StructuredLogger log) throws IOException {
        this.log = log;
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/v1/sagas/place-order", new TraceContextFilter(this::placeOrder, log));
    }

    public int port() { return server.getAddress().getPort(); }
    public void start() { server.start(); }
    public void stop() { server.stop(0); }

    private void placeOrder(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            send(ex, 405, "{\"code\":\"METHOD_NOT_ALLOWED\"}");
            return;
        }
        Matcher m = PRODUCT_ID.matcher(readBody(ex));
        if (!m.find()) {
            send(ex, 400, "{\"code\":\"VALIDATION\",\"field\":\"productId\"}");
            return;
        }
        String sagaId = UUID.randomUUID().toString();
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("saga_id", sagaId);
        fields.put("product_type", "FRAME");
        fields.put("product_id", m.group(1));
        fields.put("attempt", 1);
        log.info("saga.started", "place-order saga started", fields);
        send(ex, 202, "{\"id\":\"" + sagaId + "\",\"type\":\"PLACE_ORDER\",\"status\":\"RUNNING\"}");
    }

    static String readBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8085"));
        StructuredLogger log = StructuredLogger.fromEnv(System.out);
        WorkflowServer app = new WorkflowServer(port, log);
        app.start();
        log.info("app.started", "listening on http://127.0.0.1:" + app.port(), null);
    }
}
