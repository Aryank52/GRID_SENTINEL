package com.gridsentinel.simulation;

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
 * Detailed impact on a specific asset from simulation.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssetImpact {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private ImpactType type;

    @Setter(AccessLevel.PRIVATE)
    private Double severity; // [0.0, 1.0]

    @Setter(AccessLevel.PRIVATE)
    private Double voltagePu; // Per unit voltage

    @Setter(AccessLevel.PRIVATE)
    private Double thermalLoadingPu; // Per unit thermal loading

    @Setter(AccessLevel.PRIVATE)
    private Double frequencyHz; // Actual frequency

    @Setter(AccessLevel.PRIVATE)
    private Integer iterationFailed; // Cascade iteration when failed

    @Setter(AccessLevel.PRIVATE)
    private String failureCause; // Description

    @Setter(AccessLevel.PRIVATE)
    private Boolean recoverable; // Can be restored without replacement

    private AssetImpact(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "Impact type cannot be null");
        this.severity = builder.severity;
        this.voltagePu = builder.voltagePu;
        this.thermalLoadingPu = builder.thermalLoadingPu;
        this.frequencyHz = builder.frequencyHz;
        this.iterationFailed = builder.iterationFailed;
        this.failureCause = builder.failureCause;
        this.recoverable = builder.recoverable != null ? builder.recoverable : true;
    }

    public enum ImpactType {
        NONE,
        OVERLOADED,
        UNDERVOLTAGE,
        OVERVOLTAGE,
        FREQUENCY_DEVIATION,
        ISLANDED,
        DEENERGIZED,
        CASCADING_FAILURE,
        PROTECTION_OPERATION,
        COMMUNICATION_LOSS,
        PHYSICAL_DAMAGE
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ImpactType type;
        private Double severity;
        private Double voltagePu;
        private Double thermalLoadingPu;
        private Double frequencyHz;
        private Integer iterationFailed;
        private String failureCause;
        private Boolean recoverable;

        private Builder() {}

        public Builder type(ImpactType type) {
            this.type = type;
            return this;
        }

        public Builder severity(double severity) {
            this.severity = severity;
            return this;
        }

        public Builder voltagePu(double voltagePu) {
            this.voltagePu = voltagePu;
            return this;
        }

        public Builder thermalLoadingPu(double thermalLoadingPu) {
            this.thermalLoadingPu = thermalLoadingPu;
            return this;
        }

        public Builder frequencyHz(double frequencyHz) {
            this.frequencyHz = frequencyHz;
            return this;
        }

        public Builder iterationFailed(int iterationFailed) {
            this.iterationFailed = iterationFailed;
            return this;
        }

        public Builder failureCause(String failureCause) {
            this.failureCause = failureCause;
            return this;
        }

        public Builder recoverable(boolean recoverable) {
            this.recoverable = recoverable;
            return this;
        }

        public AssetImpact build() {
            return new AssetImpact(this);
        }
    }
}