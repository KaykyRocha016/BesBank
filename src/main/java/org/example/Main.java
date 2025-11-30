package org.example;

import org.example.application.service.AccountCommandHandler;
import org.example.domain.commands.DepositarDinheiroCommand;
import org.example.domain.commands.SacarDinheiroCommand;
import org.example.infrastructure.EventStore;
import org.example.readModel.AccountBalanceProjection;
import org.example.readModel.BalanceProjector;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA BANCÁRIO (CQRS + EVENT SOURCING zFefeu, Caio Preto e Doutor Bosta) ===");

        // 1. Configuração dos componentes principais
        UUID contaId = UUID.randomUUID();
        System.out.println("Conta criada: " + contaId);
        System.out.println("--------------------------------------------------");

        // WRITE SIDE (Event Sourcing)
        EventStore eventStore = new EventStore();
        AccountCommandHandler commandHandler = new AccountCommandHandler(eventStore);

        // READ SIDE (Projeção)
        BalanceProjector projector = new BalanceProjector();

        // Simulação do Event Broker/Listener, que pega os eventos do Event Store e joga no Projector
        // (Em um sistema real, isso seria assíncrono - Kafka/RabbitMQ)
        Runnable eventListener = () -> {
            // Em uma simulação simples, pegamos todos os eventos e projetamos
            System.out.println("\n8. Consumindo Eventos do Broker (Listener)");

            // Simulação: o Event Store envia todos os eventos para o Projector
            List<Object> allEvents = eventStore.getAllEvents();
            allEvents.forEach(projector::project);

            System.out.println("--------------------------------------------------");
        };

        // --- FLUXO DE ESCRITA (Depósito) ---
        System.out.println("\n--- 1. ENVIANDO COMANDO DE DEPÓSITO ---");
        // 1. Usuário Envia Comando
        DepositarDinheiroCommand deposito = new DepositarDinheiroCommand(contaId, new BigDecimal("100.00"));
        commandHandler.handle(deposito); // Dispara fluxo 2-7

        // --- FLUXO DE LEITURA (Projeção do Saldo) ---
        eventListener.run(); // Simula o Event Listener consumindo e projetando eventos 8-10

        // --- FLUXO DE CONSULTA (Saldo) ---
        System.out.println("\n--- 2. ENVIANDO CONSULTA DE SALDO ---");
        // 11. Usuário Consulta Saldo
        AccountBalanceProjection saldo1 = projector.getBalance(contaId); // Dispara fluxo 11-12
        System.out.println("SALDO FINAL CONSULTADO: " + saldo1.currentBalance());
        System.out.println("--------------------------------------------------");

        // --- FLUXO DE ESCRITA (Saque) ---
        System.out.println("\n--- 3. ENVIANDO COMANDO DE SAQUE ---");
        SacarDinheiroCommand saque = new SacarDinheiroCommand(contaId, new BigDecimal("30.00"));
        commandHandler.handle(saque);

        // --- FLUXO DE LEITURA (Nova Projeção do Saldo) ---
        eventListener.run();

        // --- FLUXO DE CONSULTA (Novo Saldo) ---
        System.out.println("\n--- 4. ENVIANDO NOVA CONSULTA DE SALDO ---");
        AccountBalanceProjection saldo2 = projector.getBalance(contaId);
        System.out.println("SALDO FINAL CONSULTADO: " + saldo2.currentBalance());
    }
}
