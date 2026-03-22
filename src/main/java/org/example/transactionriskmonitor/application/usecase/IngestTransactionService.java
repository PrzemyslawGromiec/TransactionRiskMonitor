package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Currency;

@Service
public class IngestTransactionService implements IngestTransactionUseCase {
    private final TransactionRepositoryPort txRepo;
    private final RiskAssessmentRepositoryPort riskAssessmentRepo;
    private final AccountProfilePort profilePort;
    private final VelocityPort velocityPort;
    private final LocationHistoryPort locationHistoryPort;
    private final MerchantHistoryPort merchantHistoryPort;
    private final AlertPublisherPort alertPublisher;
    private final RiskScorer riskScorer;
    private final RiskPolicy riskPolicy;

    public IngestTransactionService(
            TransactionRepositoryPort txRepo,
            RiskAssessmentRepositoryPort riskAssessmentRepo,
            AccountProfilePort profilePort,
            VelocityPort velocityPort,
            LocationHistoryPort locationHistoryPort,
            MerchantHistoryPort merchantHistoryPort,
            AlertPublisherPort alertPublisher,
            RiskScorer riskScorer,
            RiskPolicy riskPolicy
    ) {
        this.txRepo = txRepo;
        this.riskAssessmentRepo = riskAssessmentRepo;
        this.profilePort = profilePort;
        this.velocityPort = velocityPort;
        this.locationHistoryPort = locationHistoryPort;
        this.merchantHistoryPort = merchantHistoryPort;
        this.alertPublisher = alertPublisher;
        this.riskScorer = riskScorer;
        this.riskPolicy = riskPolicy;
    }

    @Override
    public IngestResult ingest(IngestTransactionCommand cmd) {
        TransactionId txId = new TransactionId(cmd.transactionId());
        AccountId accountId = new AccountId(cmd.accountId());
        MerchantId merchantId = new MerchantId(cmd.merchantId());
        Country country = new Country(cmd.country());
        Money money = new Money(cmd.amount(), Currency.getInstance(cmd.currency()));
        Instant occurredAt = cmd.occurredAt();

        if (txRepo.exists(txId)) {
            return new IngestResult.Duplicated(txId.value());
        }

        Transaction tx = new Transaction(txId, accountId, merchantId, money, country, occurredAt);

        boolean firstTimeMerchant = merchantHistoryPort.isFirstTimeMerchant(accountId, merchantId);
        AccountProfile profile = profilePort.load(accountId);
        LocationChange locationChange = locationHistoryPort.observe(accountId, occurredAt, country);
        VelocityStats velocity = velocityPort.observe(accountId, occurredAt, money, tx.merchantId());

        RiskAssessment assessment = riskScorer.score(
                tx,
                profile,
                velocity,
                locationChange,
                firstTimeMerchant
        );

        txRepo.save(tx);
        riskAssessmentRepo.save(txId, assessment);

        if (assessment.riskScore().value() >= riskPolicy.highRiskThreshold()) {
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
