package org.example.transactionriskmonitor.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public record IngestTransactionCommand(
    String transactionId,
    String accountId,
    String merchantId,
    BigDecimal amount,
    String currency,
    String country,
    Instant occurredAt
) {}
