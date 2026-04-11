package org.example.transactionriskmonitor.application.query.dto;

import org.example.transactionriskmonitor.domain.model.RiskReason;

import java.time.Instant;
import java.util.Set;

public record TransactionSearchResponse(
        Long id,
        String transactionId,
        String accountId,
        Integer riskScore,
        Set<RiskReason> reasons,
        Instant occurredAt
) {
}
