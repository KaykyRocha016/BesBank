package org.example.infrastructure;

import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.events.Event;

import java.util.List;
import java.util.UUID;

public interface IEventStore {
    AccountAggregate load(UUID accountId);
    void save(UUID accountId, List<Event> events);
    List<Event> getAllEvents();
}
