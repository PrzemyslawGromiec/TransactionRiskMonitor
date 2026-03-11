package org.example.transactionriskmonitor.domain.model;

public record VelocitySignal(boolean suspicious) {

    public static VelocitySignal flagged() {
        return new VelocitySignal(true);
    }

    public static VelocitySignal normal() {
        return new VelocitySignal(false);
    }
}
