package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento após um depósito bem sucedido
public class MoneyDepositedEvent extends  Event {

    public MoneyDepositedEvent(UUID accountId, BigDecimal amount, Instant timestamp) {
        super(accountId, amount, timestamp);
    }
}
