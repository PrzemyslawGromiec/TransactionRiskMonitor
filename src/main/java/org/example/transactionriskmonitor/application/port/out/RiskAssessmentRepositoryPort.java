package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.TransactionId;

import java.util.Optional;

public interface RiskAssessmentRepositoryPort {
    void save(TransactionId transactionId, RiskAssessment assessment);
    Optional<RiskAssessment> findByTransactionId(TransactionId transactionId);
}
