package org.example.infrastructure;

import org.example.domain.aggregate.AccountAggregate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventStore {
    // aqui ta servindo para simular o banco de dados de Event Store
    // (chave: ID da conta, valor: stream de eventos)
    private final Map<UUID, List<Object>> eventStreams = new HashMap<>();

    public AccountAggregate load(UUID accountId) {
        AccountAggregate aggregate = new AccountAggregate(accountId);
        List<Object> history = eventStreams.getOrDefault(accountId, new ArrayList<>());

        // reconstrói o estado aplicando todos os eventos
        history.forEach(aggregate::apply);

        System.out.println("  -> Agregado reconstruído. Eventos aplicados: " + history.size());
        return aggregate;
    }

    public void save(UUID accountId, List<Object> newEvents) {
        eventStreams.computeIfAbsent(accountId, k -> new ArrayList<>()).addAll(newEvents);
        System.out.println("  -> Eventos persistidos no Event Store: " + newEvents.size());

        // se fosse um trem de verddade
        // em um sistema real, aqui o Event Publisher publicaria no Broker (Kafka)
    }

    public List<Object> getAllEvents() {
        // retorna todos os eventos em ordem de ocorrência (simulação simplificada)
        List<Object> all = new ArrayList<>();
        eventStreams.values().forEach(all::addAll);
        return all;
    }
}