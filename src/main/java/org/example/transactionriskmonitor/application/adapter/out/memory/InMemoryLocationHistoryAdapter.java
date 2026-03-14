package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.LocationChange;
import org.example.transactionriskmonitor.application.port.out.LocationHistoryPort;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.Country;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryLocationHistoryAdapter implements LocationHistoryPort {
    private final Duration rapidChangeThreshold;
    private final Map<AccountId, LastSeen> lastSeenByAccount = new ConcurrentHashMap<>();

    public InMemoryLocationHistoryAdapter(Duration rapidChangeThreshold) {
        this.rapidChangeThreshold = rapidChangeThreshold;
    }

    @Override
    public LocationChange observe(AccountId accountId, Instant occurredAt, Country currentCountry) {
        LastSeen prev = lastSeenByAccount.put(accountId, new LastSeen(currentCountry, occurredAt));

        if (prev == null) {
            return new LocationChange(false, null, Duration.ZERO);
        }

        boolean changedCountry = !prev.country.equals(currentCountry);
        Duration delta = Duration.between(prev.occurredAt, occurredAt).abs();

        boolean suspicious = changedCountry && delta.compareTo(rapidChangeThreshold) < 0;

        return new LocationChange(suspicious, prev.country, delta);
    }

    private record LastSeen(Country country, Instant occurredAt) {}
}
