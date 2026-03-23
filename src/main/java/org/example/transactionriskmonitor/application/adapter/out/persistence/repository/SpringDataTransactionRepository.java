package org.example.transactionriskmonitor.application.adapter.out.persistence.repository;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, Long> {
    boolean existsByTransactionId(String transactionId);
    Optional<TransactionJpaEntity> findByTransactionId(String transactionId);
}
