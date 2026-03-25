package org.example.transactionriskmonitor.application.query.dto;

import org.example.transactionriskmonitor.domain.model.RiskReason;

import java.time.Instant;
import java.util.List;

public record TransactionSearchCriteria(
        String transactionId,
        String accountId,
        List<RiskReason> reasons,
        Integer minRiskScore,
        Integer maxRiskScore,
        Instant occurredFrom,
        Instant occurredTo
) {
}
