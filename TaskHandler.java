import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler implements HttpHandler {

    private final TaskRepository repo;

    public TaskHandler(TaskRepository repo) {
        this.repo = repo;
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
            int id = extractId(path);
            handleDelete(exchange, id);
        } else {
            send(exchange, 404, "Not Found");
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        List<Task> tasks = repo.findAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            json.append("{\"id\":").append(t.getId())
                .append(",\"title\":\"").append(t.getTitle()).append("\"")
                .append(",\"completed\":").append(t.isCompleted()).append("}");
            if (i < tasks.size() - 1) json.append(",");
        }
        json.append("]");
        send(exchange, 200, json.toString());
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String title = parseTitle(body);
        if (title == null) {
            send(exchange, 400, "{\"error\":\"title is required\"}");
            return;
        }
        Task task = repo.save(title);
        String json = "{\"id\":" + task.getId() + ",\"title\":\"" + task.getTitle() + "\",\"completed\":false}";
        send(exchange, 201, json);
    }

    private void handleDelete(HttpExchange exchange, int id) throws IOException {
        boolean deleted = repo.deleteById(id);
        if (!deleted) {
            send(exchange, 404, "{\"error\":\"Task not found\"}");
            return;
        }
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String parseTitle(String json) {
        int start = json.indexOf("\"title\"");
        if (start == -1) return null;
        int colon = json.indexOf(":", start);
        int firstQuote = json.indexOf("\"", colon);
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (firstQuote == -1 || secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
