package org.example.transactionriskmonitor.config;

import org.example.transactionriskmonitor.application.adapter.out.memory.InMemoryAccountProfileAdapter;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.AccountProfilePort;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.Country;
import org.example.transactionriskmonitor.domain.model.TrustStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.util.Set;

@Configuration
@Profile("memory-demo")
public class DemoRunnerConfig {

    @Bean
    CommandLineRunner demoRunner(
            IngestTransactionUseCase ingestTransactionUseCase,
            AccountProfilePort accountProfilePort
    ) {
        return args -> {
            if (accountProfilePort instanceof InMemoryAccountProfileAdapter profileAdapter) {
                profileAdapter.put(
                        new AccountId("acc-1"),
                        new AccountProfile(
                                false,
                                Set.of(new Country("IR")),
                                TrustStatus.FLAGGED
                        )
                );
            }

            Instant now = Instant.now();

            var tx1 = new IngestTransactionCommand(
                    "tx-1",
                    "acc-1",
                    "amazon",
                    "9000.00",
                    "GBP",
                    "IR",
                    now
            );

            var tx2 = new IngestTransactionCommand(
                    "tx-2",
                    "acc-1",
                    "amazon",
                    "9000.00",
                    "GBP",
                    "US",
                    now.plusSeconds(60)
            );

            var tx3 = new IngestTransactionCommand(
                    "tx-3",
                    "acc-1",
                    "amazon",
                    "9000.00",
                    "GBP",
                    "GB",
                    now.plusSeconds(120)
            );

            var tx4 = new IngestTransactionCommand(
                    "tx-1",
                    "acc-1",
                    "amazon",
                    "9000.00",
                    "GBP",
                    "IR",
                    now
            );

            System.out.println(ingestTransactionUseCase.ingest(tx1));
            System.out.println(ingestTransactionUseCase.ingest(tx2));
            System.out.println(ingestTransactionUseCase.ingest(tx3));
            System.out.println(ingestTransactionUseCase.ingest(tx4));
        };
    }
}
