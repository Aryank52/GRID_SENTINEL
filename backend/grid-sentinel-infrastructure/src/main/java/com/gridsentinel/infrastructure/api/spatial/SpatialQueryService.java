package com.gridsentinel.infrastructure.api.spatial;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.asset.AssetType;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.infrastructure.persistence.repository.AssetRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridEdgeRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridNodeRepository;
import com.gridsentinel.infrastructure.persistence.repository.IncidentRepository;
import com.gridsentinel.infrastructure.persistence.repository.RiskScoreRepository;
import com.gridsentinel.infrastructure.persistence.repository.ThreatEventRepository;
import com.gridsentinel.risk.RiskScore;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for executing spatial queries and converting domain entities to GeoJSON.
 * Handles viewport-aware queries, filtering, and GeoJSON serialization.
 */
@Service
@Transactional(readOnly = true)
public class SpatialQueryService {

    private static final int MAX_FEATURES = 10000;

    private final AssetRepository assetRepository;
    private final GridNodeRepository gridNodeRepository;
    private final GridEdgeRepository gridEdgeRepository;
    private final ThreatEventRepository threatEventRepository;
    private final IncidentRepository incidentRepository;
    private final RiskScoreRepository riskScoreRepository;

    public SpatialQueryService(AssetRepository assetRepository,
                                GridNodeRepository gridNodeRepository,
                                GridEdgeRepository gridEdgeRepository,
                                ThreatEventRepository threatEventRepository,
                                IncidentRepository incidentRepository,
                                RiskScoreRepository riskScoreRepository) {
        this.assetRepository = assetRepository;
        this.gridNodeRepository = gridNodeRepository;
        this.gridEdgeRepository = gridEdgeRepository;
        this.threatEventRepository = threatEventRepository;
        this.incidentRepository = incidentRepository;
        this.riskScoreRepository = riskScoreRepository;
    }

    // =========================================================================
    // Grid Nodes
    // =========================================================================

