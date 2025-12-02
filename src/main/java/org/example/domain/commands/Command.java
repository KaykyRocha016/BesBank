package org.example.domain.commands;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Command{
    BigDecimal amount;
    UUID accountId;

    public Command (UUID accountId, BigDecimal amount) {
        this.amount = amount;
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
