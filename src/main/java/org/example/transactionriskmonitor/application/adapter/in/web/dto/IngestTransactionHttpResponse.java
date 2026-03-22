package org.example.transactionriskmonitor.application.adapter.in.web.dto;

public record IngestTransactionHttpResponse(
        String transactionId,
        String status,
        Integer riskScore,
        String message
) {
}
