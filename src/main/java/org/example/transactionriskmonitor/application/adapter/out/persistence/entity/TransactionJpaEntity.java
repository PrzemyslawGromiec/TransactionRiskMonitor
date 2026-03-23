package org.example.transactionriskmonitor.application.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_tx_account", columnList = "account_id"),
                @Index(name = "idx_tx_occurred", columnList = "occurred_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transactions_transaction_id",
                        columnNames = "transaction_id")
        }
)
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TransactionJpaEntity() {

    }

    public TransactionJpaEntity(
            String transactionId,
            String accountId,
            String merchantId,
            BigDecimal amount,
            String currency,
            String country,
            Instant occurredAt
    ) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.country = country;
        this.occurredAt = occurredAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

}
