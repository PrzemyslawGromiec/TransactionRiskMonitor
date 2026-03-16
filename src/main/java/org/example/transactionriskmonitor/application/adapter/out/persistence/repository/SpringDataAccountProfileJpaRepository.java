package org.example.transactionriskmonitor.application.adapter.out.persistence.repository;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.AccountProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAccountProfileJpaRepository extends JpaRepository<AccountProfileJpaEntity, String> {
}
