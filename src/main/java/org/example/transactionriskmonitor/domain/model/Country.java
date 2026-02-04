package org.example.transactionriskmonitor.domain.model;

public record Country(String name) {
    public Country {
        if (name == null || !name.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Country must be ISO2 (e.g., GB)");
        }
        name = name.trim();
    }
}
