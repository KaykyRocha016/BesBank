package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento gerado após um saque bem-sucedido
public record DinheiroSacadoEvent(
        UUID accountId,
        BigDecimal amount,
        Instant timestamp
) {}