package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.MerchantHistoryPort;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.MerchantId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InMemoryMerchantHistoryAdapter implements MerchantHistoryPort {
    private final Map<AccountId, Set<MerchantId>> merchantHistoryByAccount = new HashMap<>();

    @Override
    public boolean isFirstTimeMerchant(AccountId accountId, MerchantId merchantId) {
        Set<MerchantId> merchants = merchantHistoryByAccount.computeIfAbsent(accountId, a -> new HashSet<>());
        boolean firstTime = !merchants.contains(merchantId);
        merchants.add(merchantId);
        return firstTime;
    }
}
