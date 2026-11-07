package com.gridsentinel.risk;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

/**
 * Recommended protection action with expected risk reduction and cost.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtectionAction {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String actionId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ActionCategory category;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double estimatedCost; // USD

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double expectedRiskReduction; // [0.0, 1.0] absolute risk score reduction

    @Setter(AccessLevel.PRIVATE)
    private Double confidence; // [0.0, 1.0] confidence in risk reduction estimate

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.TimeRange implementationWindow;

    @Setter(AccessLevel.PRIVATE)
    private Integer priority; // 1 = highest

    private ProtectionAction(Builder builder) {
        this.actionId = Objects.requireNonNull(builder.actionId, "Action ID cannot be null");
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.category = Objects.requireNonNull(builder.category, "Category cannot be null");
        this.estimatedCost = Objects.requireNonNull(builder.estimatedCost, "Estimated cost cannot be null");
        if (this.estimatedCost < 0) {
            throw new IllegalArgumentException("Estimated cost cannot be negative");
        }
        this.expectedRiskReduction = Objects.requireNonNull(builder.expectedRiskReduction, "Expected risk reduction cannot be null");
        if (this.expectedRiskReduction < 0.0 || this.expectedRiskReduction > 1.0) {
            throw new IllegalArgumentException("Expected risk reduction must be in [0.0, 1.0]");
        }
        this.confidence = builder.confidence != null ? builder.confidence : 0.8;
        if (this.confidence < 0.0 || this.confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be in [0.0, 1.0]");
        }
        this.implementationWindow = builder.implementationWindow;
        this.priority = builder.priority;
    }

    /**
     * Cost-effectiveness ratio: risk reduction per dollar (higher = better).
     */
    public double costEffectiveness() {
        if (estimatedCost <= 0) return Double.MAX_VALUE;
        return expectedRiskReduction / estimatedCost * 1_000_000; // per $1M
    }

    /**
     * Risk reduction per dollar adjusted by confidence.
     */
    public double adjustedCostEffectiveness() {
        return costEffectiveness() * confidence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String actionId = "ACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String name;
        private String description;
        private ActionCategory category;
        private Double estimatedCost;
        private Double expectedRiskReduction;
        private Double confidence;
        private CommonKernel.TimeRange implementationWindow;
        private Integer priority;

        private Builder() {}

        public Builder actionId(String actionId) {
            this.actionId = Objects.requireNonNull(actionId);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(ActionCategory category) {
            this.category = category;
            return this;
        }

        public Builder estimatedCost(double estimatedCost) {
            this.estimatedCost = estimatedCost;
            return this;
        }

        public Builder expectedRiskReduction(double expectedRiskReduction) {
            this.expectedRiskReduction = expectedRiskReduction;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder implementationWindow(CommonKernel.TimeRange implementationWindow) {
            this.implementationWindow = implementationWindow;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public ProtectionAction build() {
            return new ProtectionAction(this);
        }
    }
}