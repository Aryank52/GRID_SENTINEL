package com.gridsentinel.risk;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * Individual factor contribution to a risk score.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class FactorContribution {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double weight; // [0.0, 1.0], sum of all weights ≈ 1.0

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double rawValue; // Raw measured value (e.g., temperature in Celsius)

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double normalizedValue; // Normalized to [0.0, 1.0] for this factor

    @Setter(AccessLevel.PRIVATE)
    private String description;

    public FactorContribution(double weight, double rawValue, double normalizedValue) {
        this(weight, rawValue, normalizedValue, null);
    }

    public FactorContribution(double weight, double rawValue, double normalizedValue, String description) {
        if (weight < 0.0 || weight > 1.0) {
            throw new IllegalArgumentException("Weight must be in [0.0, 1.0]");
        }
        if (normalizedValue < 0.0 || normalizedValue > 1.0) {
            throw new IllegalArgumentException("Normalized value must be in [0.0, 1.0]");
        }
        this.weight = weight;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.description = description;
    }

    /**
     * Absolute contribution to final score (weight * normalizedValue).
     */
    public double contribution() {
        return weight * normalizedValue;
    }

    public static FactorContribution of(double weight, double rawValue, double normalizedValue) {
        return new FactorContribution(weight, rawValue, normalizedValue);
    }

    public static FactorContribution of(double weight, double rawValue, double normalizedValue, String description) {
        return new FactorContribution(weight, rawValue, normalizedValue, description);
    }
}