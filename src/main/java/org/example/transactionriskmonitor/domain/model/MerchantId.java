package org.example.transactionriskmonitor.domain.model;

public record MerchantId(String value) {
    public MerchantId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("MerchantId must not be blank");
        }
        value = value.trim();
    }
}
