package org.example.domain.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawCommand(
        UUID accountId,
        BigDecimal amount
) {}