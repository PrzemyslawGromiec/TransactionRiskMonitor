package org.example.transactionriskmonitor.application.port.in;

import java.time.Instant;

public record IngestTransactionCommand(
    String transactionId,
    String accountId,
    String amount,
    String currency,
    String country,
    Instant occurredAt
) {}
