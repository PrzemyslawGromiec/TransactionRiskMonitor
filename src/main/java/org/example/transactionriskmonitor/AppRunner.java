package org.example.transactionriskmonitor;

import org.example.transactionriskmonitor.application.adapter.out.*;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.application.usecase.IngestTransactionService;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;

import java.time.Duration;
import java.util.Set;

public class AppRunner {
    public static void main(String[] args) {
        TransactionRepositoryPort txRepo = new InMemoryTransactionRepository();
        RiskAssessmentRepositoryPort assessmentRepo = new InMemoryRiskAssessmentRepository();
        MerchantHistoryPort merchantHistoryPort = new InMemoryMerchantHistoryAdapter();
        InMemoryAccountProfileAdapter profileAdapter = new InMemoryAccountProfileAdapter();

        profileAdapter.put(
                new AccountId("acc-1"),
                new AccountProfile(
                        false,
                        Set.of(new Country("IR")),
                        TrustStatus.FLAGGED
                )
        );

        VelocityPort velocityPort = new InMemoryVelocityAdapter(Duration.ofMinutes(5));
        LocationHistoryPort locationPort = new InMemoryLocationHistoryAdapter(Duration.ofMinutes(10));
        AlertPublisherPort alertPublisher = new ConsoleAlertPublisher();
        RiskPolicy policy = RiskPolicy.defaultPolicy();

        RiskScorer riskScorer = new RiskScorer(policy);

        IngestTransactionUseCase service = new IngestTransactionService(
                txRepo,
                assessmentRepo,
                profileAdapter,
                velocityPort,
                locationPort,
                merchantHistoryPort,
                alertPublisher,
                riskScorer,
                policy
        );

        var now = java.time.Instant.now();

        IngestTransactionCommand tx1 = new IngestTransactionCommand(
                "tx-1",
                "acc-1",
                "amazon",
                "9000.00",
                "GBP",
                "IR",
                now
        );

        IngestTransactionCommand tx2 = new IngestTransactionCommand(
                "tx-2",
                "acc-1",
                "amazon",
                "9000.00",
                "GBP",
                "US",
                now.plusSeconds(60)
        );

        IngestTransactionCommand tx3 = new IngestTransactionCommand(
                "tx-3",
                "acc-1",
                "amazon",
                "9000.00",
                "GBP",
                "GB",
                now.plusSeconds(120)
        );

        IngestTransactionCommand tx4 = new IngestTransactionCommand(
                "tx-1",
                "acc-1",
                "amazon",
                "9000.00",
                "GBP",
                "IR",
                now
        );

        System.out.println(service.ingest(tx1));
        System.out.println(service.ingest(tx2));
        System.out.println(service.ingest(tx3));
        System.out.println(service.ingest(tx4));
    }
}
