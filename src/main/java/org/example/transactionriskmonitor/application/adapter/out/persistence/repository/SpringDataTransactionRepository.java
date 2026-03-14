package org.example.transactionriskmonitor.application.adapter.out.persistence.repository;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, String> {
}
