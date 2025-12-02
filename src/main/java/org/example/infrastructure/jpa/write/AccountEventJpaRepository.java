package org.example.infrastructure.jpa.write;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountEventJpaRepository extends JpaRepository<@NonNull AccountEventEntity, @NonNull Long> {
    List<AccountEventEntity> findByAccountIdOrderByIdAsc(UUID accountId);
}
