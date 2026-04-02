package org.example.transactionriskmonitor.application.adapter.out.persistence.seeder;

import org.example.transactionriskmonitor.application.adapter.out.persistence.entity.AccountProfileJpaEntity;
import org.example.transactionriskmonitor.application.adapter.out.persistence.repository.SpringDataAccountProfileJpaRepository;
import org.example.transactionriskmonitor.domain.model.TrustStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("postgres")
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

        repository.save(new AccountProfileJpaEntity(
                "acc-1001",
                false,
                TrustStatus.TRUSTED,
                Set.of()
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-1002",
                false,
                TrustStatus.NORMAL,
                Set.of()
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-1003",
                false,
                TrustStatus.TRUSTED,
                Set.of("RU")
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-2001",
                true,
                TrustStatus.NORMAL,
                Set.of("NG", "PK")
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-2002",
                true,
                TrustStatus.NORMAL,
                Set.of()
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-2003",
                true,
                TrustStatus.NORMAL,
                Set.of("NG", "PK", "IR")
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-3001",
                false,
                TrustStatus.FLAGGED,
                Set.of("RU", "IR")
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-3002",
                false,
                TrustStatus.FLAGGED,
                Set.of("RU", "IR", "KP")
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-4001",
                true,
                TrustStatus.TRUSTED,
                Set.of()
        ));

        repository.save(new AccountProfileJpaEntity(
                "acc-5001",
                false,
                TrustStatus.NORMAL,
                Set.of("NG", "PK", "RU", "IR")
        ));
    }
}
