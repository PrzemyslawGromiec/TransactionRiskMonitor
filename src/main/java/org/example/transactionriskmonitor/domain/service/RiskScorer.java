package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.EnumSet;

public final class RiskScorer {
    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("5000");
    private static final int HIGH_RISK_THRESHOLD = 85;

    public RiskScore score(Transaction tx, AccountProfile profile) {
        EnumSet<RiskReason> reasons = EnumSet.noneOf(RiskReason.class);
        int score = 0;

        if (tx.money().amount().compareTo(HIGH_AMOUNT) > 0) {
            score += 35;
            reasons.add(RiskReason.HIGH_AMOUNT);
        }

        if (profile.isNewAccount()) {
            score += 15;
            reasons.add(RiskReason.NEW_ACCOUNT);
        }

        if (profile.isCountryHighRisk(tx.country())) {
            score += 20;
            reasons.add(RiskReason.HIGH_RISK_COUNTRY);
        }

        switch (profile.trustStatus()) {
            case FLAGGED -> {
                score += 30;
                reasons.add(RiskReason.FLAGGED_ACCOUNT);
            }
            case TRUSTED -> {
                score -=20;
                reasons.add(RiskReason.TRUSTED_ACCOUNT);
            }
        }

        score = Math.max(0, Math.min(score, 100));
        return new RiskScore(score);
    }

    public boolean isHighRisk(RiskScore score) {
        return score.value() >= HIGH_RISK_THRESHOLD;
    }
}
