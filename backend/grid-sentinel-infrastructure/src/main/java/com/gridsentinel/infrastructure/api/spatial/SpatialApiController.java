package com.gridsentinel.infrastructure.api.spatial;

import com.gridsentinel.infrastructure.api.spatial.SpatialQueryParams;
import com.gridsentinel.infrastructure.api.spatial.SpatialQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for spatial/GeoJSON API endpoints.
 * Provides viewport-aware spatial queries for the Digital Grid Twin map.
 */
@RestController
@RequestMapping("/api/spatial")
@Validated
public class SpatialApiController {

    private final SpatialQueryService spatialQueryService;

    public SpatialApiController(SpatialQueryService spatialQueryService) {
        this.spatialQueryService = spatialQueryService;
    }

    /**
     * Get grid nodes as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters (bbox, filters, pagination)
     * @return GeoJSON FeatureCollection of GridNode features
     *
     * Example: GET /api/spatial/grid/nodes?bbox=-122.5,37.0,-121.5,38.0&nodeType=SUBSTATION&limit=500
     */
    @GetMapping("/grid/nodes")
    public ResponseEntity<GeoJsonFeatureCollection<GridNodeProperties>> getGridNodes(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getGridNodes(params));
    }

    /**
     * Get grid edges as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters
     * @return GeoJSON FeatureCollection of GridEdge features
     *
     * Example: GET /api/spatial/grid/edges?bbox=-122.5,37.0,-121.5,38.0&edgeType=TRANSMISSION_LINE
     */
    @GetMapping("/grid/edges")
    public ResponseEntity<GeoJsonFeatureCollection<GridEdgeProperties>> getGridEdges(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getGridEdges(params));
    }

    /**
     * Get assets as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters
     * @return GeoJSON FeatureCollection of Asset features
     *
     * Example: GET /api/spatial/assets?bbox=-122.5,37.0,-121.5,38.0&criticality=HIGH&riskBand=CRITICAL
     */
    @GetMapping("/assets")
    public ResponseEntity<GeoJsonFeatureCollection<AssetProperties>> getAssets(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getAssets(params));
    }

    /**
     * Get threat events as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters
     * @return GeoJSON FeatureCollection of ThreatEvent features
     *
     * Example: GET /api/spatial/threats?bbox=-122.5,37.0,-121.5,38.0&severity=CRITICAL&minConfidence=0.8
     */
    @GetMapping("/threats")
    public ResponseEntity<GeoJsonFeatureCollection<ThreatEventProperties>> getThreats(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getThreats(params));
    }

    /**
     * Get incidents as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters
     * @return GeoJSON FeatureCollection of Incident features
     *
     * Example: GET /api/spatial/incidents?bbox=-122.5,37.0,-121.5,38.0&activeOnly=true
     */
    @GetMapping("/incidents")
    public ResponseEntity<GeoJsonFeatureCollection<IncidentProperties>> getIncidents(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getIncidents(params));
    }

    /**
     * Get risk scores as GeoJSON FeatureCollection.
     *
     * @param params Spatial query parameters
     * @return GeoJSON FeatureCollection of RiskScore features
     *
     * Example: GET /api/spatial/risk?bbox=-122.5,37.0,-121.5,38.0&riskBand=CRITICAL
     */
    @GetMapping("/risk")
    public ResponseEntity<GeoJsonFeatureCollection<RiskScoreProperties>> getRiskScores(
            @Valid SpatialQueryParams params) {
        return ResponseEntity.ok(spatialQueryService.getRiskScores(params));
    }

    /**
     * Health check endpoint for spatial API.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Spatial API operational");
    }
}