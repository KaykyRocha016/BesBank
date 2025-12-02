package org.example.domain.commands;

import java.math.BigDecimal;
import java.util.UUID;

public class DepositCommand extends Command {

    public DepositCommand(BigDecimal amount, UUID accountId) {
        super(accountId, amount);
    }
}