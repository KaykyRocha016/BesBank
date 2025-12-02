package org.example.readModel;

import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.withdrewMoneyEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// o Projector transforma eventos em estado de leitura (Read Model Updater)
public class BalanceProjector {

    // simula o Read DB (armazenamento otimizado para leitura)
    private final Map<UUID, BigDecimal> readDatabase = new HashMap<>();

    public void project(Object event) {
        System.out.println("9. Processando Projeção (Evento): " + event.getClass().getSimpleName());

        if (event instanceof MoneyDepositedEvent e) {
            BigDecimal currentBalance = readDatabase.getOrDefault(e.accountId(), BigDecimal.ZERO);
            BigDecimal newBalance = currentBalance.add(e.amount());
            readDatabase.put(e.accountId(), newBalance);
        } else if (event instanceof withdrewMoneyEvent e) {
            BigDecimal currentBalance = readDatabase.getOrDefault(e.accountId(), BigDecimal.ZERO);
            BigDecimal newBalance = currentBalance.subtract(e.amount());
            readDatabase.put(e.accountId(), newBalance);
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

    private UUID getAccountId(Object event) {
        if (event instanceof MoneyDepositedEvent e) return e.accountId();
        if (event instanceof withdrewMoneyEvent e) return e.accountId();
        return null;
    }
}