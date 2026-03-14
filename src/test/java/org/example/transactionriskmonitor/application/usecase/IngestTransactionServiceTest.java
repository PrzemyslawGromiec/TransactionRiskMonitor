package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.adapter.out.memory.InMemoryRiskAssessmentRepository;
import org.example.transactionriskmonitor.application.adapter.out.memory.InMemoryTransactionRepository;
import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IngestTransactionServiceTest {
    private final RiskPolicy policy = RiskPolicy.defaultPolicy();

    @Test
    void shouldReturnDuplicate_whenTransactionAlreadyExists() {
        var txRepo = new InMemoryTransactionRepository();
        var riskAssessmentRepo = new InMemoryRiskAssessmentRepository();
        var alerts = new RecordingAlertPublisher();

        txRepo.save(transaction(
                "tx-1",
                "acc-1",
                "amazon",
                "100.00",
                "GBP",
                "GB",
                "2026-02-03T12:00:00Z"
        ));

        var service = createService(
                txRepo,
                riskAssessmentRepo,
                trustedProfile(),
                lowVelocity(),
                normalLocation(),
                notFirstTimeMerchant(),
                alerts
        );

        var cmd = command(
                "tx-1",
                "acc-1",
                "amazon",
                "100.00",
                "GBP",
                "GB",
                "2026-02-03T12:00:00Z"
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Duplicated.class, result);
        assertEquals(1, txRepo.store().size(), "Should not save duplicate transaction");
        assertEquals(0, riskAssessmentRepo.store().size(), "Should not save assessment for duplicate");
        assertEquals(0, alerts.publishedCount(), "Should not publish alert for duplicate");
    }

    @Test
    void shouldPersistAndReturnAccepted_forNewTransaction() {
        var txRepo = new InMemoryTransactionRepository();
        var riskAssessmentRepo = new InMemoryRiskAssessmentRepository();
        var alerts = new RecordingAlertPublisher();

        var service = createService(
                txRepo,
                riskAssessmentRepo,
                trustedProfile(),
                lowVelocity(),
                normalLocation(),
                notFirstTimeMerchant(),
                alerts
        );

        var cmd = command(
                "tx-2",
                "acc-2",
                "amazon",
                "50.00",
                "GBP",
                "GB",
                "2026-02-03T12:01:00Z"
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, result);
        assertEquals(1, txRepo.store().size(), "Should save transaction");
        assertEquals(1, riskAssessmentRepo.store().size(), "Should save risk assessment");
        assertEquals(0, alerts.publishedCount(), "Low-risk tx should not publish alert");
    }

    @Test
    void shouldPublishHighRiskAlert_whenScoreIsHigh() {
        var txRepo = new InMemoryTransactionRepository();
        var riskAssessmentRepo = new InMemoryRiskAssessmentRepository();
        var alerts = new RecordingAlertPublisher();

        var service = createService(
                txRepo,
                riskAssessmentRepo,
                flaggedHighRiskProfile(),
                lowVelocity(),
                normalLocation(),
                notFirstTimeMerchant(),
                alerts
        );

        var cmd = command(
                "tx-3",
                "acc-3",
                "amazon",
                "9000.00",
                "GBP",
                "GB",
                "2026-02-03T12:02:00Z"
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, result);
        assertEquals(1, txRepo.store().size(), "Should save transaction");
        assertEquals(1, riskAssessmentRepo.store().size(), "Should save assessment");
        assertEquals(1, alerts.publishedCount(), "High-risk tx should publish alert");

        HighRiskAlert alert = alerts.lastPublished();
        assertEquals("tx-3", alert.transactionId().value());
        assertEquals("acc-3", alert.accountId().value());
        assertTrue(alert.riskScore().value() >= policy.highRiskThreshold());
    }

    @Test
    void shouldSaveBeforePublishingAlert_forHighRiskTransaction() {
        List<String> calls = new ArrayList<>();

        TransactionRepositoryPort txRepo = new TransactionRepositoryPort() {
            @Override
            public boolean exists(TransactionId id) {
                return false;
            }

            @Override
            public Transaction save(Transaction tx) {
                calls.add("save-transaction");
                return tx;
            }

            @Override
            public Optional<Transaction> findById(TransactionId id) {
                return Optional.empty();
            }
        };

        RiskAssessmentRepositoryPort riskAssessmentRepo = new RiskAssessmentRepositoryPort() {
            @Override
            public void save(TransactionId transactionId, RiskAssessment assessment) {
                calls.add("save-assessment");
            }

            @Override
            public Optional<RiskAssessment> findByTransactionId(TransactionId transactionId) {
                return Optional.empty();
            }
        };

        AlertPublisherPort publisher = alert -> calls.add("publish");

        var service = createService(
                txRepo,
                riskAssessmentRepo,
                flaggedHighRiskProfile(),
                lowVelocity(),
                normalLocation(),
                notFirstTimeMerchant(),
                publisher
        );

        service.ingest(command(
                "tx-100",
                "acc-100",
                "amazon",
                "9000.00",
                "GBP",
                "GB",
                "2026-02-03T12:10:00Z"
        ));

        assertEquals(List.of("save-transaction", "save-assessment", "publish"), calls);
    }

    @Test
    void shouldPublishOnlyOneAlert_whenSameHighRiskTransactionIsIngestedTwice() {
        var txRepo = new InMemoryTransactionRepository();
        var riskAssessmentRepo = new InMemoryRiskAssessmentRepository();
        var alerts = new RecordingAlertPublisher();

        var service = createService(
                txRepo,
                riskAssessmentRepo,
                flaggedHighRiskProfile(),
                lowVelocity(),
                normalLocation(),
                notFirstTimeMerchant(),
                alerts
        );

        var cmd = command(
                "tx-777",
                "acc-777",
                "amazon",
                "9000.00",
                "GBP",
                "GB",
                "2026-02-03T12:20:00Z"
        );

        IngestResult first = service.ingest(cmd);
        IngestResult second = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, first);
        assertInstanceOf(IngestResult.Duplicated.class, second);
        assertEquals(1, alerts.publishedCount(), "Alert must be published only once");
        assertEquals(1, txRepo.store().size(), "Transaction must be stored only once");
        assertEquals(1, riskAssessmentRepo.store().size(), "Assessment must be stored only once");
    }

    // ---------- helpers ----------

    private IngestTransactionService createService(
            TransactionRepositoryPort txRepo,
            RiskAssessmentRepositoryPort riskAssessmentRepo,
            AccountProfilePort profilePort,
            VelocityPort velocityPort,
            LocationHistoryPort locationHistoryPort,
            MerchantHistoryPort merchantHistoryPort,
            AlertPublisherPort alertPublisher
    ) {
        return new IngestTransactionService(
                txRepo,
                riskAssessmentRepo,
                profilePort,
                velocityPort,
                locationHistoryPort,
                merchantHistoryPort,
                alertPublisher,
                new RiskScorer(policy),
                policy
        );
    }

    private static IngestTransactionCommand command(
            String txId,
            String accId,
            String merchantId,
            String amount,
            String currency,
            String country,
            String occurredAt
    ) {
        return new IngestTransactionCommand(
                txId,
                accId,
                merchantId,
                amount,
                currency,
                country,
                Instant.parse(occurredAt)
        );
    }

    private static Transaction transaction(
            String txId,
            String accId,
            String merchantId,
            String amount,
            String currency,
            String country,
            String occurredAt
    ) {
        return new Transaction(
                new TransactionId(txId),
                new AccountId(accId),
                new MerchantId(merchantId),
                new Money(new BigDecimal(amount), Currency.getInstance(currency)),
                new Country(country),
                Instant.parse(occurredAt)
        );
    }

    private static AccountProfilePort trustedProfile() {
        return new StubAccountProfilePort(new AccountProfile(
                false,
                Set.of(new Country("GB")),
                TrustStatus.TRUSTED
        ));
    }

    private static AccountProfilePort flaggedHighRiskProfile() {
        return new StubAccountProfilePort(new AccountProfile(
                true,
                Set.of(new Country("GB")),
                TrustStatus.FLAGGED
        ));
    }

    private record StubAccountProfilePort(AccountProfile profile) implements AccountProfilePort {

        @Override
            public AccountProfile load(AccountId accountId) {
                return profile;
            }
        }

    private static final class RecordingAlertPublisher implements AlertPublisherPort {
        private final List<HighRiskAlert> publishedAlerts = new ArrayList<>();

        @Override
        public void publish(HighRiskAlert alert) {
            publishedAlerts.add(alert);
        }

        int publishedCount() {
            return publishedAlerts.size();
        }

        HighRiskAlert lastPublished() {
            if (publishedAlerts.isEmpty()) {
                throw new AssertionError("No alerts published");
            }
            return publishedAlerts.get(publishedAlerts.size() - 1);
        }
    }

    private record StubVelocityPort(VelocityStats stats) implements VelocityPort {

        @Override
            public VelocityStats observe(AccountId accountId, Instant occurredAt, Money amount, MerchantId merchantId) {
                return stats;
            }
        }

    private static VelocityPort lowVelocity() {
        return new StubVelocityPort(new VelocityStats(
                1,
                new Money(BigDecimal.ZERO, Currency.getInstance("GBP")),
                1
        ));
    }

    private record StubLocationHistoryPort(LocationChange change) implements LocationHistoryPort {

        @Override
            public LocationChange observe(AccountId accountId, Instant occurredAt, Country currentCountry) {
                return change;
            }
        }

    private static LocationHistoryPort normalLocation() {
        return new StubLocationHistoryPort(new LocationChange(false, null, Duration.ZERO));
    }

    private record StubMerchantHistoryPort(boolean firstTimeMerchant) implements MerchantHistoryPort {

        @Override
            public boolean isFirstTimeMerchant(AccountId accountId, MerchantId merchantId) {
                return firstTimeMerchant;
            }
        }

    private static MerchantHistoryPort notFirstTimeMerchant() {
        return new StubMerchantHistoryPort(false);
    }

}