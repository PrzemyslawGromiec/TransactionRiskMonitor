package org.example.transactionriskmonitor.application.query.dto;

public record TransactionSearchRow(
        Long id,
        String transactionId,
        String accountId,
        Integer riskScore
) {
}
