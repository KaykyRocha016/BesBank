package org.example.application.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.application.service.IEventPublisher;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.events.Event;

import java.util.Properties;

public class EventPublisher implements IEventPublisher {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public EventPublisher(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // garante durabilidade
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // evita duplicatas
        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static final String TOPIC_NAME = "account-events";

    @Override
    public void publish(Event event) throws JsonProcessingException {

        var eventJson = objectMapper.writeValueAsString(event);
        var eventType = event.getClass().getSimpleName();
        ProducerRecord<String, String> record = new ProducerRecord<>(
                TOPIC_NAME,
                event.getAccountId().toString(),
                eventJson
        );

        record.headers().add("eventType", eventType.getBytes());

        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("  -> Evento publicado no Kafka: " + eventType +
                        " | Partition: " + metadata.partition() +
                        " | Offset: " + metadata.offset());
            } else {
                System.err.println("Erro ao publicar evento: " + exception.getMessage());
            }
        });
    }

    @Override
    public void close() {
        producer.flush();
        producer.close();
    }
}
