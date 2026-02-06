package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.out.AccountProfilePort;
import org.example.transactionriskmonitor.application.port.out.AlertPublisherPort;
import org.example.transactionriskmonitor.application.port.out.TransactionRepositoryPort;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class IngestTransactionServiceTest {

    @Test
    void shouldReturnDuplicate_whenTransactionAlreadyExists() {
        var repo = new InMemoryTransactionRepository();
        var profilePort = new StubAccountProfilePort(new AccountProfile(
                false,
                Set.of(new Country("GB")),
                TrustStatus.TRUSTED
        ));

        var alerts = new RecordingAlertPublisherReport();

        //marking transaction as already exists
        repo.markExist(new TransactionId("tx-1"));

        var service = new IngestTransactionService(repo, profilePort, alerts, new RiskScorer());

        var cmd = new IngestTransactionCommand(
                "tx-1", "acc-1", "100.00", "GBP", "GB",
                Instant.parse("2026-02-03T12:00:00Z")
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Duplicated.class, result);
        assertEquals(0, repo.savedCount(), "Should not save duplicate transaction");
        assertEquals(0, alerts.publishedCount(), "Should not publish alert for duplicate");
    }

    @Test
    void shouldPersistAndReturnAccepted_forNewTransaction() {
        var repo = new InMemoryTransactionRepository();
        var profilePort = new StubAccountProfilePort(new AccountProfile(
                false,
                Set.of(new Country("US")),
                TrustStatus.TRUSTED
        ));

        var alerts = new RecordingAlertPublisherReport();

        var service = new IngestTransactionService(repo, profilePort, alerts, new RiskScorer());

        var cmd = new IngestTransactionCommand(
                "tx-2", "acc-2", "50.00", "GBP", "GB",
                Instant.parse("2026-02-03T12:01:00Z")
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, result);
        assertEquals(1, repo.savedCount(), "Should save scored transaction");
        assertEquals(0, alerts.publishedCount(), "Low-risk tx should not publish alert");
    }

    @Test
    void shouldPublishHighRiskAlert_whenScoreIsHigh() {
        var repo = new InMemoryTransactionRepository();
        var profilePort = new StubAccountProfilePort(new AccountProfile(
                true,
                Set.of(new Country("GB")),
                TrustStatus.FLAGGED
        ));

        var alerts = new RecordingAlertPublisherReport();

        var service = new IngestTransactionService(repo, profilePort, alerts, new RiskScorer());

        var cmd = new IngestTransactionCommand(
                "tx-3", "acc-3", "9000.0", "GBP", "GB",
                Instant.parse("2026-02-03T12:02:00Z")
        );

        IngestResult result = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, result);
        assertEquals(1, repo.savedCount());
        assertEquals(1, alerts.publishedCount(), "High-risk tx should publish alert");

        HighRiskAlert alert = alerts.lastPublished();
        assertEquals("tx-3", alert.transactionId().value());
        assertEquals("acc-3", alert.accountId().value());
        assertTrue(alert.riskScore().value() >= 80);
    }

    @Test
    void shouldSaveBeforePublishingAlert_forHighRiskTransaction() {
        List<String> calls = new ArrayList<>();

        TransactionRepositoryPort repo = new TransactionRepositoryPort() {
            private final Set<TransactionId> existing = new HashSet<>();

            @Override
            public boolean exists(TransactionId id) {
                return existing.contains(id);
            }

            @Override
            public void save(Transaction tx, RiskScore score) {
                existing.add(tx.id());
                calls.add("save");
            }
        };

        AlertPublisherPort publisher = alert -> calls.add("publish");

        AccountProfilePort profiles = accountId -> new AccountProfile(
                true,
                Set.of(new Country("GB")),
                TrustStatus.FLAGGED
        );

        IngestTransactionService service = new IngestTransactionService(
                repo, profiles, publisher, new RiskScorer()
        );

        IngestTransactionCommand cmd = new IngestTransactionCommand(
                "tx-100",
                "acc-100",
                "9000.00",
                "GBP",
                "GB",
                Instant.parse("2026-02-03T12:10:00Z")
        );

        service.ingest(cmd);

        assertEquals(List.of("save", "publish"), calls);
    }

    @Test
    void shouldPublishOnlyOneAlert_whenSameHighRiskTransactionIsIngestedTwice() {
        List<HighRiskAlert> published = new ArrayList<>();
        AlertPublisherPort publisher = published::add;

        TransactionRepositoryPort repo = new TransactionRepositoryPort() {
            private final Set<TransactionId> existing = new HashSet<>();

            @Override
            public boolean exists(TransactionId id) {
                return existing.contains(id);
            }

            @Override
            public void save(Transaction tx, RiskScore score) {
                existing.add(tx.id());
            }
        };

        AccountProfilePort profiles = accountId -> new AccountProfile(
                true,
                Set.of(new Country("GB")),
                TrustStatus.FLAGGED
        );

        var service = new IngestTransactionService(repo, profiles, publisher, new RiskScorer());

        var cmd = new IngestTransactionCommand(
                "tx-777", "acc-777", "9000.00", "GBP", "GB",
                Instant.parse("2026-02-03T12:20:00Z")
        );

        var first = service.ingest(cmd);
        var second = service.ingest(cmd);

        assertInstanceOf(IngestResult.Accepted.class, first);
        assertInstanceOf(IngestResult.Duplicated.class, second);
        assertEquals(1, published.size(), "Alert must be published only once");
    }



    //In memory fake repository useful for unit test
    private static final class InMemoryTransactionRepository implements TransactionRepositoryPort {

        private final Set<TransactionId> existing = ConcurrentHashMap.newKeySet();
        private final Map<TransactionId, RiskScore> saved = new ConcurrentHashMap<>();

        @Override
        public boolean exists(TransactionId id) {
            return existing.contains(id);
        }

        @Override
        public void save(Transaction tx, RiskScore score) {
            existing.add(tx.id());
            saved.put(tx.id(), score);
        }

        void markExist(TransactionId id) {
            existing.add(id);
        }

        int savedCount() {
            return saved.size();
        }
    }

    //Profile port that returns fixed data
    private static final class StubAccountProfilePort implements AccountProfilePort {
        private final AccountProfile profile;

        private StubAccountProfilePort(AccountProfile profile) {
            this.profile = profile;
        }

        @Override
        public AccountProfile load(AccountId accountId) {
            return profile;
        }
    }

    //Alert publisher
    private static final class RecordingAlertPublisherReport implements AlertPublisherPort {
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
}