package org.example.presentation;

import lombok.NonNull;
import org.example.application.service.AccountQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class ReadController {

    private final AccountQueryService queryService;

    public ReadController(AccountQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<@NonNull BalanceResponse> getBalance(@PathVariable UUID accountId) {
        BigDecimal balance = queryService.getBalance(accountId);
        return ResponseEntity.ok(new BalanceResponse(accountId, balance));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<@NonNull AccountInfoResponse> getAccountInfo(@PathVariable UUID accountId) {
        var account = queryService.getAccountInfo(accountId);
        return ResponseEntity.ok(new AccountInfoResponse(
                account.getAccountId(),
                account.getBalance(),
                account.getVersion()
        ));
    }

    public record BalanceResponse(UUID accountId, BigDecimal balance) {}
    public record AccountInfoResponse(UUID accountId, BigDecimal balance, Long version) {}
}