    public GeoJsonFeatureCollection<GridNodeProperties> getGridNodes(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<GridNode> nodes = gridNodeRepository.findByBbox(envelope);

        // Apply filters
        if (params.getNodeType() != null) {
            nodes = nodes.stream()
                .filter(n -> n.getType().name().equalsIgnoreCase(params.getNodeType()))
                .toList();
        }
        if (params.getMinVoltageKv() != null || params.getMaxVoltageKv() != null) {
            double min = params.getMinVoltageKv() != null ? params.getMinVoltageKv() : 0;
            double max = params.getMaxVoltageKv() != null ? params.getMaxVoltageKv() : Double.MAX_VALUE;
            nodes = nodes.stream()
                .filter(n -> n.getVoltageLevelKv() >= min && n.getVoltageLevelKv() <= max)
                .toList();
        }

        // Limit results
        if (nodes.size() > params.getLimit()) {
            nodes = nodes.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<GridNodeProperties>> features = nodes.stream()
            .map(this::toGridNodeFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<GridNodeProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<GridNodeProperties> toGridNodeFeature(GridNode node) {
        Point point = (Point) node.getGeometry().getGeometry();
        GeoJsonGeometry.Point geometry = GeoJsonGeometry.point(point.getX(), point.getY());

        GridNodeProperties props = GridNodeProperties.builder()
            .id(node.getId().value())
            .name(node.getName())
            .type(node.getType().name())
            .voltageLevelKv(node.getVoltageLevelKv())
            .primaryAssetId(node.getPrimaryAssetId() != null ? node.getPrimaryAssetId().value() : null)
            .operator(node.getOperator())
            .degree(node.degree())
            .isSubstation(node.isSubstation())
            .isGeneration(node.isGeneration())
            .isLoad(node.isLoad())
            .build();

        return GeoJsonFeature.of(geometry, props, node.getId().value());
    }

    // =========================================================================
    // Grid Edges
    // =========================================================================

    public GeoJsonFeatureCollection<GridEdgeProperties> getGridEdges(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<GridEdge> edges = gridEdgeRepository.findByBbox(envelope);

        // Apply filters
        if (params.getEdgeType() != null) {
            edges = edges.stream()
                .filter(e -> e.getType().name().equalsIgnoreCase(params.getEdgeType()))
                .toList();
        }
        if (params.getMinVoltageKv() != null || params.getMaxVoltageKv() != null) {
            double min = params.getMinVoltageKv() != null ? params.getMinVoltageKv() : 0;
            double max = params.getMaxVoltageKv() != null ? params.getMaxVoltageKv() : Double.MAX_VALUE;
            edges = edges.stream()
                .filter(e -> e.getVoltageLevelKv() >= min && e.getVoltageLevelKv() <= max)
                .toList();
        }

        // Limit results
        if (edges.size() > params.getLimit()) {
            edges = edges.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<GridEdgeProperties>> features = edges.stream()
            .map(this::toGridEdgeFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<GridEdgeProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<GridEdgeProperties> toGridEdgeFeature(GridEdge edge) {
        LineString lineString = (LineString) edge.getGeometry().getGeometry();
        Coordinate[] coords = lineString.getCoordinates();
        List<List<Double>> coordinates = new ArrayList<>();
        for (Coordinate coord : coords) {
            coordinates.add(List.of(coord.x, coord.y));
        }
        GeoJsonGeometry.LineString geometry = GeoJsonGeometry.lineString(coordinates);

        GridEdgeProperties props = GridEdgeProperties.builder()
            .id(edge.getId().value())
            .name(edge.getName())
            .type(edge.getType().name())
            .fromNodeId(edge.getFromNodeId().value())
            .toNodeId(edge.getToNodeId().value())
            .voltageLevelKv(edge.getVoltageLevelKv())
            .thermalRatingAmps(edge.getThermalRatingAmps())
            .mvaRating(edge.getMvaRating())
            .lengthKm(edge.getLengthKm())
            .circuitCount(edge.getCircuitCount())
            .conductorType(edge.getConductorType())
            .yearInstalled(edge.getYearInstalled())
            .primaryAssetId(edge.getPrimaryAssetId() != null ? edge.getPrimaryAssetId().value() : null)
            .isTransformer(edge.isTransformer())
            .build();

        return GeoJsonFeature.of(geometry, props, edge.getId().value());
    }

    // =========================================================================
    // Assets
    // =========================================================================

    public GeoJsonFeatureCollection<AssetProperties> getAssets(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<Asset> assets = assetRepository.findByBbox(envelope);

        // Apply filters
        if (params.getAssetType() != null) {
            List<String> types = params.parseAssetTypes();
            assets = assets.stream()
                .filter(a -> types.contains(a.getType().name()))
                .toList();
        }
        if (params.getCriticality() != null) {
            assets = assets.stream()
                .filter(a -> a.getCriticality().name().equalsIgnoreCase(params.getCriticality()))
                .toList();
        }
        if (params.getRiskBand() != null) {
            // Would need join with risk_scores - simplified for now
        }
        if (params.getMinHealthIndex() != null || params.getMaxHealthIndex() != null) {
            double min = params.getMinHealthIndex() != null ? params.getMinHealthIndex() : 0;
            double max = params.getMaxHealthIndex() != null ? params.getMaxHealthIndex() : 1.0;
            assets = assets.stream()
                .filter(a -> a.getHealthIndex().value() >= min && a.getHealthIndex().value() <= max)
                .toList();
        }

        // Limit results
        if (assets.size() > params.getLimit()) {
            assets = assets.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<AssetProperties>> features = assets.stream()
            .map(this::toAssetFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<AssetProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<AssetProperties> toAssetFeature(Asset asset) {
        Geometry geom = asset.getGeometry().getGeometry();
        GeoJsonGeometry geometry = convertGeometry(geom);

        // Get latest risk score
        Optional<RiskScore> riskScore = riskScoreRepository.findTopByAssetIdOrderByComputedAtDesc(asset.getId());
        CommonKernel.RiskBand riskBand = riskScore.map(RiskScore::getBand).orElse(null);
        Double riskScoreValue = riskScore.map(RiskScore::getScore).orElse(null);

        AssetProperties props = AssetProperties.builder()
            .id(asset.getId().value())
            .name(asset.getName())
            .type(asset.getType().name())
            .typeDisplayName(asset.getType().getDisplayName())
            .criticality(asset.getCriticality().name())
            .healthIndex(asset.getHealthIndex().value())
            .healthStatus(asset.getHealthIndex().isHealthy() ? "HEALTHY" :
                         asset.getHealthIndex().isDegraded() ? "DEGRADED" : "CRITICAL")
            .redundancyLevel(asset.getRedundancyLevel())
            .hasRedundancy(asset.hasRedundancy())
            .gridNodeId(asset.getGridNodeId() != null ? asset.getGridNodeId().value() : null)
            .description(asset.getDescription())
            .installedAt(asset.getInstalledAt())
            .lastInspectedAt(asset.getLastInspectedAt())
            .riskBand(riskBand != null ? riskBand.name() : null)
            .riskScore(riskScoreValue)
            .baseRiskWeight(asset.getType().baseRiskWeight())
            .build();

        return GeoJsonFeature.of(geometry, props, asset.getId().value());
    }

    // =========================================================================
    // Threat Events
    // =========================================================================

    public GeoJsonFeatureCollection<ThreatEventProperties> getThreats(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<ThreatEvent> threats;

        if (params.getSeverity() != null) {
            threats = threatEventRepository.findByBboxAndSeverity(envelope, params.getSeverity().toUpperCase());
        } else if (params.getThreatType() != null) {
            threats = threatEventRepository.findByBboxAndType(envelope, params.getThreatType().toUpperCase());
        } else if (params.getThreatStatus() != null) {
            threats = threatEventRepository.findByBboxAndStatus(envelope, params.getThreatStatus().toUpperCase());
        } else if (params.getMinConfidence() != null) {
            threats = threatEventRepository.findByBboxAndMinConfidence(envelope, params.getMinConfidence());
        } else {
            threats = threatEventRepository.findByBbox(envelope);
        }

        // Apply additional filters
        if (params.getDetectedAfter() != null) {
            threats = threats.stream()
                .filter(t -> t.getDetectedAt().isAfter(params.getDetectedAfter()))
                .toList();
        }
        if (params.getDetectedBefore() != null) {
            threats = threats.stream()
                .filter(t -> t.getDetectedAt().isBefore(params.getDetectedBefore()))
                .toList();
        }

        // Limit results
        if (threats.size() > params.getLimit()) {
            threats = threats.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<ThreatEventProperties>> features = threats.stream()
            .map(this::toThreatEventFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<ThreatEventProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<ThreatEventProperties> toThreatEventFeature(ThreatEvent threat) {
        Geometry geom = threat.getLocation() != null ? threat.getLocation().getGeometry() : null;
        GeoJsonGeometry geometry = geom != null ? convertGeometry(geom) :
            GeoJsonGeometry.point(0, 0); // fallback

        ThreatEventProperties props = ThreatEventProperties.builder()
            .id(threat.getId().value())
            .type(threat.getType().name())
            .severity(threat.getSeverity().name())
            .confidence(threat.getConfidence())
            .detectedAt(threat.getDetectedAt())
            .firstObservedAt(threat.getFirstObservedAt())
            .affectedAssetId(threat.getAffectedAssetId() != null ? threat.getAffectedAssetId().value() : null)
            .nearbyGridNodeId(threat.getNearbyGridNodeId() != null ? threat.getNearbyGridNodeId().value() : null)
            .description(threat.getDescription())
            .rationale(threat.getRationale())
            .status(threat.getStatus().name())
            .detectionStrategy(threat.getDetectionStrategy())
            .strategyVersion(threat.getStrategyVersion())
            .isActionable(threat.isActionable())
            .evidenceCount(threat.getSupportingEvidence().size())
            .build();

        return GeoJsonFeature.of(geometry, props, threat.getId().value());
    }

    // =========================================================================
    // Incidents
    // =========================================================================

    public GeoJsonFeatureCollection<IncidentProperties> getIncidents(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<Incident> incidents;

        if (Boolean.TRUE.equals(params.getActiveOnly())) {
            incidents = incidentRepository.findActiveByBbox(envelope);
        } else if (params.getIncidentState() != null) {
            incidents = incidentRepository.findByBboxAndState(envelope, params.getIncidentState().toUpperCase());
        } else if (params.getIncidentSeverity() != null) {
            incidents = incidentRepository.findByBboxAndSeverity(envelope, params.getIncidentSeverity().toUpperCase());
        } else {
            incidents = incidentRepository.findByBbox(envelope);
        }

        // Limit results
        if (incidents.size() > params.getLimit()) {
            incidents = incidents.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<IncidentProperties>> features = incidents.stream()
            .map(this::toIncidentFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<IncidentProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<IncidentProperties> toIncidentFeature(Incident incident) {
        Geometry geom = incident.getLocation() != null ? incident.getLocation().getGeometry() : null;
        GeoJsonGeometry geometry = geom != null ? convertGeometry(geom) :
            GeoJsonGeometry.point(0, 0); // fallback

        IncidentProperties props = IncidentProperties.builder()
            .id(incident.getId().value())
            .incidentNumber(incident.getIncidentNumber())
            .state(incident.getState().name())
            .severity(incident.getSeverity().name())
            .originatingThreatId(incident.getOriginatingThreatId() != null ? incident.getOriginatingThreatId().value() : null)
            .primaryAssetId(incident.getPrimaryAssetId() != null ? incident.getPrimaryAssetId().value() : null)
            .primaryGridNodeId(incident.getPrimaryGridNodeId() != null ? incident.getPrimaryGridNodeId().value() : null)
            .description(incident.getDescription())
            .operatorNotes(incident.getOperatorNotes())
            .assignedOperatorId(incident.getAssignedOperatorId() != null ? incident.getAssignedOperatorId().value() : null)
            .incidentCommanderId(incident.getIncidentCommanderId() != null ? incident.getIncidentCommanderId().value() : null)
            .detectedAt(incident.getDetectedAt())
            .triagedAt(incident.getTriagedAt())
            .confirmedAt(incident.getConfirmedAt())
            .responseStartedAt(incident.getResponseStartedAt())
            .containedAt(incident.getContainedAt())
            .recoveringAt(incident.getRecoveringAt())
            .resolvedAt(incident.getResolvedAt())
            .closedAt(incident.getClosedAt())
            .isActive(incident.isActive())
            .isTerminal(incident.isTerminal())
            .stateHistory(incident.getStateHistory().stream()
                .map(st -> Map.<String, Object>of(
                    "toState", st.getToState().name(),
                    "actorId", st.getActorId() != null ? st.getActorId().value() : null,
                    "reason", st.getReason(),
                    "timestamp", st.getTimestamp()
                ))
                .toList())
            .build();

        return GeoJsonFeature.of(geometry, props, incident.getId().value());
    }

    // =========================================================================
    // Risk Scores
    // =========================================================================

    public GeoJsonFeatureCollection<RiskScoreProperties> getRiskScores(SpatialQueryParams params) {
        Envelope envelope = createEnvelope(params);
        List<RiskScore> scores = riskScoreRepository.findLatestByAssetBbox(envelope);

        // Apply filters
        if (params.getRiskBand() != null) {
            scores = scores.stream()
                .filter(r -> r.getBand().name().equalsIgnoreCase(params.getRiskBand()))
                .toList();
        }
        if (params.getMinRiskScore() != null || params.getMaxRiskScore() != null) {
            double min = params.getMinRiskScore() != null ? params.getMinRiskScore() : 0;
            double max = params.getMaxRiskScore() != null ? params.getMaxRiskScore() : 1.0;
            scores = scores.stream()
                .filter(r -> r.getScore() >= min && r.getScore() <= max)
                .toList();
        }

        // Limit results
        if (scores.size() > params.getLimit()) {
            scores = scores.subList(0, params.getLimit());
        }

        List<GeoJsonFeature<RiskScoreProperties>> features = scores.stream()
            .map(this::toRiskScoreFeature)
            .collect(Collectors.toList());

        return GeoJsonFeatureCollection.<RiskScoreProperties>of(features, calculateBbox(features));
    }

    private GeoJsonFeature<RiskScoreProperties> toRiskScoreFeature(RiskScore score) {
        // RiskScore doesn't have direct geometry - use asset's geometry
        // For now, return point at asset location (would need asset join)
        GeoJsonGeometry geometry = GeoJsonGeometry.point(0, 0); // placeholder

        RiskScoreProperties props = RiskScoreProperties.builder()
            .id(score.getId().value())
            .assetId(score.getAssetId().value())
            .score(score.getScore())
            .band(score.getBand().name())
            .modelName(score.getModelName())
            .modelVersion(score.getModelVersion())
            .strategyName(score.getStrategyName())
            .factorBreakdown(score.getFactorBreakdown() != null ?
                score.getFactorBreakdown().getAllContributions().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    )) : Map.of())
            .computedAt(score.getComputedAt())
            .correlationId(score.getCorrelationId() != null ? score.getCorrelationId().value() : null)
            .build();

        return GeoJsonFeature.of(geometry, props, score.getId().value());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Envelope createEnvelope(SpatialQueryParams params) {
        double[] bbox = params.parseBbox();
        if (bbox != null) {
            return new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        }
        // Return world envelope if no bbox provided
        return new Envelope(-180, 180, -90, 90);
    }

    private GeoJsonGeometry convertGeometry(Geometry geom) {
        if (geom instanceof Point point) {
            return GeoJsonGeometry.point(point.getX(), point.getY());
        } else if (geom instanceof LineString lineString) {
            Coordinate[] coords = lineString.getCoordinates();
            List<List<Double>> coordinates = new ArrayList<>();
            for (Coordinate coord : coords) {
                coordinates.add(List.of(coord.x, coord.y));
            }
            return GeoJsonGeometry.lineString(coordinates);
        } else if (geom instanceof Polygon polygon) {
            // Exterior ring
            List<List<Double>> exterior = new ArrayList<>();
            for (Coordinate coord : polygon.getExteriorRing().getCoordinates()) {
                exterior.add(List.of(coord.x, coord.y));
            }
            // Interior rings (holes)
            List<List<List<Double>>> coordinates = new ArrayList<>();
            coordinates.add(exterior);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                List<List<Double>> interior = new ArrayList<>();
                for (Coordinate coord : polygon.getInteriorRingN(i).getCoordinates()) {
                    interior.add(List.of(coord.x, coord.y));
                }
                coordinates.add(interior);
            }
            return GeoJsonGeometry.polygon(coordinates);
        }
        // Fallback for other geometry types (MultiPoint, MultiLineString, etc.)
        // Use centroid as point representation
        return GeoJsonGeometry.point(geom.getCentroid().getX(), geom.getCentroid().getY());
    }

    private <T> GeoJsonFeatureCollection.BoundingBox calculateBbox(List<GeoJsonFeature<T>> features) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (GeoJsonFeature<?> feature : features) {
            GeoJsonGeometry geom = feature.getGeometry();
            if (geom == null) continue;

            if ("Point".equals(geom.getType())) {
                @SuppressWarnings("unchecked")
                List<Double> coords = (List<Double>) geom.getCoordinates();
                if (coords != null && coords.size() >= 2) {
                    minX = Math.min(minX, coords.get(0));
                    minY = Math.min(minY, coords.get(1));
                    maxX = Math.max(maxX, coords.get(0));
                    maxY = Math.max(maxY, coords.get(1));
                }
            } else if ("LineString".equals(geom.getType()) || "Polygon".equals(geom.getType())) {
                // Simplified: just use first coordinate for bbox calculation
                // In production, would iterate all coordinates
                @SuppressWarnings("unchecked")
                List<List<Double>> coords = (List<List<Double>>) geom.getCoordinates();
                if (coords != null && !coords.isEmpty()) {
                    List<Double> first = coords.get(0);
                    if (first != null && first.size() >= 2) {
                        minX = Math.min(minX, first.get(0));
                        minY = Math.min(minY, first.get(1));
                        maxX = Math.max(maxX, first.get(0));
                        maxY = Math.max(maxY, first.get(1));
                    }
                }
            }
        }

        if (minX == Double.MAX_VALUE) return null;
        return GeoJsonFeatureCollection.BoundingBox.of(minX, minY, maxX, maxY);
    }
}