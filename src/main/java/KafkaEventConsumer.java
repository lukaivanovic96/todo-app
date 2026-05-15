import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class KafkaEventConsumer implements Runnable {

    private static final String TOPIC = "task-events";
    private final KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public KafkaEventConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "todo-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // Čita od početka kad se consumer group prvi put spoji
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumer = new KafkaConsumer<>(props);
    }

    @Override
    public void run() {
        consumer.subscribe(List.of(TOPIC));
        System.out.println("[Kafka Consumer] Slušam topic: " + TOPIC);

        while (running) {
            // poll čeka max 1 sekundu na nove poruke, zatim vraća batch (može biti prazan)
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

            records.forEach(record -> {
                System.out.printf(
                    "[Kafka Consumer] topic=%s | partition=%d | offset=%d | key=%s | value=%s%n",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value()
                );
            });
        }

        consumer.close();
    }

    public void stop() {
        running = false;
    }
}
