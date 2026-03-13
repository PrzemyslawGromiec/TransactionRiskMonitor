package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.application.port.out.LocationChange;
import org.example.transactionriskmonitor.application.port.out.VelocityStats;
import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;

import java.math.BigDecimal;
import java.util.EnumSet;

public final class RiskScorer {
    private static final BigDecimal HIGH_AMOUNT = new BigDecimal("5000");
    private static final BigDecimal VELOCITY_AMOUNT_THRESHOLD = new BigDecimal("3000");
    private static final int HIGH_RISK_THRESHOLD = 80;

    /*
     * A transaction is evaluated within the context of an AccountProfile.
     * The RiskScore is derived by combining transaction signals
     * with account characteristics.
     * Transaction → what happened
     * AccountProfile → who the account is
     */

    /*
    * When ingesting transaction, if the same account has >= N transactions within T minutes
    * add reason HIGH_VELOCITY and increase score
    */

    public RiskAssessment score(Transaction tx, AccountProfile profile, VelocityStats velocity, LocationChange locationChange) {
        if (tx == null || profile == null) {
            throw new IllegalArgumentException("Transaction or account profile not exist");
        }

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

        if (isHighVelocity(velocity)) {
            score += 25;
            reasons.add(RiskReason.HIGH_VELOCITY);
        }

        if (locationChange != null && locationChange.suspicious()) {
            score += 25;
            reasons.add(RiskReason.IMPOSSIBLE_TRAVEL);
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
        return new RiskAssessment(new RiskScore(score), reasons);
    }

    public boolean isHighRisk(RiskScore score) {
        return score.value() >= HIGH_RISK_THRESHOLD;
    }

    private boolean isHighVelocity(VelocityStats velocity) {
        if (velocity == null) {
            return false;
        }

        boolean manyTransactions = velocity.countInWindow() >= 5;
        boolean largeVelocityAmount = velocity.sumInWindow().amount().compareTo(VELOCITY_AMOUNT_THRESHOLD) >= 0;
        return manyTransactions || largeVelocityAmount;
    }
}
