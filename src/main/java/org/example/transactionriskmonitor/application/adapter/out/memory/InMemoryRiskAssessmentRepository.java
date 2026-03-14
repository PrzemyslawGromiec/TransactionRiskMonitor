package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.RiskAssessmentRepositoryPort;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.TransactionId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class InMemoryRiskAssessmentRepository implements RiskAssessmentRepositoryPort {
    private final Map<TransactionId, RiskAssessment> store = new ConcurrentHashMap<>();

    @Override
    public void save(TransactionId transactionId, RiskAssessment assessment) {
        store.put(transactionId, assessment);
    }

    @Override
    public Optional<RiskAssessment> findByTransactionId(TransactionId transactionId) {
        return Optional.ofNullable(store.get(transactionId));
    }

    public Map<TransactionId, RiskAssessment> store() {
        return Map.copyOf(store);
    }
}
