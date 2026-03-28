package org.example.transactionriskmonitor.application.adapter.out.persistence.mapper;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;

@Component
public class RiskAssessmentPersistenceMapper {

    public RiskAssessmentJpaEntity toEntity(
            String transactionId,
            RiskAssessment domain,
            Instant assessedAt
    ) {
        return new RiskAssessmentJpaEntity(
                transactionId,
                domain.riskScore().value(),
                new HashSet<>(domain.reasons()),
                assessedAt
        );
    }

    public RiskAssessment toDomain(RiskAssessmentJpaEntity entity) {
        return new RiskAssessment(
                new RiskScore(entity.getRiskScore()),
                entity.getReasons().isEmpty()
                        ? EnumSet.noneOf(RiskReason.class)
                        : EnumSet.copyOf(entity.getReasons())
        );
    }
}