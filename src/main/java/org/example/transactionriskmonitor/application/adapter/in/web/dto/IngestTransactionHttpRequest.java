package org.example.transactionriskmonitor.application.adapter.in.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record IngestTransactionHttpRequest(
        @NotBlank(message = "transactionId must not be blank")
        String transactionId,

        @NotBlank(message = "accountId must not be blank")
        String accountId,
        @NotNull(message = "amount must not be null")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "currency must not be blank")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code, e.g. GBP")
        String currency,

        @NotBlank(message = "merchantId must not be blank")
        String merchantId,

        @NotBlank(message = "country must not be blank")
        @Pattern(regexp = "^[A-Z]{2}$", message = "country must be a 2-letter ISO code, e.g. GB")
        String country,

        @NotNull(message = "occurredAt must not be null")
        @PastOrPresent(message = "occurredAt cannot be in the future")
        Instant occurredAt
) {
}
