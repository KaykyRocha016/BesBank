package org.example.domain.commands;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawCommand extends Command {
    public WithdrawCommand(BigDecimal amount, UUID accountId) {
        super(accountId, amount);
    }
}