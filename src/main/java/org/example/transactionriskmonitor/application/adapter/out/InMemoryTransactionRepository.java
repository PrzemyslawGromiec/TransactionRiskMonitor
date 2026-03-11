package org.example.transactionriskmonitor.application.adapter.out;

import org.example.transactionriskmonitor.application.port.out.TransactionRepositoryPort;
import org.example.transactionriskmonitor.domain.model.RiskScore;
import org.example.transactionriskmonitor.domain.model.Transaction;
import org.example.transactionriskmonitor.domain.model.TransactionId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTransactionRepository  implements TransactionRepositoryPort {
    private final Map<TransactionId, StoredTransaction> store = new ConcurrentHashMap<>();

    @Override
    public boolean exists(TransactionId id) {
        return store.containsKey(id);
    }

    @Override
    public void save(Transaction tx, RiskScore score) {
        store.put(tx.id(), new StoredTransaction(tx, score));
    }

    public Map<TransactionId, StoredTransaction> store() {
        return Map.copyOf(store);
    }

    public record StoredTransaction(Transaction transaction, RiskScore riskScore) {}
}
