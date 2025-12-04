package org.example.domain.aggregate;

import lombok.Getter;
import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class AccountAggregate {
    private final UUID accountId;
    private BigDecimal balance = BigDecimal.ZERO;

    public AccountAggregate(UUID accountId) {
        this.accountId = accountId;
    }

    public void apply(Event event) {
        if (event instanceof MoneyDepositedEvent) {
            this.balance = this.balance.add(event.getAmount());
        } else if (event instanceof WithdrawMoneyEvent) {
            this.balance = this.balance.subtract(event.getAmount());
        }
    }

    public List<Event> handle(DepositCommand command) {
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        MoneyDepositedEvent event = new MoneyDepositedEvent(
                command.getAccountId(),
                command.getAmount(),
                Instant.now()
        );

        apply(event);
        return List.of(event);
    }

    public List<Event> handle(WithdrawCommand command) {
        if (command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do saque deve ser positivo.");
        }

        if (this.balance.compareTo(command.getAmount()) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }

        WithdrawMoneyEvent event = new WithdrawMoneyEvent(
                command.getAccountId(),
                command.getAmount(),
                Instant.now()
        );

        apply(event);
        return List.of(event);
    }
}
