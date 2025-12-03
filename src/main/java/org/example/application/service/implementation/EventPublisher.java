package org.example.application.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.application.service.IEventPublisher;
import org.example.domain.events.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Properties;

@Component
public class EventPublisher implements IEventPublisher {
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topic.events:account-events}")
    private String topicName;

    public EventPublisher() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        this.producer = new KafkaProducer<>(props);
        System.out.println("EventPublisher inicializado com servidor: " + bootstrapServers);
    }

    @Override
    public void publish(Event event) throws JsonProcessingException {
        var eventJson = objectMapper.writeValueAsString(event);
        var eventType = event.getClass().getSimpleName();

        ProducerRecord<String, String> record = new ProducerRecord<>(
                topicName,
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
    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("EventPublisher fechado");
        }
    }
}
