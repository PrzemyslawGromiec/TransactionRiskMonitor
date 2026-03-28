package org.example.transactionriskmonitor.application.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.example.transactionriskmonitor.domain.model.RiskReason;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentJpaEntity {

    @Id
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private String transactionId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @ElementCollection(targetClass = RiskReason.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "risk_assessment_reasons",
            joinColumns = @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    )
    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<RiskReason> reasons = new HashSet<>();

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected RiskAssessmentJpaEntity() {
    }

    public RiskAssessmentJpaEntity(
            String transactionId,
            int riskScore,
            Set<RiskReason> reasons,
            Instant assessedAt
    ) {
        this.transactionId = transactionId;
        this.riskScore = riskScore;
        this.reasons = reasons != null ? new HashSet<>(reasons) : new HashSet<>();
        this.assessedAt = assessedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public Set<RiskReason> getReasons() {
        return reasons;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public void setReasons(Set<RiskReason> reasons) {
        this.reasons = reasons != null ? new HashSet<>(reasons) : new HashSet<>();
    }

    public void setAssessedAt(Instant assessedAt) {
        this.assessedAt = assessedAt;
    }
}
