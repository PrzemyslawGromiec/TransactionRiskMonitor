package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.MerchantId;

public interface MerchantHistoryPort {
    boolean isFirstTimeMerchant(AccountId accountId, MerchantId merchantId);
}
