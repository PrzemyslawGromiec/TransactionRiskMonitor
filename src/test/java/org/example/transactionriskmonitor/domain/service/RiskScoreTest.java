package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.application.port.out.LocationChange;
import org.example.transactionriskmonitor.application.port.out.VelocityStats;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.model.RiskAssessment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreTest {
    private final RiskScorer scorer = new RiskScorer(RiskPolicy.defaultPolicy());

    @Test
    void flaggedAccountHighAmountHighRiskCountry_shouldProduceHighScore() {
        AccountProfile profile = new AccountProfile(
                true,
                Set.of(new Country("GB")),
                TrustStatus.FLAGGED
        );

        Transaction tx = new Transaction(
                new TransactionId("tx-1"),
                new AccountId("acc-1"),
                new MerchantId("amazon"),
                new Money(new BigDecimal("9000"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.parse("2026-02-03T12:00:00Z")
        );

        RiskAssessment assessment = scorer.score(
                tx,
                profile,
                normalVelocity(),
                usualLocation(),
                false
        );
        RiskScore score = assessment.riskScore();

        assertTrue(score.value() >= 80, "Expected high risk score");
        assertTrue(scorer.isHighRisk(score));
    }

    @Test
    void trustedLowAmountLowRiskCountry_shouldProduceLowScore() {
        AccountProfile profile = new AccountProfile(
                false,
                Set.of(new Country("IR")),
                TrustStatus.TRUSTED
        );

        Transaction tx = new Transaction(
                new TransactionId("tx-2"),
                new AccountId("acc-2"),
                new MerchantId("amazon"),
                new Money(new BigDecimal("50"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.now()
        );

        RiskAssessment assessment = scorer.score(
                tx,
                profile,
                normalVelocity(),
                usualLocation(),
                false
        );

        RiskScore score = assessment.riskScore();

        assertTrue(score.value() < 80);
        assertFalse(scorer.isHighRisk(score));
    }

    @Test
    void highVelocity_shouldIncreaseRisk() {
        AccountProfile profile = new AccountProfile(
                false,
                Set.of(new Country("IR")),
                TrustStatus.TRUSTED
        );

        Transaction tx = new Transaction(
                new TransactionId("tx-3"),
                new AccountId("acc-3"),
                new MerchantId("amazon"),
                new Money(new BigDecimal("100"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.now()
        );

        RiskAssessment assessment = scorer.score(
                tx,
                profile,
                highVelocity(),
                usualLocation(),
                false
        );

        assertTrue(assessment.riskScore().value() > 0);
        assertTrue(assessment.reasons().contains(RiskReason.HIGH_VELOCITY));
    }

    @Test
    void firstTimeMerchant_shouldIncreaseRiskAndAddReason() {
        AccountProfile profile = new AccountProfile(
                false,
                Set.of(new Country("IR")),
                TrustStatus.TRUSTED
        );

        Transaction tx = new Transaction(
                new TransactionId("tx-10"),
                new AccountId("acc-10"),
                new MerchantId("amazon"),
                new Money(new BigDecimal("100"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.now()
        );

        RiskAssessment assessment = scorer.score(
                tx,
                profile,
                normalVelocity(),
                usualLocation(),
                true
        );

        assertTrue(assessment.reasons().contains(RiskReason.FIRST_TIME_MERCHANT));
    }

    @Test
    void returningMerchant_shouldNotAddFirstTimeMerchantReason() {
        AccountProfile profile = new AccountProfile(
                false,
                Set.of(new Country("IR")),
                TrustStatus.TRUSTED
        );

        Transaction tx = new Transaction(
                new TransactionId("tx-11"),
                new AccountId("acc-11"),
                new MerchantId("amazon"),
                new Money(new BigDecimal("100"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.now()
        );

        RiskAssessment assessment = scorer.score(
                tx,
                profile,
                normalVelocity(),
                usualLocation(),
                false
        );

        assertFalse(assessment.reasons().contains(RiskReason.FIRST_TIME_MERCHANT));
    }

    private VelocityStats normalVelocity() {
        return new VelocityStats(
                1,
                new Money(BigDecimal.ZERO, Currency.getInstance("GBP")),
                1
        );
    }

    private VelocityStats highVelocity() {
        return new VelocityStats(
                10,
                new Money(new BigDecimal("10000"), Currency.getInstance("GBP")),
                1
        );
    }

    private LocationChange usualLocation() {
        return new LocationChange(false, new Country("GB"), Duration.ofMinutes(1));
    }

}
