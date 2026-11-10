package com.gridsentinel.application.impact;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.NodeType;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default deterministic impact assessment strategy.
 * <p>
 * Computes impact based on:
 * - Number and criticality of affected assets
 * - Topology disruption (nodes/edges affected)
 * - Geographic extent
 * - Incident severity
 * - Threat severity
 * </p>
 */
@Component
public class DefaultImpactAssessmentStrategy implements ImpactAssessmentStrategy {

    private static final String STRATEGY_NAME = "default-impact-assessment-v1";
    private static final String STRATEGY_VERSION = "1.0";

    // Impact severity thresholds
    private static final int CRITICAL_ASSET_THRESHOLD = 1;
    private static final int HIGH_ASSET_THRESHOLD = 3;
    private static final int MEDIUM_ASSET_THRESHOLD = 5;
    private static final int CRITICAL_NODE_THRESHOLD = 2;
    private static final int HIGH_NODE_THRESHOLD = 5;

    @Override
    public ImpactAssessmentResult assess(ImpactAssessmentContext context) {
        // Calculate impact scores for different dimensions
        double assetImpact = calculateAssetImpact(context);
        double topologyImpact = calculateTopologyImpact(context);
        double geographicImpact = calculateGeographicImpact(context);
        double criticalityImpact = calculateCriticalityImpact(context);
        double incidentImpact = calculateIncidentImpact(context);

        // Overall impact score (weighted combination)
        double overallImpact = (assetImpact * 0.35) +
                (topologyImpact * 0.25) +
                (geographicImpact * 0.15) +
                (criticalityImpact * 0.15) +
                (incidentImpact * 0.10);

        // Clamp to [0.0, 1.0]
        overallImpact = Math.max(0.0, Math.min(1.0, overallImpact));

        // Determine impact severity - use the result's enum
        ImpactAssessmentResult.ImpactSeverity severity = determineSeverity(overallImpact);

        // Build affected elements list
        List<ImpactAssessmentResult.AffectedElement> affectedElements = buildAffectedElements(context);

        // Calculate quantitative metrics
        double totalLoadLostMw = estimateLoadLost(context);
        double totalCustomersAffected = estimateCustomersAffected(context);
        double estimatedRestorationHours = estimateRestorationTime(context, overallImpact);
        double economicImpactUsd = estimateEconomicImpact(context, totalLoadLostMw, estimatedRestorationHours);

        return new ImpactAssessmentResult(
                context.incidentId(),
                overallImpact,
                severity,
                context.affectedAssets(),
                context.affectedNodes(),
                context.affectedEdges(),
                context.topology(),
                context.geographicExtent(),
                context.criticalExposure(),
                affectedElements,
                totalLoadLostMw,
                totalCustomersAffected,
                estimatedRestorationHours,
                economicImpactUsd,
                context.correlationId(),
                Instant.now()
        );
    }

    @Override
    public StrategyMetadata metadata() {
        return new StrategyMetadata(
                STRATEGY_NAME,
                STRATEGY_VERSION,
                "Default impact assessment using asset criticality, topology disruption, geographic extent, and incident severity",
                List.of("affected_assets", "affected_nodes", "affected_edges", "geographic_extent", "incident_severity", "threat_severity")
        );
    }

    private double calculateAssetImpact(ImpactAssessmentContext context) {
        if (context.affectedAssets().isEmpty()) return 0.0;

        // Weight by criticality
        double weightedSum = context.affectedAssets().stream()
                .mapToDouble(a -> switch (a.getCriticality()) {
                    case MISSION_CRITICAL -> 1.0;
                    case HIGH -> 0.75;
                    case MEDIUM -> 0.5;
                    case LOW -> 0.25;
                })
                .sum();

        double maxPossible = context.affectedAssets().size() * 1.0;
        return Math.min(1.0, weightedSum / maxPossible);
    }

    private double calculateTopologyImpact(ImpactAssessmentContext context) {
        int nodeCount = context.affectedNodes().size();
        int edgeCount = context.affectedEdges().size();

        // Check if critical nodes are affected (substations, generation)
        long criticalNodes = context.affectedNodes().stream()
                .filter(n -> n.getType() == NodeType.SUBSTATION ||
                        n.getType() == NodeType.GENERATION ||
                        n.getType() == NodeType.HVDC_CONVERTER)
                .count();

        // Check for cut edges (bridges in topology)
        int bridgeEdges = findBridgeEdges(context.topology()).size();

        double nodeImpact = Math.min(1.0, nodeCount / 10.0);
        double edgeImpact = Math.min(1.0, edgeCount / 20.0);
        double criticalNodeImpact = Math.min(1.0, criticalNodes * 0.3);
        double bridgeImpact = Math.min(1.0, bridgeEdges * 0.4);

        return Math.min(1.0, (nodeImpact + edgeImpact + criticalNodeImpact + bridgeImpact) / 2.0);
    }

    private double calculateGeographicImpact(ImpactAssessmentContext context) {
        ImpactAssessmentResult.GeographicExtent extent = context.geographicExtent();
        if (extent.minLon() == null) return 0.0;

        double area = (extent.maxLon() - extent.minLon()) * (extent.maxLat() - extent.minLat());
        // Normalize: assume 1 degree^2 is significant
        return Math.min(1.0, area / 1.0);
    }

