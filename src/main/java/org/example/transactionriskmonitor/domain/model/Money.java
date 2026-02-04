package org.example.transactionriskmonitor.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Invalid money.");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Negative amount.");
        }
        amount = amount.stripTrailingZeros();
    }
}
