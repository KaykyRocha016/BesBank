package org.example.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// evento gerado após um saque bem-sucedido
public class WithdrawMoneyEvent extends Event{

    public WithdrawMoneyEvent(UUID accountId, BigDecimal amount, Instant timestamp) {
        super(accountId, amount, timestamp);
    }

    public WithdrawMoneyEvent() {
        super();
    }
}