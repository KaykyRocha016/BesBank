package org.example.readModel;

import java.math.BigDecimal;
import java.util.UUID;

// projeção simples para armazenar o saldo atual de uma conta
public record AccountBalanceProjection(
        UUID accountId,
        BigDecimal currentBalance
) {}