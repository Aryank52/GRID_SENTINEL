package com.gridsentinel.application.risk;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.TopologySnapshot;

/**
 * Application-layer risk scoring context.
 */
public record RiskScoringContext(
        TopologySnapshot topology,
        EnvironmentalContext environment,
        HistoricalContext history,
        CommonKernel.CorrelationId correlationId
) {}