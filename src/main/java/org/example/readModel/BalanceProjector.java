package org.example.readModel;

import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// o Projector transforma eventos em estado de leitura (Read Model Updater)
public class BalanceProjector {

    // simula o Read DB (armazenamento otimizado para leitura)
    private final Map<UUID, BigDecimal> readDatabase = new HashMap<>();

    public void project(Event event) {
        System.out.println("9. Processando Projeção (Evento): " + event.getClass().getSimpleName());
        var currentBalance = readDatabase.getOrDefault(event.getAccountId(),BigDecimal.ZERO);
        if (event instanceof MoneyDepositedEvent e) {
            BigDecimal newBalance = currentBalance.add(e.getAmount());
            readDatabase.put(e.getAccountId(), newBalance);
        } else if (event instanceof WithdrawMoneyEvent e) {
            BigDecimal newBalance = currentBalance.subtract(e.getAmount());
            readDatabase.put(e.getAccountId(), newBalance);
        }

        // 10. salva o Estado Atual (Update Saldo no Read DB)

        System.out.println("  -> Novo saldo (READ SIDE): " + readDatabase.get(getAccountId(event)));
    }

    // simula a consulta da Read API (GET /saldo)
    public AccountBalanceProjection getBalance(UUID accountId) {
        BigDecimal balance = readDatabase.getOrDefault(accountId, BigDecimal.ZERO);
        System.out.println("11-12. Consulta Saldo: Saldo no Read DB para " + accountId + ": " + balance);
        return new AccountBalanceProjection(accountId, balance);
    }

    private UUID getAccountId(Event event) {
        return event.getAccountId();
    }
}