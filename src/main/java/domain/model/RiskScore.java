package domain.model;

public record RiskScore(int value) {
    public RiskScore {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("RiskScore must be a number from 0 to 100.");
        }
    }

    public boolean isHighRisk() {
        return value >= 100;
    }
}
