import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        TaskRepository repo = new TaskRepository();
        repo.save("Kupiti mleko");
        repo.save("Nauciti Kafku");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HtmlHandler());
        server.createContext("/api/tasks", new TaskHandler(repo));
        server.start();

        System.out.println("Server running on http://localhost:8080");
    }
}
