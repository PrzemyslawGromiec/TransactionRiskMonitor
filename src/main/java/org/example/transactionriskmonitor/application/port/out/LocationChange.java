package org.example.transactionriskmonitor.application.port.out;

import org.example.transactionriskmonitor.domain.model.Country;

import java.time.Duration;

public record LocationChange(
        boolean suspicious,
        Country previousCountry,
        Duration timeSincePrevious
) {
}
