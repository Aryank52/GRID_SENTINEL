package com.gridsentinel.risk;

/**
 * Standard risk factors used in grid asset risk scoring.
 * Each factor has a default weight that can be overridden by strategy configuration.
 */
public enum RiskFactor {
    // Asset intrinsic factors
    ASSET_CRITICALITY("Asset Criticality", 0.20, "Inherent importance of asset to grid operation"),
    ASSET_TYPE_RISK("Asset Type Risk", 0.10, "Base risk associated with asset type"),
    ASSET_HEALTH("Asset Health Index", 0.15, "Current condition/degradation of asset"),
    ASSET_AGE("Asset Age", 0.05, "Years since installation"),
    REDUNDANCY_LEVEL("Redundancy Level", 0.10, "N-x redundancy availability"),

    // Physical exposure factors
    PHYSICAL_EXPOSURE("Physical Exposure", 0.10, "Accessibility to human activity, weather, terrain"),
    ENVIRONMENTAL_RISK("Environmental Risk", 0.08, "Flood, seismic, wildfire, landslide exposure"),
    VEGETATION_PROXIMITY("Vegetation Proximity", 0.05, "Distance to vegetation encroachment"),
    WEATHER_EXPOSURE("Weather Exposure", 0.05, "Historical extreme weather frequency"),

    // Network topology factors
    NETWORK_CENTRALITY("Network Centrality", 0.12, "Betweenness/degree centrality in grid graph"),
    LOAD_SERVED("Load Served", 0.08, "MW of load dependent on this asset"),
    ALTERNATIVE_PATHS("Alternative Paths", 0.07, "Availability of re-routing options"),

    // Historical factors
    INCIDENT_FREQUENCY("Historical Incident Frequency", 0.10, "Past incidents at or near this asset"),
    MEAN_TIME_TO_REPAIR("Mean Time to Repair", 0.05, "Historical average repair duration"),
    FAILURE_MODE_HISTORY("Failure Mode History", 0.05, "Recurring failure modes observed");

    private final String displayName;
    private final double defaultWeight;
    private final String description;

    RiskFactor(String displayName, double defaultWeight, String description) {
        this.displayName = displayName;
        this.defaultWeight = defaultWeight;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get all factors sorted by default weight (descending).
     */
    public static java.util.List<RiskFactor> byWeightDesc() {
        return java.util.Arrays.stream(values())
                .sorted((a, b) -> Double.compare(b.defaultWeight, a.defaultWeight))
                .toList();
    }

    /**
     * Get factors by category.
     */
    public static java.util.List<RiskFactor> intrinsic() {
        return java.util.List.of(
                ASSET_CRITICALITY, ASSET_TYPE_RISK, ASSET_HEALTH, ASSET_AGE, REDUNDANCY_LEVEL
        );
    }

    public static java.util.List<RiskFactor> environmental() {
        return java.util.List.of(
                PHYSICAL_EXPOSURE, ENVIRONMENTAL_RISK, VEGETATION_PROXIMITY, WEATHER_EXPOSURE
        );
    }

    public static java.util.List<RiskFactor> network() {
        return java.util.List.of(
                NETWORK_CENTRALITY, LOAD_SERVED, ALTERNATIVE_PATHS
        );
    }

    public static java.util.List<RiskFactor> historical() {
        return java.util.List.of(
                INCIDENT_FREQUENCY, MEAN_TIME_TO_REPAIR, FAILURE_MODE_HISTORY
        );
    }
}