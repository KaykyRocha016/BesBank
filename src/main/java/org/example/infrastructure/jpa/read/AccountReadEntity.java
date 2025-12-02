package org.example.infrastructure.jpa.read;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "account_read")
public class AccountReadEntity {

    @Id
    private UUID accountId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Long version = 0L;

    public AccountReadEntity() {}

    public AccountReadEntity(UUID accountId) {
        this.accountId = accountId;
    }
}