package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;

import java.util.EnumSet;

public record RiskAssessment(RiskScore score, EnumSet<RiskReason> reasons) {
}
