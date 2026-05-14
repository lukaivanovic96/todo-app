import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HtmlHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Tasks</title>
                <style>
                    body { font-family: Arial; margin: 20px; }
                    ul { list-style: none; padding: 0; }
                    li { padding: 10px; border-bottom: 1px solid #ddd; }
                    input { padding: 8px; width: 300px; }
                    button { padding: 8px 15px; }
                </style>
            </head>
            <body>
                <h1>My Tasks</h1>
                <div>
                    <input type="text" id="title" placeholder="Add a new task...">
                    <button onclick="addTask()">Add</button>
                </div>
                <ul id="taskList"></ul>
                <script>
                    async function loadTasks() {
                        const res = await fetch('/api/tasks');
                        const tasks = await res.json();
                        const list = document.getElementById('taskList');
                        list.innerHTML = '';
                        tasks.forEach(t => {
                            const li = document.createElement('li');
                            li.textContent = '[' + (t.completed ? 'x' : ' ') + '] ' + t.title;
                            list.appendChild(li);
                        });
                    }

                    async function addTask() {
                        const input = document.getElementById('title');
                        if (!input.value) return;
                        await fetch('/api/tasks', {
                            method: 'POST',
                            body: JSON.stringify({title: input.value})
                        });
                        input.value = '';
                        loadTasks();
                    }

                    loadTasks();
                </script>
            </body>
            </html>
            """;

        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
