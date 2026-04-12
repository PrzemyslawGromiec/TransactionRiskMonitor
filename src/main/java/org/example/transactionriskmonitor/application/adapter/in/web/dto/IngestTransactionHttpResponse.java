package org.example.transactionriskmonitor.application.adapter.in.web.dto;

import org.example.transactionriskmonitor.domain.model.RiskReason;

import java.time.Instant;
import java.util.Set;

public record IngestTransactionHttpResponse(
        String transactionId,
        String status,
        Integer riskScore,
        Set<RiskReason> reasons,
        Instant occurredAt,
        Instant processedAt,
        String message
) {
}
