import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        TaskRepository repo = new TaskRepository();
        repo.save("Kupiti mleko");
        repo.save("Nauciti Kafku");

        KafkaEventProducer kafka = new KafkaEventProducer();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HtmlHandler());
        server.createContext("/api/tasks", new TaskHandler(repo, kafka));
        server.start();

        System.out.println("Server running on http://localhost:8080");

        // Consumer radi u pozadinskom threadu — ne blokira HTTP server
        KafkaEventConsumer consumer = new KafkaEventConsumer();
        Thread consumerThread = new Thread(consumer, "kafka-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
    }
}
