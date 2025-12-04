package org.example.application.service;

import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.example.domain.events.Event;
import org.example.infrastructure.IEventStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class AccountCommandHandler {

    private final IEventStore eventStore;

    public AccountCommandHandler(IEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void handle(DepositCommand command) {
        process(
                command,
                command.getAccountId(),
                aggregate -> aggregate.handle(command)
        );
    }

    public void handle(WithdrawCommand command) {
        process(
                command,
                command.getAccountId(),
                aggregate -> aggregate.handle(command)
        );
    }

    private <TCommand> void process(
            TCommand command,
            UUID accountId,
            Function<AccountAggregate, List<Event>> handler
    ) {
        System.out.println("📥 Despachando Comando: " + command.getClass().getSimpleName());

        AccountAggregate aggregate = eventStore.load(accountId);
        List<Event> newEvents = handler.apply(aggregate);
        eventStore.save(accountId, newEvents);

        System.out.println("✅ Novo saldo (WRITE SIDE): " + aggregate.getBalance());
    }
}