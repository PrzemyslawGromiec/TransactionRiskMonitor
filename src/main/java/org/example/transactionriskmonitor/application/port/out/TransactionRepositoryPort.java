package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;
import org.example.transactionriskmonitor.domain.model.TransactionId;

public interface TransactionRepositoryPort {
    boolean exists(TransactionId id);
    void save(Transaction tx, RiskScore score);
}
