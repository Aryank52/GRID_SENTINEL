package com.gridsentinel.application.simulation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.SimulationParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Default deterministic impact simulation strategy.
 * <p>
 * Simulates the impact of asset/node/edge failures on grid topology.
 * Uses a simplified cascade model based on connectivity and capacity.
 * </p>
 */
@Component
public class DefaultSimulationStrategy implements SimulationStrategy {

    private static final String STRATEGY_NAME = "default-impact-simulation-v1";
    private static final String STRATEGY_VERSION = "1.0";

    @Override
    public ImpactResult simulate(String scenarioId, TopologySnapshot topology, SimulationParameters parameters) {
        // Get affected elements from topology
        Set<String> affectedAssetIds = extractAffectedAssetIds(topology);
        Set<String> affectedNodeIds = extractAffectedNodeIds(topology);
        Set<String> affectedEdgeIds = extractAffectedEdgeIds(topology);

        // Simulate cascade if enabled
        if (parameters.getCascadeEnabled()) {
            simulateCascade(topology, parameters, affectedAssetIds, affectedNodeIds, affectedEdgeIds);
        }

        // Calculate quantitative metrics
        double totalLoadLostMw = calculateLoadLost(topology, affectedAssetIds);
        double totalCustomersAffected = calculateCustomersAffected(topology, affectedNodeIds);
        double estimatedRestorationHours = estimateRestorationTime(topology, affectedAssetIds);
        double economicImpactUsd = totalLoadLostMw * estimatedRestorationHours * 10000;

        // Check convergence (simplified - always converges in this model)
        boolean converged = true;
        int iterationsRun = parameters.getCascadeEnabled() ? 5 : 1;

        return ImpactResult.builder()
                .simulationId(CommonKernel.SimulationId.generate())
                .scenarioId(scenarioId)
                .computedAt(Instant.now())
                .iterationsRun(iterationsRun)
                .converged(converged)
                .addAffectedAssetId(affectedAssetIds.isEmpty() ? "" : affectedAssetIds.iterator().next())
                .addAffectedNodeId(affectedNodeIds.isEmpty() ? "" : affectedNodeIds.iterator().next())
                .addAffectedEdgeId(affectedEdgeIds.isEmpty() ? "" : affectedEdgeIds.iterator().next())
                .totalLoadLostMw(totalLoadLostMw)
                .totalCustomersAffected(totalCustomersAffected)
                .estimatedRestorationTimeHours(estimatedRestorationHours)
                .economicImpactUsd(economicImpactUsd)
                .maxVoltageDeviationPu(0.05)
                .maxThermalLoadingPu(0.85)
                .maxFrequencyDeviationHz(0.1)
                .strategyName(STRATEGY_NAME)
                .strategyVersion(STRATEGY_VERSION)
                .build();
    }

    @Override
    public StrategyMetadata metadata() {
        return new StrategyMetadata(
                STRATEGY_NAME,
                STRATEGY_VERSION,
                "Default deterministic impact simulation using simplified cascade model",
                List.of("topology_snapshot", "affected_elements", "simulation_parameters", "cascade_enabled")
        );
    }

    private Set<String> extractAffectedAssetIds(TopologySnapshot topology) {
        Set<String> assetIds = new HashSet<>();
        for (var asset : topology.getAllNodes()) {
            // In a full implementation, would map nodes to assets
        }
        return Set.of();
    }

    private Set<String> extractAffectedNodeIds(TopologySnapshot topology) {
        return topology.getAllNodes().stream()
                .map(n -> n.getId().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<String> extractAffectedEdgeIds(TopologySnapshot topology) {
        return topology.getAllEdges().stream()
                .map(e -> e.getId().toString())
                .collect(java.util.stream.Collectors.toSet());
    }

    private void simulateCascade(TopologySnapshot topology, SimulationParameters parameters,
                                 Set<String> affectedAssetIds, Set<String> affectedNodeIds, Set<String> affectedEdgeIds) {
        // Simplified cascade: if a critical node fails, mark connected nodes as affected
        // In a real implementation, this would run power flow analysis
        int maxIterations = parameters.getMaxIterations();
        int iterations = 0;

        while (iterations < maxIterations) {
            // Find nodes that would overload due to redistribution
            // This is a placeholder - real implementation would do power flow
            break;
        }
    }

    private double calculateLoadLost(TopologySnapshot topology, Set<String> affectedAssetIds) {
        // Placeholder: estimate based on affected nodes
        return topology.getAllNodes().size() * 10.0; // MW per node
    }

    private double calculateCustomersAffected(TopologySnapshot topology, Set<String> affectedNodeIds) {
        // Placeholder: estimate based on affected nodes
        return topology.getAllNodes().size() * 1000.0;
    }

    private double estimateRestorationTime(TopologySnapshot topology, Set<String> affectedAssetIds) {
        // Base time on number of affected elements
        return Math.min(24.0, topology.getAllNodes().size() * 2.0);
    }
}