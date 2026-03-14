package org.example.transactionriskmonitor.application.adapter.out.persistence.repository;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataRiskAssessmentJpaRepository extends JpaRepository<RiskAssessmentJpaEntity, String> {
}
