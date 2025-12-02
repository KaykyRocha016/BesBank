package org.example.domain.aggregate;

import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.withdrewMoneyEvent;

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
        if (event instanceof MoneyDepositedEvent e) {
            this.balance = this.balance.add(e.getAmount());
        } else if (event instanceof withdrewMoneyEvent e) {
            this.balance = this.balance.subtract(e.getAmount());
        }
    }

    // lógica para lidar com o comando de Depósito
    public List<Event> handle(DepositCommand command) {
        //algum dos amigoões adicionar mais alguma lógica aqui tipo pra negativo
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        // gera o evento
        MoneyDepositedEvent event = new MoneyDepositedEvent(
                command.accountId(),
                command.amount(),
                Instant.now());

        // aplica o evento (atualiza o estado)
        apply(event);

        return List.of(event);
    }

    // lógica para lidar com o comando de Saque
    public List<Event> handle(WithdrawCommand command) {
        // Validação de negócio: Não pode sacar mais do que o saldo
        //algum dos amigoões adicionar mais alguma lógica aqui tipo pra negativo e coisas q achar necessario pls
        if (command.amount().compareTo(balance) > 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }

        // gera o evento
        withdrewMoneyEvent event = new withdrewMoneyEvent(
                command.accountId(),
                command.amount(),
                Instant.now());

        // aplica o evento
        apply(event);

        return List.of(event);
    }

    // Getters para fins de demonstração
    public UUID getAccountId() { return accountId; }
    public BigDecimal getBalance() { return balance; }
}