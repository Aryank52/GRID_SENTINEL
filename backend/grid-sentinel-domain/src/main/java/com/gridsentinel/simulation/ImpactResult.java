package com.gridsentinel.simulation;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "simulation_id", nullable = false, columnDefinition = "uuid"))
    })
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
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedAssetIds = new HashSet<>();

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedNodeIds = new HashSet<>();

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<String> affectedEdgeIds = new HashSet<>();

    @Transient
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
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> alternatePaths = new HashMap<>();

    @Setter(AccessLevel.PRIVATE)
    private Integer islandsFormed = 0;

    @Setter(AccessLevel.PRIVATE)
    private Double largestIslandLoadMw = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private String strategyName;

    @Setter(AccessLevel.PRIVATE)
    private String strategyVersion;

    public ImpactResult(String resultId, CommonKernel.SimulationId simulationId, String scenarioId,
                        Instant computedAt, Integer iterationsRun, Boolean converged,
                        Set<String> affectedAssetIds, Set<String> affectedNodeIds,
                        Set<String> affectedEdgeIds, Set<String> deenergizedLoadIds,
                        Double totalLoadLostMw, Double totalCustomersAffected,
                        Double estimatedRestorationTimeHours, Double economicImpactUsd,
                        Double maxVoltageDeviationPu, Double maxThermalLoadingPu,
                        Double maxFrequencyDeviationHz, Map<String, String> alternatePaths,
                        Integer islandsFormed, Double largestIslandLoadMw,
                        String strategyName, String strategyVersion) {
        this.resultId = resultId;
        this.simulationId = Objects.requireNonNull(simulationId, "Simulation ID cannot be null");
        this.scenarioId = Objects.requireNonNull(scenarioId, "Scenario ID cannot be null");
        this.computedAt = computedAt != null ? computedAt : Instant.now();
        this.iterationsRun = Objects.requireNonNull(iterationsRun, "Iterations run cannot be null");
        this.converged = Objects.requireNonNull(converged, "Converged flag cannot be null");
        this.affectedAssetIds = Set.copyOf(affectedAssetIds);
        this.affectedNodeIds = Set.copyOf(affectedNodeIds);
        this.affectedEdgeIds = Set.copyOf(affectedEdgeIds);
        this.deenergizedLoadIds = Set.copyOf(deenergizedLoadIds);
        this.totalLoadLostMw = totalLoadLostMw;
        this.totalCustomersAffected = totalCustomersAffected;
        this.estimatedRestorationTimeHours = estimatedRestorationTimeHours;
        this.economicImpactUsd = economicImpactUsd;
        this.maxVoltageDeviationPu = maxVoltageDeviationPu;
        this.maxThermalLoadingPu = maxThermalLoadingPu;
        this.maxFrequencyDeviationHz = maxFrequencyDeviationHz;
        this.alternatePaths = Map.copyOf(alternatePaths);
        this.islandsFormed = islandsFormed;
        this.largestIslandLoadMw = largestIslandLoadMw;
        this.strategyName = strategyName;
        this.strategyVersion = strategyVersion;
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

    public static ImpactResultBuilder builder() {
        return new ImpactResultBuilder();
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

    public static final class ImpactResultBuilder {
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
        private String strategyName;
        private String strategyVersion;

        private ImpactResultBuilder() {}

        public ImpactResultBuilder simulationId(CommonKernel.SimulationId simulationId) {
            this.simulationId = Objects.requireNonNull(simulationId);
            return this;
        }

        public ImpactResultBuilder scenarioId(String scenarioId) {
            this.scenarioId = Objects.requireNonNull(scenarioId);
            return this;
        }

        public ImpactResultBuilder computedAt(Instant computedAt) {
            this.computedAt = computedAt;
            return this;
        }

        public ImpactResultBuilder iterationsRun(int iterationsRun) {
            this.iterationsRun = iterationsRun;
            return this;
        }

        public ImpactResultBuilder converged(Boolean converged) {
            this.converged = converged;
            return this;
        }

        public ImpactResultBuilder addAffectedAssetId(String assetId) {
            this.affectedAssetIds.add(Objects.requireNonNull(assetId));
            return this;
        }

        public ImpactResultBuilder addAffectedNodeId(String nodeId) {
            this.affectedNodeIds.add(Objects.requireNonNull(nodeId));
            return this;
        }

        public ImpactResultBuilder addAffectedEdgeId(String edgeId) {
            this.affectedEdgeIds.add(Objects.requireNonNull(edgeId));
            return this;
        }

        public ImpactResultBuilder addDeenergizedLoadId(String loadId) {
            this.deenergizedLoadIds.add(Objects.requireNonNull(loadId));
            return this;
        }

        public ImpactResultBuilder totalLoadLostMw(double totalLoadLostMw) {
            this.totalLoadLostMw = totalLoadLostMw;
            return this;
        }

        public ImpactResultBuilder totalCustomersAffected(double totalCustomersAffected) {
            this.totalCustomersAffected = totalCustomersAffected;
            return this;
        }

        public ImpactResultBuilder estimatedRestorationTimeHours(double estimatedRestorationTimeHours) {
            this.estimatedRestorationTimeHours = estimatedRestorationTimeHours;
            return this;
        }

        public ImpactResultBuilder economicImpactUsd(double economicImpactUsd) {
            this.economicImpactUsd = economicImpactUsd;
            return this;
        }

        public ImpactResultBuilder maxVoltageDeviationPu(double maxVoltageDeviationPu) {
            this.maxVoltageDeviationPu = maxVoltageDeviationPu;
            return this;
        }

        public ImpactResultBuilder maxThermalLoadingPu(double maxThermalLoadingPu) {
            this.maxThermalLoadingPu = maxThermalLoadingPu;
            return this;
        }

        public ImpactResultBuilder maxFrequencyDeviationHz(double maxFrequencyDeviationHz) {
            this.maxFrequencyDeviationHz = maxFrequencyDeviationHz;
            return this;
        }

        public ImpactResultBuilder addAlternatePath(String nodeId, String path) {
            this.alternatePaths.put(Objects.requireNonNull(nodeId), Objects.requireNonNull(path));
            return this;
        }

        public ImpactResultBuilder islandsFormed(int islandsFormed) {
            this.islandsFormed = islandsFormed;
            return this;
        }

        public ImpactResultBuilder largestIslandLoadMw(double largestIslandLoadMw) {
            this.largestIslandLoadMw = largestIslandLoadMw;
            return this;
        }

        public ImpactResultBuilder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public ImpactResultBuilder strategyVersion(String strategyVersion) {
            this.strategyVersion = strategyVersion;
            return this;
        }

        public ImpactResult build() {
            return new ImpactResult(
                    simulationId.getValue().toString(),
                    simulationId, scenarioId, computedAt, iterationsRun, converged,
                    affectedAssetIds, affectedNodeIds, affectedEdgeIds, deenergizedLoadIds,
                    totalLoadLostMw, totalCustomersAffected, estimatedRestorationTimeHours,
                    economicImpactUsd, maxVoltageDeviationPu, maxThermalLoadingPu,
                    maxFrequencyDeviationHz, alternatePaths, islandsFormed, largestIslandLoadMw,
                    strategyName, strategyVersion
            );
        }
    }
}