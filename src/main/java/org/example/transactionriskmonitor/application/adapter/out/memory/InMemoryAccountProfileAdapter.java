package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.AccountProfilePort;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.AccountProfile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountProfileAdapter implements AccountProfilePort {
    private final Map<AccountId, AccountProfile> profiles = new ConcurrentHashMap<>();


    @Override
    public AccountProfile load(AccountId accountId) {
        AccountProfile profile = profiles.get(accountId);
        if (profile == null) {
            throw new IllegalArgumentException("No account profile found for accountId: " + accountId);
        }
        return profile;
    }

    public void put(AccountId accountId, AccountProfile profile) {
        profiles.put(accountId, profile);
    }
}
