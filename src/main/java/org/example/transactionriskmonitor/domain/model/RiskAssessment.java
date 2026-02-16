package org.example.transactionriskmonitor.domain.model;

import java.util.EnumSet;

public record RiskAssessment(RiskScore riskScore, EnumSet<RiskReason> reasons) {
}
