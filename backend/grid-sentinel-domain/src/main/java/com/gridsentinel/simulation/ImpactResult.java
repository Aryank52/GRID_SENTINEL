package com.gridsentinel.simulation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.GridEdge;
import jakarta.persistence.Embedded;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Deterministic result of an impact simulation.
 * Contains affected assets, services, alternate paths, and quantitative impact metrics.
 */
@Entity
@Table(name = "impact_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImpactResult {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private String resultId; // Same as simulation ID

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.SimulationId simulationId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String scenarioId;

    @Setter(AccessLevel.PRIVATE)
    private Instant computedAt;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Integer iterationsRun;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Boolean converged;

    // Affected elements
    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedAssetIds = new HashSet<>();

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedNodeIds = new HashSet<>();

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedEdgeIds = new HashSet<>();

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<String> deenergizedLoadIds = new HashSet<>();

    // Quantitative metrics
    @Setter(AccessLevel.PRIVATE)
    private Double totalLoadLostMw = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double totalCustomersAffected = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double estimatedRestorationTimeHours = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double economicImpactUsd = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double maxVoltageDeviationPu = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double maxThermalLoadingPu = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double maxFrequencyDeviationHz = 0.0;

    // Alternate paths and redundancy
    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> alternatePaths = new HashMap<>(); // nodeId -> path description

    @Setter(AccessLevel.PRIVATE)
    private Integer islandsFormed = 0;

    @Setter(AccessLevel.PRIVATE)
    private Double largestIslandLoadMw = 0.0;

    // Detailed per-asset impact
    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Map<String, AssetImpact> assetImpacts = new HashMap<>();

    // Cascading failure trace
    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Map<Integer, Set<String>> cascadeSteps = new HashMap<>(); // iteration -> failed asset IDs

    @Setter(AccessLevel.PRIVATE)
    private String strategyName;

    @Setter(AccessLevel.PRIVATE)
    private String strategyVersion;

    private ImpactResult(Builder builder) {
        this.resultId = builder.simulationId.getValue().toString();
        this.simulationId = Objects.requireNonNull(builder.simulationId, "Simulation ID cannot be null");
        this.scenarioId = Objects.requireNonNull(builder.scenarioId, "Scenario ID cannot be null");
        this.computedAt = builder.computedAt != null ? builder.computedAt : Instant.now();
        this.iterationsRun = Objects.requireNonNull(builder.iterationsRun, "Iterations run cannot be null");
        this.converged = Objects.requireNonNull(builder.converged, "Converged flag cannot be null");
        this.affectedAssetIds = Set.copyOf(builder.affectedAssetIds);
        this.affectedNodeIds = Set.copyOf(builder.affectedNodeIds);
        this.affectedEdgeIds = Set.copyOf(builder.affectedEdgeIds);
        this.deenergizedLoadIds = Set.copyOf(builder.deenergizedLoadIds);
        this.totalLoadLostMw = builder.totalLoadLostMw;
        this.totalCustomersAffected = builder.totalCustomersAffected;
        this.estimatedRestorationTimeHours = builder.estimatedRestorationTimeHours;
        this.economicImpactUsd = builder.economicImpactUsd;
        this.maxVoltageDeviationPu = builder.maxVoltageDeviationPu;
        this.maxThermalLoadingPu = builder.maxThermalLoadingPu;
        this.maxFrequencyDeviationHz = builder.maxFrequencyDeviationHz;
        this.alternatePaths = Map.copyOf(builder.alternatePaths);
        this.islandsFormed = builder.islandsFormed;
        this.largestIslandLoadMw = builder.largestIslandLoadMw;
        this.assetImpacts = Map.copyOf(builder.assetImpacts);
        this.cascadeSteps = Map.copyOf(builder.cascadeSteps);
        this.strategyName = builder.strategyName;
        this.strategyVersion = builder.strategyVersion;
    }

    public Set<String> getAffectedAssetIds() {
        return Collections.unmodifiableSet(affectedAssetIds);
    }

    public Set<String> getAffectedNodeIds() {
        return Collections.unmodifiableSet(affectedNodeIds);
    }

    public Set<String> getAffectedEdgeIds() {
        return Collections.unmodifiableSet(affectedEdgeIds);
    }

    public Set<String> getDeenergizedLoadIds() {
        return Collections.unmodifiableSet(deenergizedLoadIds);
    }

    public Map<String, String> getAlternatePaths() {
        return Collections.unmodifiableMap(alternatePaths);
    }

    public Map<String, AssetImpact> getAssetImpacts() {
        return Collections.unmodifiableMap(assetImpacts);
    }

    public Map<Integer, Set<String>> getCascadeSteps() {
        return Collections.unmodifiableMap(cascadeSteps);
    }

    /**
     * Get summary for quick dashboard display.
     */
    public ImpactSummary summary() {
        return new ImpactSummary(
                affectedAssetIds.size(),
                affectedNodeIds.size(),
                totalLoadLostMw,
                totalCustomersAffected,
                estimatedRestorationTimeHours,
                economicImpactUsd,
                islandsFormed,
                converged
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImpactResult)) return false;
        ImpactResult that = (ImpactResult) o;
        return resultId != null && resultId.equals(that.resultId);
    }

    @Override
    public int hashCode() {
        return resultId != null ? resultId.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public record ImpactSummary(
            int affectedAssetCount,
            int affectedNodeCount,
            double totalLoadLostMw,
            double totalCustomersAffected,
            double estimatedRestorationTimeHours,
            double economicImpactUsd,
            int islandsFormed,
            boolean converged
    ) {}

    public static final class Builder {
        private CommonKernel.SimulationId simulationId;
        private String scenarioId;
        private Instant computedAt;
        private Integer iterationsRun;
        private Boolean converged;
        private Set<String> affectedAssetIds = new HashSet<>();
        private Set<String> affectedNodeIds = new HashSet<>();
        private Set<String> affectedEdgeIds = new HashSet<>();
        private Set<String> deenergizedLoadIds = new HashSet<>();
        private Double totalLoadLostMw = 0.0;
        private Double totalCustomersAffected = 0.0;
        private Double estimatedRestorationTimeHours = 0.0;
        private Double economicImpactUsd = 0.0;
        private Double maxVoltageDeviationPu = 0.0;
        private Double maxThermalLoadingPu = 0.0;
        private Double maxFrequencyDeviationHz = 0.0;
        private Map<String, String> alternatePaths = new HashMap<>();
        private Integer islandsFormed = 0;
        private Double largestIslandLoadMw = 0.0;
        private Map<String, AssetImpact> assetImpacts = new HashMap<>();
        private Map<Integer, Set<String>> cascadeSteps = new HashMap<>();
        private String strategyName;
        private String strategyVersion;

        private Builder() {}

        public Builder simulationId(CommonKernel.SimulationId simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public Builder scenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
            return this;
        }

        public Builder computedAt(Instant computedAt) {
            this.computedAt = computedAt;
            return this;
        }

        public Builder iterationsRun(int iterationsRun) {
            this.iterationsRun = iterationsRun;
            return this;
        }

        public Builder converged(boolean converged) {
            this.converged = converged;
            return this;
        }

        public Builder addAffectedAssetId(String assetId) {
            this.affectedAssetIds.add(Objects.requireNonNull(assetId));
            return this;
        }

        public Builder addAffectedNodeId(String nodeId) {
            this.affectedNodeIds.add(Objects.requireNonNull(nodeId));
            return this;
        }

        public Builder addAffectedEdgeId(String edgeId) {
            this.affectedEdgeIds.add(Objects.requireNonNull(edgeId));
            return this;
        }

        public Builder addDeenergizedLoadId(String loadId) {
            this.deenergizedLoadIds.add(Objects.requireNonNull(loadId));
            return this;
        }

        public Builder totalLoadLostMw(double totalLoadLostMw) {
            this.totalLoadLostMw = totalLoadLostMw;
            return this;
        }

        public Builder totalCustomersAffected(double totalCustomersAffected) {
            this.totalCustomersAffected = totalCustomersAffected;
            return this;
        }

        public Builder estimatedRestorationTimeHours(double estimatedRestorationTimeHours) {
            this.estimatedRestorationTimeHours = estimatedRestorationTimeHours;
            return this;
        }

        public Builder economicImpactUsd(double economicImpactUsd) {
            this.economicImpactUsd = economicImpactUsd;
            return this;
        }

        public Builder maxVoltageDeviationPu(double maxVoltageDeviationPu) {
            this.maxVoltageDeviationPu = maxVoltageDeviationPu;
            return this;
        }

        public Builder maxThermalLoadingPu(double maxThermalLoadingPu) {
            this.maxThermalLoadingPu = maxThermalLoadingPu;
            return this;
        }

        public Builder maxFrequencyDeviationHz(double maxFrequencyDeviationHz) {
            this.maxFrequencyDeviationHz = maxFrequencyDeviationHz;
            return this;
        }

        public Builder alternatePath(String nodeId, String pathDescription) {
            this.alternatePaths.put(Objects.requireNonNull(nodeId), Objects.requireNonNull(pathDescription));
            return this;
        }

        public Builder islandsFormed(int islandsFormed) {
            this.islandsFormed = islandsFormed;
            return this;
        }

        public Builder largestIslandLoadMw(double largestIslandLoadMw) {
            this.largestIslandLoadMw = largestIslandLoadMw;
            return this;
        }

        public Builder assetImpact(String assetId, AssetImpact impact) {
            this.assetImpacts.put(Objects.requireNonNull(assetId), Objects.requireNonNull(impact));
            return this;
        }

        public Builder cascadeStep(int iteration, Set<String> failedAssetIds) {
            this.cascadeSteps.put(iteration, Set.copyOf(Objects.requireNonNull(failedAssetIds)));
            return this;
        }

        public Builder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public Builder strategyVersion(String strategyVersion) {
            this.strategyVersion = strategyVersion;
            return this;
        }

        public ImpactResult build() {
            return new ImpactResult(this);
        }
    }
}