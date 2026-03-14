package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;
import org.example.transactionriskmonitor.domain.model.TransactionId;

import java.util.Optional;

public interface TransactionRepositoryPort {
    boolean exists(TransactionId id);
    Transaction save(Transaction tx);
    Optional<Transaction> findById(TransactionId id);
}
