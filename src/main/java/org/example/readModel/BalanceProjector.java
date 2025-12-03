package org.example.readModel;

import org.example.domain.events.*;
import org.example.infrastructure.jpa.read.AccountReadEntity;
import org.example.infrastructure.jpa.read.AccountReadJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class BalanceProjector {

    private final AccountReadJpaRepository readRepository;

    public BalanceProjector(AccountReadJpaRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Transactional("readTransactionManager") // se tiver transaction manager separado
    public void project(Event event) {
        System.out.println("9. Processando Projeção (Evento): " + event.getClass().getSimpleName());

        AccountReadEntity readEntity = readRepository
                .findByAccountId(event.getAccountId())
                .orElseGet(() -> {
                    AccountReadEntity newEntity = new AccountReadEntity();
                    newEntity.setAccountId(event.getAccountId());
                    newEntity.setBalance(BigDecimal.ZERO);
                    return newEntity;
                });

        BigDecimal currentBalance = readEntity.getBalance();

        if (event instanceof MoneyDepositedEvent e) {
            BigDecimal newBalance = currentBalance.add(e.getAmount());
            readEntity.setBalance(newBalance);
            System.out.println("  -> Depósito aplicado: " + e.getAmount() + " | Novo saldo: " + newBalance);

        } else if (event instanceof WithdrawMoneyEvent e) {
            BigDecimal newBalance = currentBalance.subtract(e.getAmount());
            readEntity.setBalance(newBalance);
            System.out.println("  -> Saque aplicado: " + e.getAmount() + " | Novo saldo: " + newBalance);
        }

        readRepository.save(readEntity);
        System.out.println("  -> Saldo atualizado no Read DB para conta: " + event.getAccountId());
    }


    public AccountBalanceProjection getBalance(UUID accountId) {
        AccountReadEntity entity = readRepository
                .findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: " + accountId));

        System.out.println("Consulta Saldo: " + accountId + " = " + entity.getBalance());
        return new AccountBalanceProjection(accountId, entity.getBalance());
    }
}
