package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento após um depósito bem sucedido
public record MoneyDepositedEvent(
        UUID accountId,
        BigDecimal amount,
        Instant timestamp
) {}