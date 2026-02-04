package org.example.transactionriskmonitor.domain.model;

public record AccountId(String value) {
    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AccountId is blank.");
        }
        value = value.trim();
    }
}
