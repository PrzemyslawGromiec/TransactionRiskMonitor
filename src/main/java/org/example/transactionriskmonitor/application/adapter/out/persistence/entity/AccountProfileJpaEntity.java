package org.example.transactionriskmonitor.application.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.example.transactionriskmonitor.domain.model.TrustStatus;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "account_profiles")
public class AccountProfileJpaEntity {
    @Id
    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "is_new_account", nullable = false)
    private boolean newAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_status", nullable = false)
    private TrustStatus trustStatus;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "account_profile_high_risk_countries",
            joinColumns = @JoinColumn(name = "account_id")
    )
    @Column(name = "country_code")
    private Set<String> highRiskCountries = new HashSet<>();

    protected AccountProfileJpaEntity() {
    }

    public AccountProfileJpaEntity(
            String accountId,
            boolean newAccount,
            TrustStatus trustStatus,
            Set<String> highRiskCountries
    ) {
        this.accountId = accountId;
        this.newAccount = newAccount;
        this.trustStatus = trustStatus;
        this.highRiskCountries = highRiskCountries;
    }

    public String getAccountId() {
        return accountId;
    }

    public boolean isNewAccount() {
        return newAccount;
    }

    public TrustStatus getTrustStatus() {
        return trustStatus;
    }

    public Set<String> getHighRiskCountries() {
        return highRiskCountries;
    }
}
