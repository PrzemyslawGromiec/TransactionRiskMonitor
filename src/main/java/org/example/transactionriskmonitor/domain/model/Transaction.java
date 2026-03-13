package org.example.transactionriskmonitor.domain.model;

import java.time.Instant;

public record Transaction(TransactionId id, AccountId accountId, MerchantId merchantId, Money money, Country country, Instant occurredAt) {
    public Transaction {
        if (id == null || accountId == null || merchantId == null || money == null || country == null || occurredAt == null) {
            throw new IllegalArgumentException("transaction fields missing");
        }
    }
}
