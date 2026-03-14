package org.example.transactionriskmonitor.application.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentJpaEntity {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "reasons", nullable = false, length = 1000)
    private String reasons;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected RiskAssessmentJpaEntity() {
    }

    public RiskAssessmentJpaEntity(
            String transactionId,
            int riskScore,
            String reasons,
            Instant assessedAt
    ) {
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.reasons = reasons;
        this.assessedAt = assessedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getReasons() {
        return reasons;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }
}
