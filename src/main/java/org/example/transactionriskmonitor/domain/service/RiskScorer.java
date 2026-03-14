package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.application.port.out.LocationChange;
import org.example.transactionriskmonitor.application.port.out.VelocityStats;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;

import java.util.EnumSet;

public final class RiskScorer {
    private final RiskPolicy policy;

    public RiskScorer(RiskPolicy policy) {
        this.policy = policy;
    }

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

    public RiskAssessment score(
            Transaction tx,
            AccountProfile profile,
            VelocityStats velocity,
            LocationChange locationChange,
            boolean firstTimeMerchant
    ) {
        if (tx == null || profile == null) {
            throw new IllegalArgumentException("Transaction or account profile not exist");
        }

        EnumSet<RiskReason> reasons = EnumSet.noneOf(RiskReason.class);
        int score = 0;

        if (isHighAmount(tx)) {
            score += policy.highAmountWeight();
            reasons.add(RiskReason.HIGH_AMOUNT);
        }

        if (profile.isNewAccount()) {
            score += policy.newAccountWeight();
            reasons.add(RiskReason.NEW_ACCOUNT);
        }

        if (profile.isCountryHighRisk(tx.country())) {
            score += policy.highRiskCountryWeight();
            reasons.add(RiskReason.HIGH_RISK_COUNTRY);
        }

        if (isHighVelocity(velocity)) {
            score += policy.highVelocityWeight();
            reasons.add(RiskReason.HIGH_VELOCITY);
        }

        if (firstTimeMerchant) {
            score += policy.firstTimeMerchantWeight();
            reasons.add(RiskReason.FIRST_TIME_MERCHANT);
        }

        if (isImpossibleTravel(locationChange)) {
            score += policy.impossibleTravelWeight();
            reasons.add(RiskReason.IMPOSSIBLE_TRAVEL);
        }

        switch (profile.trustStatus()) {
            case FLAGGED -> {
                score += policy.flaggedAccountWeight();
                reasons.add(RiskReason.FLAGGED_ACCOUNT);
            }
            case TRUSTED -> {
                score -= policy.trustedAccountPenalty();
                reasons.add(RiskReason.TRUSTED_ACCOUNT);
            }
        }

        score = Math.max(0, Math.min(score, 100));
        return new RiskAssessment(new RiskScore(score), reasons);
    }

    private static boolean isImpossibleTravel(LocationChange locationChange) {
        return locationChange != null && locationChange.suspicious();
    }

    private boolean isHighAmount(Transaction tx) {
        return tx.money().amount().compareTo(policy.highAmountThreshold()) > 0;
    }

    public boolean isHighRisk(RiskScore score) {
        return score.value() >= policy.highRiskThreshold();
    }

    private boolean isHighVelocity(VelocityStats velocity) {
        if (velocity == null) {
            return false;
        }

        boolean manyTransactions =
                velocity.countInWindow() >= policy.velocityCountThreshold();

        boolean largeVelocityAmount =
                velocity.sumInWindow() != null
                        && velocity.sumInWindow().amount().compareTo(policy.velocityAmountThreshold()) >= 0;

        boolean manyDistinctMerchants =
                velocity.distinctMerchantsInWindow() >= policy.distinctMerchantThreshold();

        return manyTransactions || largeVelocityAmount || manyDistinctMerchants;
    }
}
