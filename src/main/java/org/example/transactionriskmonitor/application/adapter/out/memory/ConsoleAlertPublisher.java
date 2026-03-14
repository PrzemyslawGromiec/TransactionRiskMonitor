package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.AlertPublisherPort;
import org.example.transactionriskmonitor.domain.event.HighRiskAlert;

public class ConsoleAlertPublisher implements AlertPublisherPort {
    @Override
    public void publish(HighRiskAlert alert) {
        System.out.println("=== HIGH RISK ALERT ===");
        System.out.println("Transaction ID: " + alert.transactionId().value());
        System.out.println("Account ID: " + alert.accountId().value());
        System.out.println("Risk score: " + alert.riskScore().value());
        System.out.println("Reasons: " + alert.reasons());
        System.out.println("Occurred at: " + alert.occurredAt());
        System.out.println("=======================");
    }
}
