package org.example.transactionriskmonitor.application.adapter.out;

import org.example.transactionriskmonitor.application.port.out.VelocityPort;
import org.example.transactionriskmonitor.application.port.out.VelocityStats;
import org.example.transactionriskmonitor.domain.model.AccountId;
import org.example.transactionriskmonitor.domain.model.Money;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class InMemoryVelocityAdapter implements VelocityPort {
    private final Duration window;
    private final Map<AccountId, Deque<TxTick>> byAccount = new HashMap<>();

    public InMemoryVelocityAdapter(Duration window) {
        this.window = window;
    }

    @Override
    public VelocityStats observe(AccountId accountId, Instant occurredAt, Money amount) {
        Deque<TxTick> txTickDeque = byAccount.computeIfAbsent(accountId, a -> new ArrayDeque<>());

        Instant cutOff = occurredAt.minus(window);
        while (!txTickDeque.isEmpty() && txTickDeque.peekFirst().occurredAt.isBefore(cutOff)) {
            txTickDeque.removeFirst();
        }

        txTickDeque.addLast(new TxTick(occurredAt, amount));

        Money sum = Money.zero(amount.currency());
        for (TxTick t : txTickDeque) {
            sum = sum.plus(t.amount());
        }

        return new VelocityStats(txTickDeque.size(), sum);
    }

    public record TxTick(Instant occurredAt, Money amount) {

    }
}
