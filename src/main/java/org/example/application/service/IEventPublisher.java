package org.example.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.domain.events.Event;

public interface IEventPublisher {
    public void publish(Event publish) throws JsonProcessingException;
    public void close();

}
