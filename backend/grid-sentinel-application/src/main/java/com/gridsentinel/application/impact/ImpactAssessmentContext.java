package com.gridsentinel.application.impact;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.TopologySnapshot;

import java.util.Set;

/**
 * Context for impact assessment.
 */
public record ImpactAssessmentContext(
        CommonKernel.IncidentId incidentId,
        CommonKernel.ThreatEventId originatingThreatId,
        com.gridsentinel.common.ThreatSeverity incidentSeverity,
        Set<Asset> affectedAssets,
        Set<GridNode> affectedNodes,
        Set<GridEdge> affectedEdges,
        com.gridsentinel.grid.TopologySnapshot topology,
        ImpactAssessmentResult.GeographicExtent geographicExtent,
        ImpactAssessmentResult.CriticalInfrastructureExposure criticalExposure,
        CommonKernel.CorrelationId correlationId
) {}