package org.example.application.projection;

import org.example.domain.events.Event;
import org.example.domain.events.MoneyDepositedEvent;
import org.example.domain.events.WithdrawMoneyEvent;
import org.example.infrastructure.jpa.read.AccountReadEntity;
import org.example.infrastructure.jpa.read.AccountReadJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountProjection {
    private final AccountReadJpaRepository readRepository;

    public AccountProjection(AccountReadJpaRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Transactional("readTransactionManager")
    public void handle(Event event) {
        if (event instanceof MoneyDepositedEvent e) {
            applyDeposit(e);
        } else if (event instanceof WithdrawMoneyEvent e) {
            applyWithdraw(e);
        }
    }

    private void applyDeposit(MoneyDepositedEvent event) {
        var readModel = readRepository.findByAccountId(event.getAccountId())
                .orElseGet(() -> new AccountReadEntity(event.getAccountId()));

        readModel.setBalance(readModel.getBalance().add(event.getAmount()));
        readModel.setVersion(readModel.getVersion() + 1);

        readRepository.save(readModel);
        System.out.println("✅ Projeção atualizada: Depósito de " + event.getAmount()
                + " | Novo saldo (READ SIDE): " + readModel.getBalance());
    }

    private void applyWithdraw(WithdrawMoneyEvent event) {
        var readModel = readRepository.findByAccountId(event.getAccountId())
                .orElseThrow(() -> new IllegalStateException("Conta não encontrada no READ MODEL"));

        readModel.setBalance(readModel.getBalance().subtract(event.getAmount()));
        readModel.setVersion(readModel.getVersion() + 1);

        readRepository.save(readModel);
        System.out.println("✅ Projeção atualizada: Saque de " + event.getAmount()
                + " | Novo saldo (READ SIDE): " + readModel.getBalance());
    }
}