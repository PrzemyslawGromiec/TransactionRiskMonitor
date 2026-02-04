package org.example.transactionriskmonitor.domain.model;

import java.util.Set;

public final class AccountProfile {
    private final boolean newAccount;
    private final Set<Country> highRiskCountries;
    private final TrustStatus trustStatus;

    public AccountProfile(boolean newAccount, Set<Country> highRiskCountries, TrustStatus trustStatus) {
        this.newAccount = newAccount;
        this.highRiskCountries = highRiskCountries;
        this.trustStatus = trustStatus;
    }

    public boolean isNewAccount() {
        return newAccount;
    }

    public TrustStatus trustStatus() {
        return trustStatus;
    }

    public boolean isCountryHighRisk(Country country) {
        return highRiskCountries.contains(country);
    }
}
