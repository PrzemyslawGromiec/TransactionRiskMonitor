package org.example.transactionriskmonitor.application.adapter.out.memory;

import org.example.transactionriskmonitor.application.port.out.VelocityPort;
import org.example.transactionriskmonitor.application.port.out.VelocityStats;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.MerchantId;
import org.example.transactionriskmonitor.domain.model.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class InMemoryVelocityAdapter implements VelocityPort {
    private final Duration window;
    private final Map<AccountId, Deque<TransactionEvent>> byAccount = new HashMap<>();

    public InMemoryVelocityAdapter(Duration window) {
        this.window = window;
    }

    // tracking recent transactions for each account in memory and returning velocity stats for risk scoring
    @Override
    public VelocityStats observe(AccountId accountId, Instant occurredAt, Money amount, MerchantId merchantId) {
        Deque<TransactionEvent> recentTransactionsInWindow = byAccount.computeIfAbsent(accountId, a -> new ArrayDeque<>());

        // anything older than this moment is no longer relevant
        Instant cutOff = occurredAt.minus(window);
        while (!recentTransactionsInWindow.isEmpty() && recentTransactionsInWindow.peekFirst().occurredAt.isBefore(cutOff)) {
            recentTransactionsInWindow.removeFirst();
        }

        recentTransactionsInWindow.addLast(new TransactionEvent(occurredAt, amount, merchantId));

        // calculate total amount in the window and number of unique merchants in the window
        Money sum = Money.zero(amount.currency());
        Set<MerchantId> distinctMerchants = new HashSet<>();
        for (TransactionEvent t : recentTransactionsInWindow) {
            sum = sum.plus(t.amount());
            distinctMerchants.add(t.merchantId);
        }

        return new VelocityStats(recentTransactionsInWindow.size(), sum, distinctMerchants.size());
    }

    public record TransactionEvent(Instant occurredAt, Money amount, MerchantId merchantId) {}
}
