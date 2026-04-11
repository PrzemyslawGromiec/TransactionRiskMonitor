package org.example.transactionriskmonitor.application.adapter.out.persistence.seeder;

import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataTransactionRepository;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.config.SeedAccounts;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@Profile("postgres-demo")
@Order(2)
public class TransactionScenarioSeeder implements CommandLineRunner {

    private final IngestTransactionUseCase ingestTransactionUseCase;
    private final SpringDataTransactionRepository transactionRepository;

    public TransactionScenarioSeeder(IngestTransactionUseCase ingestTransactionUseCase, SpringDataTransactionRepository repository) {
        this.ingestTransactionUseCase = ingestTransactionUseCase;
        this.transactionRepository = repository;
    }

    @Override
    public void run(String... args) {
        if (transactionRepository.count() > 0) {
            return;
        }

        Instant baseTime = Instant.parse("2026-04-11T00:00:00Z");
        seedNormalTransactions(baseTime);
        seedHighAmountTransactions(baseTime.plusSeconds(3600));
        seedHighVelocityTransactions(baseTime.plusSeconds(7200));
        seedImpossibleTravelTransactions(baseTime.plusSeconds(10800));
        seedNewAccountTransactions(baseTime.plusSeconds(14400));
        seedDuplicateTransactions(baseTime.plusSeconds(18000));
        seedConflictingSignals(baseTime.plusSeconds(21600));
        seedFlaggedHighRiskScenario(baseTime.plusSeconds(24000));
    }

    // normal transaction
    private void seedNormalTransactions(Instant baseTime) {
        for (int i = 0; i < 20; i++) {
            ingest(
                    "tx-normal-" + i,
                    SeedAccounts.NORMAL_SAFE_1,
                    50 + i,
                    "merchant-1",
                    "GB",
                    baseTime.plusSeconds(i * 60)
            );
        }
    }

    // high amount
    private void seedHighAmountTransactions(Instant baseTime) {
        for (int i = 0; i < 20; i++) {
            ingest(
                    "tx-high-" + i,
                    SeedAccounts.NORMAL_SAFE_2,
                    10000,
                    "merchant-2",
                    "GB",
                    baseTime.plusSeconds(i * 900)
            );
        }
    }

    // high velocity - same amount, short interval
    private void seedHighVelocityTransactions(Instant baseTime) {
        for (int i = 0; i < 10; i++) {
            ingest(
                    "tx-velocity-" + i,
                    SeedAccounts.NORMAL_SAFE_2,
                    100,
                    "merchant-3",
                    "GB",
                    baseTime.plusSeconds(i * 5)
            );
        }
    }

    // impossible travel
    private void seedImpossibleTravelTransactions(Instant baseTime) {
        ingest(
                "tx-travel-1",
                SeedAccounts.FLAGGED_SAFE_1,
                200,
                "merchant-4",
                "GB",
                baseTime
        );

        ingest(
                "tx-travel-2",
                SeedAccounts.FLAGGED_SAFE_1,
                200,
                "merchant-4",
                "NG",
                baseTime.plusSeconds(120)
        );
    }

    // new account, first time merchant
    private void seedNewAccountTransactions(Instant baseTime) {
        for (int i = 0; i < 6; i++) {
            ingest(
                    "tx-new-" + i,
                    SeedAccounts.NEW_NORMAL_RISKY_2,
                    300,
                    "merchant-new-" + i,
                    "NG",
                    baseTime.plusSeconds(i * 60)
            );
        }
    }

    private void seedDuplicateTransactions(Instant baseTime) {
        String txId = "tx-dup-1";

        ingest(
                txId,
                SeedAccounts.TRUSTED_SAFE_1,
                100,
                "merchant-5",
                "GB",
                baseTime
        );

        // duplicate
        ingest(
                txId,
                SeedAccounts.TRUSTED_SAFE_1,
                100,
                "merchant-5",
                "GB",
                baseTime.plusSeconds(60)
        );
    }

    // conflicting signals (trusted vs risky behaviour)
    private void seedConflictingSignals(Instant base) {
        for (int i = 0; i < 6; i++) {
            ingest(
                    "tx-conflict-" + i,
                    SeedAccounts.TRUSTED_RISKY_1,
                    200,
                    "merchant-6",
                    "RU",
                    base.plusSeconds(i * 10)
            );
        }
    }

    // combination of factors
    private void seedFlaggedHighRiskScenario(Instant baseTime) {
        for (int i = 0; i < 5; i++) {
            ingest(
                    "tx-flagged-" + i,
                    SeedAccounts.FLAGGED_RISKY_1,
                    150,
                    "merchant-unique-" + i,
                    "RU",
                    baseTime.plusSeconds(i * 1800)
            );
        }
    }

    private void ingest(String transactionId, String accountId, double amount, String merchantId,
                        String country, Instant occurredAt) {
        ingestTransactionUseCase.ingest(
                new IngestTransactionCommand(
                        transactionId,
                        accountId,
                        merchantId,
                        BigDecimal.valueOf(amount),
                        "GBP",
                        country,
                        occurredAt
                )
        );
    }
}
