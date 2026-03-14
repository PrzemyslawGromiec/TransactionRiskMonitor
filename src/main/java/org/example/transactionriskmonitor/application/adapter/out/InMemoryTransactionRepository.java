package org.example.transactionriskmonitor.application.adapter.out;

import org.example.transactionriskmonitor.application.port.out.TransactionRepositoryPort;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;
import org.example.transactionriskmonitor.domain.model.TransactionId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTransactionRepository  implements TransactionRepositoryPort {
    private final Map<TransactionId, Transaction> store = new ConcurrentHashMap<>();

    @Override
    public boolean exists(TransactionId id) {
        return store.containsKey(id);
    }

    @Override
    public Transaction save(Transaction tx) {
        store.put(tx.id(), tx);
        return tx;
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return Optional.ofNullable(store.get(id));
    }

    public Map<TransactionId, Transaction> store() {
        return Map.copyOf(store);
    }
}
