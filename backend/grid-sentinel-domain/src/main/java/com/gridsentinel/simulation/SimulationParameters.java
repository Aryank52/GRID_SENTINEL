package com.gridsentinel.simulation;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Parameters controlling simulation behavior.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimulationParameters {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Boolean cascadeEnabled = true;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double loadRedistributionFactor = 1.0; // 1.0 = full redistribution

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Integer maxIterations = 100;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Long randomSeed = 0L; // 0 = use system random

    @Setter(AccessLevel.PRIVATE)
    private Double voltageCollapseThreshold = 0.85; // p.u.

    @Setter(AccessLevel.PRIVATE)
    private Double thermalOverloadThreshold = 1.0; // 1.0 = 100% rating

    @Setter(AccessLevel.PRIVATE)
    private Double frequencyDeviationThreshold = 0.5; // Hz

    // @ElementCollection - Temporarily disabled
    // @Setter(AccessLevel.PRIVATE)
    // private Set<String> excludedAssetIds = new HashSet<>();
    
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<String> excludedAssetIds = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    private Boolean includeCyberEffects = false;

    @Setter(AccessLevel.PRIVATE)
    private Boolean includeCommunicationDelays = false;

    private SimulationParameters(Builder builder) {
        this.cascadeEnabled = builder.cascadeEnabled;
        this.loadRedistributionFactor = builder.loadRedistributionFactor;
        this.maxIterations = builder.maxIterations;
        this.randomSeed = builder.randomSeed;
        this.voltageCollapseThreshold = builder.voltageCollapseThreshold;
        this.thermalOverloadThreshold = builder.thermalOverloadThreshold;
        this.frequencyDeviationThreshold = builder.frequencyDeviationThreshold;
        this.excludedAssetIds = Set.copyOf(builder.excludedAssetIds);
        this.includeCyberEffects = builder.includeCyberEffects;
        this.includeCommunicationDelays = builder.includeCommunicationDelays;

        validate();
    }

    private void validate() {
        if (loadRedistributionFactor < 0.0 || loadRedistributionFactor > 2.0) {
            throw new IllegalArgumentException("Load redistribution factor must be in [0.0, 2.0]");
        }
        if (maxIterations < 1 || maxIterations > 10000) {
            throw new IllegalArgumentException("Max iterations must be in [1, 10000]");
        }
        if (voltageCollapseThreshold <= 0.0 || voltageCollapseThreshold > 1.0) {
            throw new IllegalArgumentException("Voltage collapse threshold must be in (0.0, 1.0]");
        }
        if (thermalOverloadThreshold <= 0.0 || thermalOverloadThreshold > 2.0) {
            throw new IllegalArgumentException("Thermal overload threshold must be in (0.0, 2.0]");
        }
    }

    public Set<String> getExcludedAssetIds() {
        return Collections.unmodifiableSet(excludedAssetIds);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Boolean cascadeEnabled = true;
        private Double loadRedistributionFactor = 1.0;
        private Integer maxIterations = 100;
        private Long randomSeed = 0L;
        private Double voltageCollapseThreshold = 0.85;
        private Double thermalOverloadThreshold = 1.0;
        private Double frequencyDeviationThreshold = 0.5;
        private Set<String> excludedAssetIds = new HashSet<>();
        private Boolean includeCyberEffects = false;
        private Boolean includeCommunicationDelays = false;

        private Builder() {}

        public Builder cascadeEnabled(boolean cascadeEnabled) {
            this.cascadeEnabled = cascadeEnabled;
            return this;
        }

        public Builder loadRedistributionFactor(double loadRedistributionFactor) {
            this.loadRedistributionFactor = loadRedistributionFactor;
            return this;
        }

        public Builder maxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
            return this;
        }

        public Builder randomSeed(long randomSeed) {
            this.randomSeed = randomSeed;
            return this;
        }

        public Builder voltageCollapseThreshold(double voltageCollapseThreshold) {
            this.voltageCollapseThreshold = voltageCollapseThreshold;
            return this;
        }

        public Builder thermalOverloadThreshold(double thermalOverloadThreshold) {
            this.thermalOverloadThreshold = thermalOverloadThreshold;
            return this;
        }

        public Builder frequencyDeviationThreshold(double frequencyDeviationThreshold) {
            this.frequencyDeviationThreshold = frequencyDeviationThreshold;
            return this;
        }

        public Builder addExcludedAssetId(String assetId) {
            this.excludedAssetIds.add(Objects.requireNonNull(assetId));
            return this;
        }

        public Builder includeCyberEffects(boolean includeCyberEffects) {
            this.includeCyberEffects = includeCyberEffects;
            return this;
        }

        public Builder includeCommunicationDelays(boolean includeCommunicationDelays) {
            this.includeCommunicationDelays = includeCommunicationDelays;
            return this;
        }

        public SimulationParameters build() {
            return new SimulationParameters(this);
        }
    }
}