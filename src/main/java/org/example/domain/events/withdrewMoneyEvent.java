package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento gerado após um saque bem-sucedido
public class withdrewMoneyEvent extends Event{

    public withdrewMoneyEvent(UUID accountId, BigDecimal amount, Instant timestamp) {
        super(accountId, amount, timestamp);
    }
}