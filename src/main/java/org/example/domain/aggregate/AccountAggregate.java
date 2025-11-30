package org.example.domain.aggregate;

import org.example.domain.commands.DepositarDinheiroCommand;
import org.example.domain.commands.SacarDinheiroCommand;
import org.example.domain.events.DinheiroDepositadoEvent;
import org.example.domain.events.DinheiroSacadoEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountAggregate {
    private final UUID accountId;
    private BigDecimal balance = BigDecimal.ZERO;

    public AccountAggregate(UUID accountId) {
        this.accountId = accountId;
    }

    // metodo para reconstruir o estado a partir dos eventos (Event Sourcing)
    public void apply(Object event) {
        if (event instanceof DinheiroDepositadoEvent e) {
            this.balance = this.balance.add(e.amount());
        } else if (event instanceof DinheiroSacadoEvent e) {
            this.balance = this.balance.subtract(e.amount());
        }
    }

    // lógica para lidar com o comando de Depósito
    public List<Object> handle(DepositarDinheiroCommand command) {
        //algum dos amigoões adicionar mais alguma lógica aqui tipo pra negativo
        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser positivo.");
        }

        // gera o evento
        DinheiroDepositadoEvent event = new DinheiroDepositadoEvent(
                command.accountId(),
                command.amount(),
                Instant.now());

        // aplica o evento (atualiza o estado)
        apply(event);

        return List.of(event);
    }

    // lógica para lidar com o comando de Saque
    public List<Object> handle(SacarDinheiroCommand command) {
        // Validação de negócio: Não pode sacar mais do que o saldo
        //algum dos amigoões adicionar mais alguma lógica aqui tipo pra negativo e coisas q achar necessario pls
        if (command.amount().compareTo(balance) > 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }

        // gera o evento
        DinheiroSacadoEvent event = new DinheiroSacadoEvent(
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