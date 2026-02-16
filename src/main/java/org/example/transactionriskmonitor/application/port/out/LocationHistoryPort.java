package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.Country;

import java.time.Instant;

public interface LocationHistoryPort {
    LocationChange observe(AccountId accountId, Instant occurredAt, Country currentCountry);
}
