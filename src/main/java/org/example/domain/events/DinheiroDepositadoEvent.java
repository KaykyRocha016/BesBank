package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento após um depósito bem sucedido
public record DinheiroDepositadoEvent(
        UUID accountId,
        BigDecimal amount,
        Instant timestamp
) {}