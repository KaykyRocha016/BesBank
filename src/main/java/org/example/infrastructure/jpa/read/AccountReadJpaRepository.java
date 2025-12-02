package org.example.infrastructure.jpa.read;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountReadJpaRepository extends JpaRepository<@NonNull AccountReadEntity, @NonNull UUID> {
    Optional<AccountReadEntity> findByAccountId(UUID accountId);
}
