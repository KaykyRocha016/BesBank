package org.example.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.application.service.IEventPublisher;
import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.events.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventStore implements IEventStore {
    private final Map<UUID, List<Event>> eventStreams = new HashMap<>();
    private final IEventPublisher eventPublisher;

    public EventStore(IEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AccountAggregate load(UUID accountId) {
        AccountAggregate aggregate = new AccountAggregate(accountId);
        var history = eventStreams.getOrDefault(accountId, new ArrayList<>());
        history.forEach(aggregate::apply);
        System.out.println("  -> Agregado reconstruído. Eventos aplicados: " + history.size());
        return aggregate;
    }

    @Override
    public void save(UUID accountId, List<Event> events) throws InternalError {
        // 1. Persiste no Event Store
        eventStreams.computeIfAbsent(accountId, k -> new ArrayList<>()).addAll(events);
        System.out.println("  -> Eventos persistidos no Event Store: " + events.size());

        // 2. Publica cada evento no Kafka

        events.forEach(event -> {
            try {
                eventPublisher.publish(event);
            } catch (JsonProcessingException e) {
                throw new InternalError(e);
            }
        });

    }

    @Override
    public List<Event> getAllEvents() {
        List<Event> all = new ArrayList<>();
        eventStreams.values().forEach(all::addAll);
        return all;
    }
}