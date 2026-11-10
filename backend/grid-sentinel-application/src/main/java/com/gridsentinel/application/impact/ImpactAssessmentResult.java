package com.gridsentinel.application.impact;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.TopologySnapshot;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Result of an impact assessment.
 */
public record ImpactAssessmentResult(
        CommonKernel.IncidentId incidentId,
        double overallImpactScore,
        ImpactSeverity severity,
        Set<Asset> affectedAssets,
        Set<GridNode> affectedNodes,
        Set<GridEdge> affectedEdges,
        TopologySnapshot topology,
        GeographicExtent geographicExtent,
        CriticalInfrastructureExposure criticalExposure,
        List<AffectedElement> affectedElements,
        double totalLoadLostMw,
        double totalCustomersAffected,
        double estimatedRestorationHours,
        double economicImpactUsd,
        CommonKernel.CorrelationId correlationId,
        Instant assessedAt
) {
    public enum ImpactSeverity {
        NEGLIGIBLE, MINOR, MODERATE, SEVERE, CATASTROPHIC
    }

    public record AffectedElement(
            String elementType,
            String elementId,
            String name,
            String type,
            String criticality,
            double health,
            String reason
    ) {}

    public record GeographicExtent(
            Double minLon, Double minLat, Double maxLon, Double maxLat,
            String geometryType
    ) {}

    public record CriticalInfrastructureExposure(
            long missionCriticalCount,
            long highCount,
            long mediumCount,
            long lowCount,
            java.util.Map<String, Long> assetTypeCounts
    ) {}
}