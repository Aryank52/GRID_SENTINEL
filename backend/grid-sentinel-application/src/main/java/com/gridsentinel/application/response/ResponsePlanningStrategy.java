package com.gridsentinel.application.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.risk.ProtectionAction;

import java.util.List;

/**
 * Application-layer response planning strategy interface.
 * Generates prioritized response plans from incidents and simulation results.
 */
public interface ResponsePlanningStrategy {

    /**
     * Generate a response plan for the given incident and context.
     */
    ResponsePlan plan(Incident incident, TopologySnapshot topology, PlanningContext context);

    /**
     * Get strategy metadata for explainability.
     */
    StrategyMetadata metadata();

    /**
     * Context for response planning.
     */
    record PlanningContext(
            List<ProtectionAction> availableActions,
            ResourceConstraints constraints,
            CommonKernel.CorrelationId correlationId
    ) {}

    record ResourceConstraints(
            int maxConcurrentTeams,
            double budgetLimit,
            CommonKernel.TimeRange timeRange
    ) {}

    record StrategyMetadata(
            String strategyName,
            String version,
            String description,
            List<String> requiredInputs
    ) {}
}