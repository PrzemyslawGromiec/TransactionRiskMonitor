package org.example.transactionriskmonitor.application.adapter.out.persistence;

import org.example.transactionriskmonitor.application.adapter.out.persistence.mapper.AccountProfilePersistenceMapper;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.example.transactionriskmonitor.application.port.out.AccountProfilePort;
import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataAccountProfileJpaRepository;
import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.AccountProfileJpaEntity;

@Component
@Profile("postgres")
public class JpaAccountProfileRepositoryAdapter implements AccountProfilePort {
    private final SpringDataAccountProfileJpaRepository repository;
    private final AccountProfilePersistenceMapper mapper;

    public JpaAccountProfileRepositoryAdapter(
            SpringDataAccountProfileJpaRepository repository, AccountProfilePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AccountProfile load(AccountId accountId) {

        AccountProfileJpaEntity entity = repository.findById(accountId.value())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No account profile found for accountId: " + accountId));

        return mapper.toDomain(entity);
    }
}
