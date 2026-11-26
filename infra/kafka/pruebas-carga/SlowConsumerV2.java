import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class SlowConsumerV2 {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka1:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "prueba-lag-group-v2");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                  StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");

        long total = 0;

        try (KafkaConsumer<String, String> consumer =
                 new KafkaConsumer<>(props)) {

            consumer.subscribe(Collections.singletonList("prueba.lag"));

            System.out.println("Consumidor iniciado: prueba-lag-group-v2");

            while (total < 100000) {
                ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    Thread.sleep(1);
                    total++;
                }

                if (!records.isEmpty()) {
                    consumer.commitSync();
                    System.out.println("Procesados: " + total +
                                       " | Pendientes aprox.: " +
                                       Math.max(0, 100000 - total));
                }
            }

            System.out.println("Prueba completada.");
        }
    }
}
