package org.example.application.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.example.application.service.IEventConsumer;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;
import org.example.readModel.BalanceProjector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Component
public class EventConsumer implements IEventConsumer {
    private KafkaConsumer<String, String> consumer;
    private final ObjectMapper objectMapper;
    private final BalanceProjector projector;
    private volatile boolean running = false;
    private Thread consumerThread;

    @Value("${kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topic.events:account-events}")
    private String topicName;

    @Value("${kafka.consumer.group-id:balance-projector-group}")
    private String groupId;

    // Spring injeta o BalanceProjector automaticamente
    public EventConsumer(BalanceProjector projector) {
        this.projector = projector;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    @Override
    public void startConsuming() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList(topicName));
        this.running = true;

        consumerThread = new Thread(() -> {
            try {
                while (running) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        processEvent(record);
                    }

                    if (!records.isEmpty()) {
                        consumer.commitSync();
                    }
                }
            } catch (Exception e) {
                if (running) {
                    System.err.println("Erro no consumer: " + e.getMessage());
                }
            } finally {
                consumer.close();
            }
        });

        consumerThread.start();
        System.out.println("EventConsumer iniciado, ouvindo tópico: " + topicName);
    }

    @Override
    public void processEvent(ConsumerRecord<String, String> record) {
        try {
            String eventType = new String(record.headers().lastHeader("eventType").value());
            Event event = deserializeEvent(record.value(), eventType);

            if (event != null) {
                System.out.println("  -> Evento recebido do Kafka: " + eventType);
                projector.project(event);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar evento: " + e.getMessage());
        }
    }

    private Event deserializeEvent(String json, String eventType) throws Exception {
        return switch (eventType) {
            case "MoneyDepositedEvent" -> objectMapper.readValue(json, MoneyDepositedEvent.class);
            case "WithdrawMoneyEvent" -> objectMapper.readValue(json, WithdrawMoneyEvent.class);
            default -> null;
        };
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (consumerThread != null) {
            try {
                consumerThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("EventConsumer parado");
    }
}
