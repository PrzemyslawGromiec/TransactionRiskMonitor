package org.example.transactionriskmonitor.application.port.in;

import org.example.transactionriskmonitor.domain.model.RiskScore;

public sealed interface IngestResult permits IngestResult.Accepted, IngestResult.Duplicated{
    record Accepted(String transactionId, RiskScore riskScore) implements IngestResult {}
    record Duplicated(String transactionId) implements IngestResult {}
}
