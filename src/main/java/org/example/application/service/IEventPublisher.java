package org.example.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.domain.events.Event;

public interface IEventPublisher {
    void publish(Event publish) throws JsonProcessingException;
    void close();

}
