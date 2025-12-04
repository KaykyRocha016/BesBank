package org.example.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.application.service.AccountCommandHandler;
import org.example.domain.commands.DepositCommand;
import org.example.domain.commands.WithdrawCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Comandos de Conta", description = "Operações de escrita (Write Side) - CQRS")
@RestController
@RequestMapping("/api/v1/accounts")
public class WriteController {
    private final AccountCommandHandler commandHandler;

    public WriteController(AccountCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<?> deposit(@PathVariable UUID accountId, @RequestBody DepositRequest request) {
        var command = new DepositCommand(request.amount(), accountId);
        commandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable UUID accountId, @RequestBody WithdrawRequest request) {
        var command = new WithdrawCommand(request.amount(), accountId);
        commandHandler.handle(command);
        return ResponseEntity.ok().build();
    }

    record DepositRequest(BigDecimal amount) {}
    record WithdrawRequest(BigDecimal amount) {}
}

