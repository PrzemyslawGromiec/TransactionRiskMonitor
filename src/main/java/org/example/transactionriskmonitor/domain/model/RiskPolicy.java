package org.example.transactionriskmonitor.domain.model;

import java.math.BigDecimal;

public record RiskPolicy(
        BigDecimal highAmountThreshold,
        BigDecimal velocityAmountThreshold,
        int highRiskThreshold,
        int velocityCountThreshold,
        int distinctMerchantThreshold,
        int highAmountWeight,
        int newAccountWeight,
        int highRiskCountryWeight,
        int highVelocityWeight,
        int impossibleTravelWeight,
        int flaggedAccountWeight,
        int trustedAccountPenalty,
        int firstTimeMerchantWeight
) {
    public static RiskPolicy defaultPolicy() {
        return new RiskPolicy(
                new BigDecimal("8000"),
                new BigDecimal("3000"),
                80,
                5,
                3,
                35,
                15,
                20,
                25,
                25,
                30,
                20,
                15
        );
    }
}
