package com.gridsentinel.risk;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Breakdown of risk score into contributing factors with weights.
 * <p>
 * Weights should sum to approximately 1.0.
 * Each factor has a weight, raw value, and normalized contribution.
 * </p>
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class FactorBreakdown {

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Map<String, FactorContribution> contributions = new HashMap<>();

    @Setter(AccessLevel.PRIVATE)
    private Double totalWeight;

    private FactorBreakdown(Builder builder) {
        this.contributions = Map.copyOf(Objects.requireNonNull(builder.contributions, "Contributions cannot be null"));
        this.totalWeight = calculateTotalWeight();
        validateWeights();
    }

    private double calculateTotalWeight() {
        return contributions.values().stream()
                .mapToDouble(FactorContribution::getWeight)
                .sum();
    }

    private void validateWeights() {
        double sum = totalWeight;
        if (sum < 0.95 || sum > 1.05) {
            throw new IllegalArgumentException("Factor weights must sum to approximately 1.0 (got " + sum + ")");
        }
    }

    public FactorContribution getContribution(String factorName) {
        return contributions.get(factorName);
    }

    public Map<String, FactorContribution> getAllContributions() {
        return Collections.unmodifiableMap(contributions);
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Get top N contributing factors by absolute contribution.
     */
    public java.util.List<FactorContribution> getTopFactors(int n) {
        return contributions.values().stream()
                .sorted((a, b) -> Double.compare(Math.abs(b.contribution()), Math.abs(a.contribution())))
                .limit(n)
                .toList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Map<String, FactorContribution> contributions = new HashMap<>();

        private Builder() {}

        public Builder addFactor(String name, FactorContribution contribution) {
            this.contributions.put(Objects.requireNonNull(name), Objects.requireNonNull(contribution));
            return this;
        }

        public Builder addFactor(RiskFactor factor, double weight, double rawValue, double normalizedValue) {
            this.contributions.put(factor.name(), new FactorContribution(weight, rawValue, normalizedValue, factor.getDescription()));
            return this;
        }

        public FactorBreakdown build() {
            return new FactorBreakdown(this);
        }
    }
}