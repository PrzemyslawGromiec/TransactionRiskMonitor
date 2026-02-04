package org.example.transactionriskmonitor.domain.service;

import org.example.transactionriskmonitor.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoreTest {
    private final RiskScorer scorer = new RiskScorer();

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
                new Money(new BigDecimal("9000"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.parse("2026-02-03T12:00:00Z")
        );

        RiskScore score = scorer.score(tx, profile);

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
                new Money(new BigDecimal("50"), Currency.getInstance("GBP")),
                new Country("GB"),
                Instant.now()
        );

        RiskScore score = scorer.score(tx, profile);

        assertTrue(score.value() < 80);
        assertFalse(scorer.isHighRisk(score));
    }
}
