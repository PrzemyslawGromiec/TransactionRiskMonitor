package org.example.transactionriskmonitor.application.port.in;

import org.example.transactionriskmonitor.domain.model.RiskReason;
import org.example.transactionriskmonitor.domain.model.RiskScore;

import java.time.Instant;
import java.util.Set;

public sealed interface IngestResult permits IngestResult.Accepted, IngestResult.Duplicated{
    record Accepted(String transactionId, RiskScore riskScore, Set<RiskReason> reasons, Instant occurredAt) implements IngestResult {}
    record Duplicated(String transactionId) implements IngestResult {}
}
