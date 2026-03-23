package org.example.transactionriskmonitor.application.usecase;

import org.example.transactionriskmonitor.application.port.in.IngestResult;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionCommand;
import org.example.transactionriskmonitor.application.port.in.IngestTransactionUseCase;
import org.example.transactionriskmonitor.application.port.out.*;
import org.example.transactionriskmonitor.domain.model.AccountProfile;
import org.example.transactionriskmonitor.domain.model.Country;
import org.example.transactionriskmonitor.domain.model.Money;
import org.example.transactionriskmonitor.domain.model.TrustStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("postgres")
class IngestTransactionConcurrencyIT {

    @Autowired
    private IngestTransactionUseCase ingestTransactionUseCase;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void shouldAcceptOneRequestAndMarkSecondAsDuplicatedWhenSameTransactionIdIsSentConcurrently() throws Exception {
        String transactionId = "tx-concurrent-" + System.nanoTime();

        IngestTransactionCommand command1 = new IngestTransactionCommand(
                transactionId,
                "acc-2001",
                "m-9001",
                new BigDecimal("120.50"),
                "GBP",
                "GB",
                Instant.parse("2026-03-23T13:00:00Z")
        );

        IngestTransactionCommand command2 = new IngestTransactionCommand(
                transactionId,
                "acc-2001",
                "m-9001",
                new BigDecimal("120.50"),
                "GBP",
                "GB",
                Instant.parse("2026-03-23T13:00:00Z")
        );

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<IngestResult> future1 = executor.submit(createConcurrentTask(command1, ready, start));
        Future<IngestResult> future2 = executor.submit(createConcurrentTask(command2, ready, start));

        boolean readyCompleted = ready.await(5, TimeUnit.SECONDS);
        if (!readyCompleted) {
            throw new IllegalStateException("Threads did not reach ready state in time");
        }

        start.countDown();

        IngestResult result1 = future1.get(10, TimeUnit.SECONDS);
        IngestResult result2 = future2.get(10, TimeUnit.SECONDS);

        List<String> statuses = List.of(extractStatus(result1), extractStatus(result2));

        long acceptedCount = statuses.stream().filter("ACCEPTED"::equals).count();
        long duplicatedCount = statuses.stream().filter("DUPLICATED"::equals).count();

        assertEquals(1, acceptedCount);
        assertEquals(1, duplicatedCount);
    }

    private Callable<IngestResult> createConcurrentTask(
            IngestTransactionCommand command,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();
            try {
                start.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Thread interrupted while waiting to start test", ex);
            }
            return ingestTransactionUseCase.ingest(command);
        };
    }

    private String extractStatus(IngestResult result) {
        return switch (result) {
            case IngestResult.Accepted ignored -> "ACCEPTED";
            case IngestResult.Duplicated ignored -> "DUPLICATED";
        };
    }

    @TestConfiguration
    static class StubPortsConfig {

        @Bean
        @Primary
        AccountProfilePort testAccountProfilePort() {
            return accountId -> new AccountProfile(
                    false,
                    Set.of(new Country("GB")),
                    TrustStatus.TRUSTED
            );
        }

        @Bean
        @Primary
        VelocityPort testVelocityPort() {
            return (accountId, occurredAt, amount, merchantId) ->
                    new VelocityStats(
                            1,
                            new Money(BigDecimal.ZERO, Currency.getInstance("GBP")),
                            1
                    );
        }

        @Bean
        @Primary
        LocationHistoryPort testLocationHistoryPort() {
            return (accountId, occurredAt, currentCountry) ->
                    new LocationChange(false, null, Duration.ZERO);
        }

        @Bean
        @Primary
        MerchantHistoryPort testMerchantHistoryPort() {
            return (accountId, merchantId) -> false;
        }

        @Bean
        @Primary
        AlertPublisherPort testAlertPublisherPort() {
            return alert -> {
                // no-op
            };
        }
    }
}