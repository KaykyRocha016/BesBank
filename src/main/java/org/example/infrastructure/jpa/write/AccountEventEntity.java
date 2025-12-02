package org.example.infrastructure.jpa.write;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "account_event")
public class AccountEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NonNull
    @Column(nullable = false)
    private UUID accountId;

    @Setter
    @NonNull
    @Column(nullable = false)
    private String type;

    @Setter
    @NonNull
    @Column(nullable = false)
    private String amount;

    @Setter
    @NonNull
    @Column(nullable = false)
    private Instant timestamp;
}
