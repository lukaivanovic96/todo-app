import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TaskHandler implements HttpHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TaskRepository repo;
    private final KafkaEventProducer kafka;

    public TaskHandler(TaskRepository repo, KafkaEventProducer kafka) {
        this.repo = repo;
        this.kafka = kafka;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/api/tasks")) {
            handleGetAll(exchange);
        } else if (method.equals("POST") && path.equals("/api/tasks")) {
            handlePost(exchange);
        } else if (method.equals("DELETE") && path.matches("/api/tasks/\\d+")) {
            handleDelete(exchange, extractId(path));
        } else {
            send(exchange, 404, Map.of("error", "Not Found"));
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        send(exchange, 200, repo.findAll());
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Map body = MAPPER.readValue(exchange.getRequestBody(), Map.class);
        String title = (String) body.get("title");
        if (title == null || title.isBlank()) {
            send(exchange, 400, Map.of("error", "title is required"));
            return;
        }
        Task task = repo.save(title);
        kafka.send("task.created", task);
        send(exchange, 201, task);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        Task task = repo.findById(id);
        boolean deleted = repo.deleteById(id);
        if (!deleted) {
            send(exchange, 404, Map.of("error", "Task not found"));
            return;
        }
        kafka.send("task.deleted", task);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private int extractId(String path) {
        return Integer.parseInt(path.split("/")[3]);
    }

    private void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
