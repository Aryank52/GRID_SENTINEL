package com.gridsentinel.observation;

/**
 * Type of evidence extracted from an observation.
 * Each type has an associated severity level for threat detection.
 */
public enum EvidenceType {
    // Thermal
    TEMPERATURE_ANOMALY,
    HOTSPOT_DETECTED,
    THERMAL_GRADIENT,
    THERMAL_DELTA,

    // Visual
    SMOKE_DETECTED,
    FIRE_DETECTED,
    VEGETATION_ENCROACHMENT,
    STRUCTURAL_DAMAGE,
    CONDUCTOR_SAG,
    CORROSION_DETECTED,
    INSULATOR_DAMAGE,
    FOREIGN_OBJECT,
    STRUCTURAL_CORROSION,

    // GIS / Spatial
    FLOOD_PROXIMITY,
    LANDSLIDE_RISK_ZONE,
    SEISMIC_ZONE,
    WILDFIRE_PERIMETER,
    LANDSLIDE_RISK,

    // Weather
    WIND_SPEED,
    WIND_GUST,
    LIGHTNING_STRIKE_PROXIMITY,
    PRECIPITATION_RATE,
    TEMPERATURE_EXTREME,
    EXTREME_WIND,
    HIGH_HUMIDITY,

    // SCADA / Telemetry
    CURRENT_OVERLOAD,
    VOLTAGE_DEVIATION,
    FREQUENCY_DEVIATION,
    POWER_SWING,
    PROTECTION_OPERATION,
    COMMUNICATION_LOSS,

    // Inspection
    INSPECTION_FINDING,
    MAINTENANCE_OVERDUE,
    CONDITION_ASSESSMENT,

    // Security / Physical
    PHYSICAL_INTRUSION,
    CYBER_INTRUSION,
    GAS_LEAK,
    MECHANICAL_ANOMALY,

    // Fire / Environmental
    FIRE,
    FLOODING,

    // Other
    UNKNOWN_ANOMALY;
}