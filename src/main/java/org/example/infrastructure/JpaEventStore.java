package org.example.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.application.projection.AccountProjection;
import org.example.application.service.IEventPublisher;
import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;
import org.example.infrastructure.jpa.write.AccountEventEntity;
import org.example.infrastructure.jpa.write.AccountEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class JpaEventStore implements IEventStore {

    private final AccountEventJpaRepository eventRepository;
    private final IEventPublisher eventPublisher;

    public JpaEventStore(
            AccountEventJpaRepository eventRepository,
            IEventPublisher eventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AccountAggregate load(UUID accountId) {
        var events = eventRepository.findByAccountIdOrderByIdAsc(accountId);

        var aggregate = new AccountAggregate(accountId);
        events.forEach(event -> {
            Event domainEvent = toDomainEvent(event);
            aggregate.apply(domainEvent);
        });

        return aggregate;
    }

    @Override
    @Transactional("writeTransactionManager")
    public void save(UUID accountId, List<Event> events) {
        for (Event event : events) {
            AccountEventEntity entity = new AccountEventEntity();
            entity.setAccountId(event.getAccountId());
            entity.setAmount(event.getAmount().toPlainString());
            entity.setTimestamp(event.getTimestamp());
            entity.setType(toType(event));
            eventRepository.save(entity);
        }

        events.forEach(event -> {
            try {
                eventPublisher.publish(event);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro ao publicar evento no Kafka", e);
            }
        });
    }

    @Override
    public List<Event> getAllEvents() {
        return List.of();
    }

    private String toType(Event e) {
        if (e instanceof MoneyDepositedEvent) return "MONEY_DEPOSITED";
        if (e instanceof WithdrawMoneyEvent) return "MONEY_WITHDRAWN";
        throw new IllegalArgumentException("Tipo de evento desconhecido: " + e.getClass());
    }

    private Event toDomainEvent(AccountEventEntity entity) {
        var amount = new BigDecimal(entity.getAmount());

        return switch (entity.getType()) {
            case "MONEY_DEPOSITED" ->
                    new MoneyDepositedEvent(entity.getAccountId(), amount, entity.getTimestamp());
            case "MONEY_WITHDRAWN" ->
                    new WithdrawMoneyEvent(entity.getAccountId(), amount, entity.getTimestamp());
            default -> throw new IllegalArgumentException("Evento desconhecido: " + entity.getType());
        };
    }
}