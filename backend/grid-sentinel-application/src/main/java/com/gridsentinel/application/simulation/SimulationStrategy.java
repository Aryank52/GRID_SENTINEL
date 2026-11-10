package com.gridsentinel.application.simulation;

import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.SimulationParameters;
import com.gridsentinel.strategy.StrategyInterfaces;

/**
 * Application-layer simulation strategy interface.
 * Adapts the domain strategy interface for application use.
 */
public interface SimulationStrategy {

    /**
     * Execute a simulation for the given scenario.
     *
     * @param scenarioId the scenario identifier
     * @param topology the grid topology snapshot
     * @param parameters simulation parameters
     * @return the simulation result
     */
    ImpactResult simulate(String scenarioId, TopologySnapshot topology, SimulationParameters parameters);

    /**
     * Get strategy metadata.
     */
    StrategyMetadata metadata();

    record StrategyMetadata(
            String strategyName,
            String version,
            String description,
            java.util.List<String> requiredInputs
    ) {}
}