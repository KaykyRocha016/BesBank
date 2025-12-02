package org.example.application.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IEventConsumer {
    void startConsuming();
    void processEvent(ConsumerRecord<String, String> record);
    void stop();
}
