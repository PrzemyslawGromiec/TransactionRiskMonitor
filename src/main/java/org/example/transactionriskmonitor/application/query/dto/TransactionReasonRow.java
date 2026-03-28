package org.example.transactionriskmonitor.application.query.dto;

import org.example.transactionriskmonitor.domain.model.RiskReason;

public record TransactionReasonRow(String transactionId, RiskReason reason) {
}
