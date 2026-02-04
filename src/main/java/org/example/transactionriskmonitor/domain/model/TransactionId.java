package org.example.transactionriskmonitor.domain.model;

public record TransactionId(String value) {
    public TransactionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TransactionId is blank");
        }
        value = value.trim();
    }
}
