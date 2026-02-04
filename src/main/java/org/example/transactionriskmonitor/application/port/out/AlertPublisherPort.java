package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.event.HighRiskAlert;

public interface AlertPublisherPort {
    void publish(HighRiskAlert alert);
}
