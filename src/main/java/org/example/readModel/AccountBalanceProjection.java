package org.example.readModel;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceProjection(
        UUID accountId,
        BigDecimal currentBalance
) {}