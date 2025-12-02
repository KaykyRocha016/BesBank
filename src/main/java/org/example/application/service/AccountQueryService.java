package org.example.application.service;

import org.example.infrastructure.jpa.read.AccountReadEntity;
import org.example.infrastructure.jpa.read.AccountReadJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountQueryService {
    private final AccountReadJpaRepository readRepository;

    public AccountQueryService(AccountReadJpaRepository readRepository) {
        this.readRepository = readRepository;
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        var readModel = readRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("Conta não encontrada"));

        return readModel.getBalance();
    }

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public AccountReadEntity getAccountInfo(UUID accountId) {
        return readRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("Conta não encontrada"));
    }
}