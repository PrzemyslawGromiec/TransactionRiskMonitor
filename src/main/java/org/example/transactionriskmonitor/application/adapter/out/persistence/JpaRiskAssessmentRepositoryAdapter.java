package org.example.transactionriskmonitor.application.adapter.out.persistence;

import org.example.transactionriskmonitor.application.adapter.out.persistence.mapper.RiskAssessmentPersistenceMapper;
import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataRiskAssessmentJpaRepository;
import org.example.transactionriskmonitor.application.port.out.RiskAssessmentRepositoryPort;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.TransactionId;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("postgres")
public class JpaRiskAssessmentRepositoryAdapter implements RiskAssessmentRepositoryPort{

    private final SpringDataRiskAssessmentJpaRepository repository;
    private final RiskAssessmentPersistenceMapper mapper;

    public JpaRiskAssessmentRepositoryAdapter(
            SpringDataRiskAssessmentJpaRepository repository,
            RiskAssessmentPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(TransactionId transactionId, RiskAssessment assessment) {
        repository.save(mapper.toEntity(transactionId.value(), assessment));
    }

    @Override
    public Optional<RiskAssessment> findByTransactionId(TransactionId transactionId) {
        return repository.findById(transactionId.value())
                .map(mapper::toDomain);
    }
}
