package org.example.transactionriskmonitor.domain.event;

import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.TransactionId;

import java.time.Instant;
import java.util.EnumSet;

public record HighRiskAlert(
        TransactionId transactionId,
        AccountId accountId,
        RiskScore riskScore,
        EnumSet<RiskReason> reasons,
        Instant occurredAt
) {
    public HighRiskAlert {
        if (riskScore.value() < 80) {
            throw new IllegalArgumentException("HighRiskAlert requires high risk score");
        }
    }
}
