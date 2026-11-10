package com.gridsentinel.application.impact;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.simulation.ImpactResult;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for assessing the impact of an incident on the grid.
 * <p>
 * Uses the Digital Grid Twin (topology, assets, nodes, edges) to determine
 * the full scope of impact including affected assets, nodes, edges, geographic
 * extent, and critical infrastructure exposure.
 * </p>
 */
@Service
@Transactional
public class AssessIncidentImpactUseCase {

    private final IncidentRepository incidentRepository;
    private final AssetRepository assetRepository;
    private final GridNodeRepository gridNodeRepository;
    private final GridEdgeRepository gridEdgeRepository;
    private final ImpactResultRepository impactResultRepository;
    private final ImpactAssessmentStrategy impactAssessmentStrategy;
    private final EventPublisher eventPublisher;

    public AssessIncidentImpactUseCase(
            IncidentRepository incidentRepository,
            AssetRepository assetRepository,
            GridNodeRepository gridNodeRepository,
            GridEdgeRepository gridEdgeRepository,
            ImpactResultRepository impactResultRepository,
            ImpactAssessmentStrategy impactAssessmentStrategy,
            EventPublisher eventPublisher
    ) {
        this.incidentRepository = incidentRepository;
        this.assetRepository = assetRepository;
        this.gridNodeRepository = gridNodeRepository;
        this.gridEdgeRepository = gridEdgeRepository;
        this.impactResultRepository = impactResultRepository;
        this.impactAssessmentStrategy = impactAssessmentStrategy;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Assess the impact of an incident.
     *
     * @param incidentId the incident to assess
     * @return the impact assessment result
     */
    public ImpactAssessmentResult assessImpact(CommonKernel.IncidentId incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Build the impact assessment context
        ImpactAssessmentContext context = buildContext(incident);

        // Execute the assessment
        ImpactAssessmentResult result = impactAssessmentStrategy.assess(context);

        // Persist the impact result
        ImpactResult impactResult = buildImpactResult(incident, result);
        impactResultRepository.save(impactResult);

        // Publish event
        eventPublisher.publishImpactAssessed(incident, impactResult);

        return result;
    }

    /**
     * Build the assessment context from the incident.
     */
    private ImpactAssessmentContext buildContext(Incident incident) {
        // Get primary affected asset
        Asset primaryAsset = incident.getPrimaryAssetId() != null
                ? assetRepository.findById(incident.getPrimaryAssetId()).orElse(null)
                : null;

        // Collect all affected assets
        Set<Asset> affectedAssets = new HashSet<>();
        if (primaryAsset != null) {
            affectedAssets.add(primaryAsset);
        }

        // Get affected grid nodes
        Set<GridNode> affectedNodes = new HashSet<>();
        if (primaryAsset != null && primaryAsset.getGridNodeId() != null) {
            GridNode primaryNode = gridNodeRepository.findById(primaryAsset.getGridNodeId()).orElse(null);
            if (primaryNode != null) {
                affectedNodes.add(primaryNode);
                // Add connected nodes from topology
                affectedNodes.addAll(getConnectedNodes(primaryNode.getId()));
            }
        }

        // Get affected grid edges
        Set<GridEdge> affectedEdges = new HashSet<>();
        for (GridNode node : affectedNodes) {
            affectedEdges.addAll(getConnectedEdges(node.getId()));
        }

        // Get topology snapshot
        TopologySnapshot topology = buildTopologySnapshot(affectedNodes, affectedEdges);

        // Calculate geographic extent
        ImpactAssessmentResult.GeographicExtent geographicExtent = calculateGeographicExtent(affectedAssets, affectedNodes);

        // Identify critical infrastructure exposure
        ImpactAssessmentResult.CriticalInfrastructureExposure criticalExposure = identifyCriticalExposure(affectedAssets);

        return new ImpactAssessmentContext(
                incident.getId(),
                incident.getOriginatingThreatId(),
                incident.getSeverity(),
                affectedAssets,
                affectedNodes,
                affectedEdges,
                topology,
                geographicExtent,
                criticalExposure,
                incident.getCorrelationId()
        );
    }

    private Set<GridNode> getConnectedNodes(CommonKernel.GridNodeId nodeId) {
        // In a full implementation, this would use topology snapshot
        // For now, query directly
        return gridEdgeRepository.findByNodeId(nodeId).stream()
                .flatMap(edge -> List.of(
                        gridNodeRepository.findById(edge.getFromNodeId()).orElse(null),
                        gridNodeRepository.findById(edge.getToNodeId()).orElse(null)
                ).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<GridEdge> getConnectedEdges(CommonKernel.GridNodeId nodeId) {
        return new HashSet<>(gridEdgeRepository.findByNodeId(nodeId));
    }

    private TopologySnapshot buildTopologySnapshot(Set<GridNode> nodes, Set<GridEdge> edges) {
        return TopologySnapshot.builder()
                .nodes(nodes)
                .edges(edges)
                .build();
    }

    private ImpactAssessmentResult.GeographicExtent calculateGeographicExtent(Set<Asset> assets, Set<GridNode> nodes) {
        // Calculate bounding box from all geometries
        double minLon = Double.MAX_VALUE, minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE, maxLat = Double.MIN_VALUE;

        for (Asset asset : assets) {
            if (asset.getGeometry() != null) {
                var coords = getCoordinates(asset.getGeometry());
                if (coords != null) {
                    for (Coordinate c : coords) {
                        minLon = Math.min(minLon, c.x);
                        minLat = Math.min(minLat, c.y);
                        maxLon = Math.max(maxLon, c.x);
                        maxLat = Math.max(maxLat, c.y);
                    }
                }
            }
        }

        for (GridNode node : nodes) {
            if (node.getGeometry() != null) {
                var coords = getCoordinates(node.getGeometry());
                if (coords != null) {
                    for (Coordinate c : coords) {
                        minLon = Math.min(minLon, c.x);
                        minLat = Math.min(minLat, c.y);
                        maxLon = Math.max(maxLon, c.x);
                        maxLat = Math.max(maxLat, c.y);
                    }
                }
            }
        }

        if (minLon == Double.MAX_VALUE) {
            return new ImpactAssessmentResult.GeographicExtent(null, null, null, null, "POINT");
        }

        return new ImpactAssessmentResult.GeographicExtent(minLon, minLat, maxLon, maxLat, "POLYGON");
    }

    private Coordinate[] getCoordinates(CommonKernel.GeometryValueObject geometry) {
        if (geometry == null) return null;
        var geom = geometry.geometry();
        if (geom == null) return null;
        return geom.getCoordinates();
    }

    private ImpactAssessmentResult.CriticalInfrastructureExposure identifyCriticalExposure(Set<Asset> assets) {
        long criticalCount = assets.stream()
                .filter(a -> a.getCriticality() == CommonKernel.Criticality.MISSION_CRITICAL)
                .count();
        long highCount = assets.stream()
                .filter(a -> a.getCriticality() == CommonKernel.Criticality.HIGH)
                .count();
        long mediumCount = assets.stream()
                .filter(a -> a.getCriticality() == CommonKernel.Criticality.MEDIUM)
                .count();
        long lowCount = assets.stream()
                .filter(a -> a.getCriticality() == CommonKernel.Criticality.LOW)
                .count();

        // Group by asset type
        Map<String, Long> typeCounts = assets.stream()
                .collect(Collectors.groupingBy(a -> a.getType().name(), Collectors.counting()));

        return new ImpactAssessmentResult.CriticalInfrastructureExposure(
                criticalCount, highCount, mediumCount, lowCount,
                typeCounts
        );
    }

    private ImpactResult buildImpactResult(Incident incident, ImpactAssessmentResult assessment) {
        // Build the domain ImpactResult from the assessment
        Set<String> affectedAssetIds = assessment.affectedAssets().stream()
                .map(a -> a.getId().toString())
                .collect(Collectors.toSet());

        Set<String> affectedNodeIds = assessment.affectedNodes().stream()
                .map(n -> n.getId().toString())
                .collect(Collectors.toSet());

        Set<String> affectedEdgeIds = assessment.affectedEdges().stream()
                .map(e -> e.getId().toString())
                .collect(Collectors.toSet());

        // Estimate load lost based on affected load assets
        double totalLoadLostMw = assessment.affectedAssets().stream()
                .filter(a -> a.getType().name().contains("LOAD") || a.getType().name().contains("CUSTOMER"))
                .mapToDouble(a -> a.getType().name().contains("INDUSTRIAL") ? 10.0 : 1.0)
                .sum();

        // Estimate customers affected
        double totalCustomersAffected = assessment.affectedAssets().stream()
                .filter(a -> a.getType().name().contains("LOAD"))
                .mapToDouble(a -> a.getType().name().contains("INDUSTRIAL") ? 1000 : 100)
                .sum();

        // Estimate restoration time based on severity
        double estimatedRestorationHours = switch (incident.getSeverity()) {
            case CRITICAL -> 24.0;
            case HIGH -> 12.0;
            case MEDIUM -> 6.0;
            case LOW -> 2.0;
            default -> 1.0;
        };

        // Economic impact estimate
        double economicImpactUsd = totalLoadLostMw * 10000 * estimatedRestorationHours; // $10k/MW-hour

        return ImpactResult.builder()
                .simulationId(CommonKernel.SimulationId.generate())
                .scenarioId("INCIDENT-" + incident.getIncidentNumber())
                .computedAt(Instant.now())
                .iterationsRun(1)
                .converged(true)
                .addAffectedAssetId(assessment.affectedAssets().stream().findFirst().map(a -> a.getId().toString()).orElse(""))
                .addAffectedNodeId(assessment.affectedNodes().stream().findFirst().map(n -> n.getId().toString()).orElse(""))
                .addAffectedEdgeId(assessment.affectedEdges().stream().findFirst().map(e -> e.getId().toString()).orElse(""))
                .totalLoadLostMw(totalLoadLostMw)
                .totalCustomersAffected(totalCustomersAffected)
                .estimatedRestorationTimeHours(estimatedRestorationHours)
                .economicImpactUsd(economicImpactUsd)
                .strategyName("incident-impact-assessment-v1")
                .strategyVersion("1.0")
                .build();
    }

    // Port interfaces
    public interface IncidentRepository {
        Optional<Incident> findById(CommonKernel.IncidentId id);
    }

    public interface AssetRepository {
        Optional<Asset> findById(CommonKernel.AssetId id);
        List<Asset> findByGridNodeId(CommonKernel.GridNodeId nodeId);
    }

    public interface GridNodeRepository {
        Optional<GridNode> findById(CommonKernel.GridNodeId id);
        List<GridNode> findByIds(Set<CommonKernel.GridNodeId> ids);
    }

    public interface GridEdgeRepository {
        Optional<GridEdge> findById(CommonKernel.GridEdgeId id);
        List<GridEdge> findByNodeId(CommonKernel.GridNodeId nodeId);
        List<GridEdge> findByIds(Set<CommonKernel.GridEdgeId> ids);
    }

    public interface ImpactResultRepository {
        ImpactResult save(ImpactResult impactResult);
        Optional<ImpactResult> findBySimulationId(CommonKernel.SimulationId simulationId);
        Optional<ImpactResult> findByScenarioId(String scenarioId);
    }

    public interface EventPublisher {
        void publishImpactAssessed(Incident incident, ImpactResult impactResult);
    }
}