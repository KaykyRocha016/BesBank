package org.example.infrastructure;

import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.events.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventStore implements IEventStore {
    // aqui ta servindo para simular o banco de dados de Event Store
    // (chave: ID da conta, valor: stream de eventos)
    private final Map<UUID, List<Event>> eventStreams = new HashMap<>();

    public AccountAggregate load(UUID accountId) {
        AccountAggregate aggregate = new AccountAggregate(accountId);
        var history = eventStreams.getOrDefault(accountId, new ArrayList<>());

        // reconstrói o estado aplicando todos os eventos
        history.forEach(aggregate::apply);

        System.out.println("  -> Agregado reconstruído. Eventos aplicados: " + history.size());
        return aggregate;
    }

    public void save(UUID accountId, List<Event> events) {
        eventStreams.computeIfAbsent(accountId, k -> new ArrayList<>()).addAll(events);
        System.out.println("  -> Eventos persistidos no Event Store: " + events.size());

        // se fosse um trem de verddade
        // em um sistema real, aqui o Event Publisher publicaria no Broker (Kafka)
    }

    public List<Event> getAllEvents() {
        // retorna todos os eventos em ordem de ocorrência (simulação simplificada)
        List<Event> all = new ArrayList<>();
        eventStreams.values().forEach(all::addAll);
        return all;
    }
}