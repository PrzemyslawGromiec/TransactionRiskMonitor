package org.example.transactionriskmonitor.application.adapter.out.persistence.mapper;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.AccountProfileJpaEntity;
import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.Country;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccountProfilePersistenceMapper {

    public AccountProfile toDomain(AccountProfileJpaEntity entity) {
        Set<Country> highRiskCountries = entity.getHighRiskCountries()
                .stream()
                .map(Country::new)
                .collect(Collectors.toSet());

        return new AccountProfile(
                entity.isNewAccount(),
                highRiskCountries,
                entity.getTrustStatus()
        );
    }
}
