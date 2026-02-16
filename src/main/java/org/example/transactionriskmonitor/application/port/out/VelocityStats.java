package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.Money;

public record VelocityStats(int countInWindow, Money sumInWindow) {
}
