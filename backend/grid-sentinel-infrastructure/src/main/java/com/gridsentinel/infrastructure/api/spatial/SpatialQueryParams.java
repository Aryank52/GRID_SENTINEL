package com.gridsentinel.infrastructure.api.spatial;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;

/**
 * Spatial query parameters for viewport-based and filtered queries.
 * All parameters are optional and can be combined.
 */
public class SpatialQueryParams {

    /**
     * Bounding box in format "minLng,minLat,maxLng,maxLat" (WGS84).
     * Example: "-122.5,37.0,-121.5,38.0"
     * If not provided, returns all features (use with caution for large datasets).
     */
    @Pattern(regexp = "^[-]?\\d+(\\.\\d+)?,[-]?\\d+(\\.\\d+)?,[-]?\\d+(\\.\\d+)?,[-]?\\d+(\\.\\d+)?$",
             message = "bbox must be in format 'minLng,minLat,maxLng,maxLat'")
    private String bbox;

    /**
     * Maximum number of features to return (pagination).
     * Default: 1000, Max: 10000
     */
    @Min(1)
    @Max(10000)
    private Integer limit = 1000;

    /**
     * Offset for pagination.
     */
    @Min(0)
    private Integer offset = 0;

    // =========================================================================
    // Asset Filters
    // =========================================================================

    /** Filter by asset type (comma-separated for multiple). */
    private String assetType;

    /** Filter by criticality (LOW, MEDIUM, HIGH, MISSION_CRITICAL). */
    private String criticality;

    /** Filter by risk band (NEGLIGIBLE, LOW, MEDIUM, HIGH, CRITICAL). */
    private String riskBand;

    /** Filter by health index range (min). */
    @Min(0)
    @Max(1)
    private Double minHealthIndex;

    /** Filter by health index range (max). */
    @Min(0)
    @Max(1)
    private Double maxHealthIndex;

    // =========================================================================
    // Grid Node Filters
    // =========================================================================

    /** Filter by node type (SUBSTATION, SWITCHING_STATION, GENERATION, etc.). */
    private String nodeType;

    /** Filter by voltage level range (min kV). */
    @Min(0)
    private Double minVoltageKv;

    /** Filter by voltage level range (max kV). */
    @Min(0)
    private Double maxVoltageKv;

    // =========================================================================
    // Grid Edge Filters
    // =========================================================================

    /** Filter by edge type (TRANSMISSION_LINE, TRANSFORMER, etc.). */
    private String edgeType;

    // =========================================================================
    // Threat Filters
    // =========================================================================

    /** Filter by threat severity (INFO, LOW, MEDIUM, HIGH, CRITICAL). */
    private String severity;

    /** Filter by threat type. */
    private String threatType;

    /** Filter by threat status (NEW, TRIAGED, CONFIRMED, etc.). */
    private String threatStatus;

    /** Filter by minimum confidence threshold. */
    @Min(0)
    @Max(1)
    private Double minConfidence;

    /** Filter by detection time range (start). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant detectedAfter;

    /** Filter by detection time range (end). */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant detectedBefore;

    // =========================================================================
    // Incident Filters
    // =========================================================================

    /** Filter by incident state (DETECTED, TRIAGED, CONFIRMED, etc.). */
    private String incidentState;

    /** Filter by incident severity. */
    private String incidentSeverity;

    /** Show only active incidents (not resolved/closed). */
    private Boolean activeOnly;

    // =========================================================================
    // Risk Filters
    // =========================================================================

    /** Filter by risk score range (min). */
    @Min(0)
    @Max(1)
    private Double minRiskScore;

    /** Filter by risk score range (max). */
    @Min(0)
    @Max(1)
    private Double maxRiskScore;

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    public String getBbox() { return bbox; }
    public void setBbox(String bbox) { this.bbox = bbox; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getOffset() { return offset; }
    public void setOffset(Integer offset) { this.offset = offset; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getCriticality() { return criticality; }
    public void setCriticality(String criticality) { this.criticality = criticality; }

    public String getRiskBand() { return riskBand; }
    public void setRiskBand(String riskBand) { this.riskBand = riskBand; }

    public Double getMinHealthIndex() { return minHealthIndex; }
    public void setMinHealthIndex(Double minHealthIndex) { this.minHealthIndex = minHealthIndex; }

    public Double getMaxHealthIndex() { return maxHealthIndex; }
    public void setMaxHealthIndex(Double maxHealthIndex) { this.maxHealthIndex = maxHealthIndex; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public Double getMinVoltageKv() { return minVoltageKv; }
    public void setMinVoltageKv(Double minVoltageKv) { this.minVoltageKv = minVoltageKv; }

    public Double getMaxVoltageKv() { return maxVoltageKv; }
    public void setMaxVoltageKv(Double maxVoltageKv) { this.maxVoltageKv = maxVoltageKv; }

    public String getEdgeType() { return edgeType; }
    public void setEdgeType(String edgeType) { this.edgeType = edgeType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getThreatType() { return threatType; }
    public void setThreatType(String threatType) { this.threatType = threatType; }

    public String getThreatStatus() { return threatStatus; }
    public void setThreatStatus(String threatStatus) { this.threatStatus = threatStatus; }

    public Double getMinConfidence() { return minConfidence; }
    public void setMinConfidence(Double minConfidence) { this.minConfidence = minConfidence; }

    public Instant getDetectedAfter() { return detectedAfter; }
    public void setDetectedAfter(Instant detectedAfter) { this.detectedAfter = detectedAfter; }

    public Instant getDetectedBefore() { return detectedBefore; }
    public void setDetectedBefore(Instant detectedBefore) { this.detectedBefore = detectedBefore; }

    public String getIncidentState() { return incidentState; }
    public void setIncidentState(String incidentState) { this.incidentState = incidentState; }

    public String getIncidentSeverity() { return incidentSeverity; }
    public void setIncidentSeverity(String incidentSeverity) { this.incidentSeverity = incidentSeverity; }

    public Boolean getActiveOnly() { return activeOnly; }
    public void setActiveOnly(Boolean activeOnly) { this.activeOnly = activeOnly; }

    public Double getMinRiskScore() { return minRiskScore; }
    public void setMinRiskScore(Double minRiskScore) { this.minRiskScore = minRiskScore; }

    public Double getMaxRiskScore() { return maxRiskScore; }
    public void setMaxRiskScore(Double maxRiskScore) { this.maxRiskScore = maxRiskScore; }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Parse bbox string into double array [minLng, minLat, maxLng, maxLat].
     * Returns null if bbox is not set or invalid.
     */
    public double[] parseBbox() {
        if (bbox == null || bbox.isBlank()) {
            return null;
        }
        try {
            String[] parts = bbox.split(",");
            if (parts.length != 4) return null;
            double minLng = Double.parseDouble(parts[0].trim());
            double minLat = Double.parseDouble(parts[1].trim());
            double maxLng = Double.parseDouble(parts[2].trim());
            double maxLat = Double.parseDouble(parts[3].trim());
            // Validate WGS84 bounds
            if (minLng < -180 || minLng > 180 || maxLng < -180 || maxLng > 180 ||
                minLat < -90 || minLat > 90 || maxLat < -90 || maxLat > 90) {
                return null;
            }
            if (minLng > maxLng || minLat > maxLat) {
                return null;
            }
            return new double[]{minLng, minLat, maxLng, maxLat};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean hasBbox() {
        return bbox != null && !bbox.isBlank();
    }

    public List<String> parseAssetTypes() {
        return assetType != null ? List.of(assetType.split(",")) : List.of();
    }
}