    private double calculateCriticalityImpact(ImpactAssessmentContext context) {
        ImpactAssessmentResult.CriticalInfrastructureExposure exposure = context.criticalExposure();

        double weightedExposure = (exposure.missionCriticalCount() * 1.0) +
                (exposure.highCount() * 0.75) +
                (exposure.mediumCount() * 0.5) +
                (exposure.lowCount() * 0.25);

        long totalAssets = exposure.missionCriticalCount() + exposure.highCount() +
                exposure.mediumCount() + exposure.lowCount();

        if (totalAssets == 0) return 0.0;
        return Math.min(1.0, weightedExposure / totalAssets);
    }

    private double calculateIncidentImpact(ImpactAssessmentContext context) {
        double incidentSeverityScore = switch (context.incidentSeverity()) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
            case INFO -> 0.1;
        };

        // Also consider threat severity if available
        double threatSeverityScore = switch (context.incidentSeverity()) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
            case INFO -> 0.1;
        };

        return (incidentSeverityScore + threatSeverityScore) / 2.0;
    }

    private ImpactAssessmentResult.ImpactSeverity determineSeverity(double overallImpact) {
        if (overallImpact >= 0.8) return ImpactAssessmentResult.ImpactSeverity.CATASTROPHIC;
        if (overallImpact >= 0.6) return ImpactAssessmentResult.ImpactSeverity.SEVERE;
        if (overallImpact >= 0.4) return ImpactAssessmentResult.ImpactSeverity.MODERATE;
        if (overallImpact >= 0.2) return ImpactAssessmentResult.ImpactSeverity.MINOR;
        return ImpactAssessmentResult.ImpactSeverity.NEGLIGIBLE;
    }

    private List<ImpactAssessmentResult.AffectedElement> buildAffectedElements(ImpactAssessmentContext context) {
        List<ImpactAssessmentResult.AffectedElement> elements = new ArrayList<>();

        // Add assets
        for (Asset asset : context.affectedAssets()) {
            elements.add(new ImpactAssessmentResult.AffectedElement(
                    "ASSET",
                    asset.getId().toString(),
                    asset.getName(),
                    asset.getType().name(),
                    asset.getCriticality().name(),
                    asset.getHealthIndex().value(),
                    "Directly affected"
            ));
        }

        // Add nodes
        for (GridNode node : context.affectedNodes()) {
            elements.add(new ImpactAssessmentResult.AffectedElement(
                    "NODE",
                    node.getId().toString(),
                    node.getName(),
                    node.getType().name(),
                    "N/A",
                    1.0,
                    "Topologically affected"
            ));
        }

        // Add edges
        for (GridEdge edge : context.affectedEdges()) {
            elements.add(new ImpactAssessmentResult.AffectedElement(
                    "EDGE",
                    edge.getId().toString(),
                    edge.getName(),
                    edge.getType().name(),
                    "N/A",
                    1.0,
                    "Topologically affected"
            ));
        }

        return elements;
    }

    private double estimateLoadLost(ImpactAssessmentContext context) {
        return context.affectedAssets().stream()
                .filter(a -> a.getType() == com.gridsentinel.asset.AssetType.SUBSTATION ||
                        a.getType() == com.gridsentinel.asset.AssetType.DISTRIBUTION_TRANSFORMER ||
                        a.getType() == com.gridsentinel.asset.AssetType.GENERATOR)
                .mapToDouble(a -> {
                    // Estimate based on asset type
                    return switch (a.getType()) {
                        case SUBSTATION -> 50.0; // MW
                        case DISTRIBUTION_TRANSFORMER -> 1.0; // MW
                        case GENERATOR -> 20.0; // MW
                        case BATTERY_STORAGE -> 10.0; // MW
                        case HYDRO_PLANT -> 100.0; // MW
                        default -> 0.5;
                    };
                })
                .sum();
    }

    private double estimateCustomersAffected(ImpactAssessmentContext context) {
        return context.affectedAssets().stream()
                .filter(a -> a.getType() == com.gridsentinel.asset.AssetType.SUBSTATION ||
                        a.getType() == com.gridsentinel.asset.AssetType.DISTRIBUTION_TRANSFORMER)
                .mapToDouble(a -> {
                    return switch (a.getType()) {
                        case SUBSTATION -> 5000.0;
                        case DISTRIBUTION_TRANSFORMER -> 100.0;
                        default -> 10.0;
                    };
                })
                .sum();
    }

    private double estimateRestorationTime(ImpactAssessmentContext context, double overallImpact) {
        // Base time on impact severity and asset criticality
        double baseHours = switch (context.incidentSeverity()) {
            case CRITICAL -> 24.0;
            case HIGH -> 12.0;
            case MEDIUM -> 6.0;
            case LOW -> 2.0;
            default -> 1.0;
        };

        // Increase for critical assets
        if (context.criticalExposure().missionCriticalCount() > 0) {
            baseHours *= 2.0;
        } else if (context.criticalExposure().highCount() > 0) {
            baseHours *= 1.5;
        }

        return baseHours;
    }

    private double estimateEconomicImpact(ImpactAssessmentContext context, double loadLostMw, double restorationHours) {
        // $10,000 per MW-hour of load lost
        return loadLostMw * restorationHours * 10000;
    }

    private Set<GridEdge> findBridgeEdges(TopologySnapshot topology) {
        // Find edges that are bridges (whose removal disconnects the graph)
        // This is a simplified implementation - in reality would use graph algorithms
        Set<GridEdge> bridges = new HashSet<>();
        // For now, return empty - full implementation would use Tarjan's algorithm
        return bridges;
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
}