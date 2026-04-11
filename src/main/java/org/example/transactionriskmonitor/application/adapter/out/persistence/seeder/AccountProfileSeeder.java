package org.example.transactionriskmonitor.application.adapter.out.persistence.seeder;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.AccountProfileJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataAccountProfileJpaRepository;
import org.example.transactionriskmonitor.config.SeedAccounts;
import org.example.transactionriskmonitor.domain.model.TrustStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile({"postgres-demo", "postgres"})
@Order(1)
public class AccountProfileSeeder implements CommandLineRunner {

    private final SpringDataAccountProfileJpaRepository repository;

    public AccountProfileSeeder(SpringDataAccountProfileJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // already seeded
        }

        //trusted and safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.TRUSTED_SAFE_1,
                false,
                TrustStatus.TRUSTED,
                Set.of()
        ));

        //normal and safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NORMAL_SAFE_1,
                false,
                TrustStatus.NORMAL,
                Set.of()
        ));

        //trusted, risky country
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.TRUSTED_RISKY_1,
                false,
                TrustStatus.TRUSTED,
                Set.of("RU")
        ));

        //normal, risky country
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NORMAL_RISKY_1,
                false,
                TrustStatus.NORMAL,
                Set.of("NG")
        ));

        //normal, safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NORMAL_SAFE_2,
                false,
                TrustStatus.NORMAL,
                Set.of()
        ));

        //new, normal, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NEW_NORMAL_RISKY_1,
                true,
                TrustStatus.NORMAL,
                Set.of("NG", "PK")
        ));

        //new, normal and safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NEW_NORMAL_SAFE_1,
                true,
                TrustStatus.NORMAL,
                Set.of()
        ));

        //new, normal, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NEW_NORMAL_RISKY_2,
                true,
                TrustStatus.NORMAL,
                Set.of("NG", "PK", "IR")
        ));

        //flagged, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.FLAGGED_RISKY_1,
                false,
                TrustStatus.FLAGGED,
                Set.of("RU", "IR")
        ));

        //flagged, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.FLAGGED_RISKY_2,
                false,
                TrustStatus.FLAGGED,
                Set.of("RU", "IR", "KP")
        ));

        //flagged, safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.FLAGGED_SAFE_1,
                false,
                TrustStatus.FLAGGED,
                Set.of()
        ));

        //new, trusted, safe
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NEW_TRUSTED_SAFE_1,
                true,
                TrustStatus.TRUSTED,
                Set.of()
        ));

        //trusted, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.TRUSTED_RISKY_2,
                false,
                TrustStatus.TRUSTED,
                Set.of("RU", "IR")
        ));

        //normal, risky countries
        repository.save(new AccountProfileJpaEntity(
                SeedAccounts.NORMAL_RISKY_2,
                false,
                TrustStatus.NORMAL,
                Set.of("NG", "PK", "RU", "IR")
        ));
    }
}
