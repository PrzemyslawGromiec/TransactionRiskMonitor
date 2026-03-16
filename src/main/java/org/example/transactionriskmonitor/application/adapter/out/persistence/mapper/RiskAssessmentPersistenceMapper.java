package org.example.transactionriskmonitor.application.adapter.out.persistence.mapper;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.RiskAssessmentJpaEntity;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

@Component
public class RiskAssessmentPersistenceMapper {

    public RiskAssessmentJpaEntity toEntity(String transactionId, RiskAssessment assessment) {
        return new RiskAssessmentJpaEntity(
                transactionId,
                assessment.riskScore().value(),
                serializeReasons(assessment.reasons()),
                Instant.now()
        );
    }

    public RiskAssessment toDomain(RiskAssessmentJpaEntity entity) {
        return new RiskAssessment(
                new RiskScore(entity.getRiskScore()),
                deserializeReasons(entity.getReasons())
        );
    }

    private String serializeReasons(EnumSet<RiskReason> reasons) {
        return reasons.stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(","));
    }

    private EnumSet<RiskReason> deserializeReasons(String reasons) {
        if (reasons == null || reasons.isBlank()) {
            return EnumSet.noneOf(RiskReason.class);
        }

        // NOTE FOR MYSELF: take each String reason and convert it into corresponding RiskReson enum using valueOf()
        return Arrays.stream(reasons.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(RiskReason::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RiskReason.class)));
    }

}