package org.example.application.service;

import org.example.domain.aggregate.AccountAggregate;
import org.example.domain.commands.DepositarDinheiroCommand;
import org.example.domain.commands.SacarDinheiroCommand;
import org.example.infrastructure.EventStore;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

// o Command Handler orquestra o fluxo de comando
public class AccountCommandHandler {

    private final EventStore eventStore;

    public AccountCommandHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void handle(DepositarDinheiroCommand command) {
        process(
                command,
                command.accountId(),
                aggregate -> aggregate.handle(command) // Função que executa a lógica específica do comando
        );
    }

    public void handle(SacarDinheiroCommand command) {
        process(
                command,
                command.accountId(),
                aggregate -> aggregate.handle(command) // Função que executa a lógica específica do comando
        );
    }

    // 1. Loga o novo estado
    private <TCommand> void process(
            TCommand command,
            UUID accountId,
            Function<AccountAggregate, List<Object>> handler
    ) {
        System.out.println("2. Despachando Comando: " + command);

        AccountAggregate aggregate = eventStore.load(accountId); // 2. reconstrói Agregado (Load)

        List<Object> newEvents = handler.apply(aggregate);       // 3. executa Lógica e gera Eventos

        eventStore.save(accountId, newEvents);                   // 4. persiste Novos Eventos

        System.out.println("  -> Novo saldo (WRITE SIDE): " + aggregate.getBalance());
    }
}