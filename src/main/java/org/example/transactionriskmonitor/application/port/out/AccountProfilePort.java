package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.AccountProfile;

public interface AccountProfilePort {
    AccountProfile load(AccountId accountId);
}
