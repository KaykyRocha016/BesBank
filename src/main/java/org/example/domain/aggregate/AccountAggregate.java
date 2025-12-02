package org.example.domain.aggregate;

import org.example.domain.commands.Command;
import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AccountAggregate {
    private final UUID accountId;
    private BigDecimal balance = BigDecimal.ZERO;

    public AccountAggregate(UUID accountId) {
        this.accountId = accountId;
    }

    // metodo para reconstruir o estado a partir dos eventos (Event Sourcing)
    public void apply(Event event) {
        this.balance = this.balance.add(event.getAmount());
    }

    // lógica para lidar com o comando de Depósito
    public List<Event> handle(Command command) {
        //algum dos amigoões adicionar mais alguma lógica aqui tipo pra negativo
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        // gera o evento
        MoneyDepositedEvent event = new MoneyDepositedEvent(
                command.getAccountId(),
                command.getAmount(),
                Instant.now());

        // aplica o evento (atualiza o estado)
        apply(event);

        return List.of(event);
    }
    public UUID getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
}