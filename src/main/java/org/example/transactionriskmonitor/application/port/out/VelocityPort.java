package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.Money;

import java.time.Instant;

public interface VelocityPort {
    VelocityStats observe (AccountId accountId, Instant occurredAt, Money money);
}
