package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public final class IngestTransactionService implements IngestTransactionUseCase {
    private static final int HIGH_RISK_THRESHOLD = 80;
    private final TransactionRepositoryPort txRepo;
    private final AccountProfilePort profilePort;
    private final VelocityPort velocityPort;
    private final LocationHistoryPort locationHistoryPort;
    private final AlertPublisherPort alertPublisher;
    private final RiskScorer riskScorer;

    public IngestTransactionService(
            TransactionRepositoryPort txRepo,
            AccountProfilePort profilePort,
            VelocityPort velocityPort, LocationHistoryPort locationHistoryPort,
            AlertPublisherPort alertPublisher,
            RiskScorer riskScorer
    ) {
        this.txRepo = txRepo;
        this.profilePort = profilePort;
        this.velocityPort = velocityPort;
        this.locationHistoryPort = locationHistoryPort;
        this.alertPublisher = alertPublisher;
        this.riskScorer = riskScorer;
    }

    @Override
    public IngestResult ingest(IngestTransactionCommand cmd) {
        TransactionId txId = new TransactionId(cmd.transactionId());
        AccountId accountId = new AccountId(cmd.accountId());
        Country country = new Country(cmd.country());
        Money money = new Money(new BigDecimal(cmd.amount()), Currency.getInstance(cmd.currency()));
        Instant occurredAt = cmd.occurredAt();

        if (txRepo.exists(txId)) {
            return new IngestResult.Duplicated(txId.value());
        }

        Transaction tx = new Transaction(txId, accountId, money, country, occurredAt);
        AccountProfile profile = profilePort.load(accountId);
        LocationChange locationChange = locationHistoryPort.observe(accountId, occurredAt, country);
        VelocityStats velocity = velocityPort.observe(accountId, occurredAt, money);
        RiskAssessment assessment = riskScorer.score(tx, profile, velocity, locationChange);
        txRepo.save(tx, assessment.riskScore());

        if (assessment.riskScore().value() >= HIGH_RISK_THRESHOLD) {
            HighRiskAlert alert = new HighRiskAlert(
                    txId,
                    accountId,
                    assessment.riskScore(),
                    assessment.reasons(),
                    occurredAt
            );
            alertPublisher.publish(alert);
        }
        return new IngestResult.Accepted(txId.value(), assessment.riskScore());
    }
}
