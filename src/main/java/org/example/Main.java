package org.example;

import org.example.application.service.AccountCommandHandler;
import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.example.domain.events.Event;
import org.example.infrastructure.EventStore;
import org.example.readModel.AccountBalanceProjection;
import org.example.readModel.BalanceProjector;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {

    }
}
