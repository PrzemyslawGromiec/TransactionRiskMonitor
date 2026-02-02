package domain.model;

import java.util.Set;

public final class AccountProfile {
    private final boolean newAccount;
    private final Set<Country> highRiskCountries;

    public AccountProfile(boolean newAccount, Set<Country> highRiskCountries) {
        this.newAccount = newAccount;
        this.highRiskCountries = highRiskCountries;
    }

    public boolean isNewAccount() {
        return newAccount;
    }

    public boolean isCountryHighRisk(Country country) {
        return highRiskCountries.contains(country);
    }
}
