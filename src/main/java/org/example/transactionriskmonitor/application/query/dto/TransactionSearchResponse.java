package org.example.transactionriskmonitor.application.query.dto;

public record TransactionSearchResponse(
        Long id,
        String transactionId,
        String accountId,
        Integer riskScore
) {
}
