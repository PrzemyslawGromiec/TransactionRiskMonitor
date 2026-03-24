package org.example.transactionriskmonitor.application.query.dto;

import java.time.Instant;

public record TransactionSearchCriteria(
        String transactionId,
        String accountId,
        String reason,
        Integer minRiskScore,
        Integer maxRiskScore,
        Instant occurredFrom,
        Instant occurredTo
) {
}
