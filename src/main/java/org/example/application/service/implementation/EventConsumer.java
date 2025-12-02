package org.example.application.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.example.application.service.IEventConsumer;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;
import org.example.readModel.BalanceProjector;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class EventConsumer implements IEventConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final ObjectMapper objectMapper;
    private final BalanceProjector projector;

    public EventConsumer(String bootstrapServers, BalanceProjector projector) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "balance-projector-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // commit manual

        this.consumer = new KafkaConsumer<>(props);
        this.consumer.subscribe(Collections.singletonList("account-events"));

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.projector = projector;
    }
    @Override
    public void startConsuming() {
        Thread consumerThread = new Thread(() -> {
            try {
                while (running) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        processEvent(record);
                    }

                    // Commit após processar todos os eventos do batch
                    consumer.commitSync();
                }
            } catch (Exception e) {
                System.err.println("Erro no consumer: " + e.getMessage());
            } finally {
                consumer.close();
            }
        });

        consumerThread.start();
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

    public void stop() {
        running = false;
    }


    private volatile boolean running = true;


}
