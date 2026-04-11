package org.example.transactionriskmonitor.application.query.dto;

import java.time.Instant;

public record TransactionSearchRow(
        Long id,
        String transactionId,
        String accountId,
        Integer riskScore,
        Instant occurredAt
) {
}
