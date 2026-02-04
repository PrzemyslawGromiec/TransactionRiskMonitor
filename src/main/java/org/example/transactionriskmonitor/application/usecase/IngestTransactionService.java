package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.AccountProfilePort;
import org.example.transactionriskmonitor.application.port.out.AlertPublisherPort;
import org.example.transactionriskmonitor.application.port.out.TransactionRepositoryPort;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;
import org.example.transactionriskmonitor.domain.model.*;
import org.example.transactionriskmonitor.domain.service.RiskScorer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.EnumSet;

public final class IngestTransactionService implements IngestTransactionUseCase {
    private static final int HIGH_RISK_THRESHOLD = 80;
    private final TransactionRepositoryPort txRepo;
    private final AccountProfilePort profilePort;
    private final AlertPublisherPort alertPublisher;
    private final RiskScorer riskScorer;

    public IngestTransactionService(
            TransactionRepositoryPort txRepo,
            AccountProfilePort profilePort,
            AlertPublisherPort alertPublisher,
            RiskScorer riskScorer
    ) {
        this.txRepo = txRepo;
        this.profilePort = profilePort;
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
        RiskScore score = riskScorer.score(tx, profile);
        txRepo.save(tx, score);

        if (score.value() >= HIGH_RISK_THRESHOLD) {
            EnumSet<RiskReason> reasons = EnumSet.noneOf(RiskReason.class);
            HighRiskAlert alert = new HighRiskAlert(
                    txId,
                    accountId,
                    score,
                    reasons,
                    occurredAt
            );
            alertPublisher.publish(alert);
        }
        return new IngestResult.Accepted(txId.value(), score);
    }
}
