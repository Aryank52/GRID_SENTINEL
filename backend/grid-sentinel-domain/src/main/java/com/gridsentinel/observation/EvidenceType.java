package com.gridsentinel.observation;

/**
 * Type of evidence extracted from an observation.
 */
public enum EvidenceType {
    // Thermal
    TEMPERATURE_ANOMALY,        // Degrees C above ambient
    HOTSPOT_DETECTED,           // Normalized intensity
    THERMAL_GRADIENT,           // Degrees C per meter

    // Visual
    SMOKE_DETECTED,             // Normalized confidence
    FIRE_DETECTED,              // Normalized confidence
    VEGETATION_ENCROACHMENT,    // Distance to conductor (meters)
    STRUCTURAL_DAMAGE,          // Normalized severity
    CONDUCTOR_SAG,              // Meters below design
    CORROSION_DETECTED,         // Normalized severity
    INSULATOR_DAMAGE,           // Normalized severity
    FOREIGN_OBJECT,             // Normalized confidence

    // GIS / Spatial
    FLOOD_PROXIMITY,            // Meters to flood zone
    LANDSLIDE_RISK_ZONE,        // Normalized risk
    SEISMIC_ZONE,               // PGA (peak ground acceleration)
    WILDFIRE_PERIMETER,         // Meters to fire perimeter

    // Weather
    WIND_SPEED,                 // m/s
    WIND_GUST,                  // m/s
    LIGHTNING_STRIKE_PROXIMITY, // Meters
    PRECIPITATION_RATE,         // mm/hr
    TEMPERATURE_EXTREME,        // Degrees C

    // SCADA / Telemetry
    CURRENT_OVERLOAD,           // % of thermal rating
    VOLTAGE_DEVIATION,          // % from nominal
    FREQUENCY_DEVIATION,        // Hz from nominal
    POWER_SWING,                // Normalized magnitude
    PROTECTION_OPERATION,       // Binary (0/1)
    COMMUNICATION_LOSS,         // Duration seconds

    // Inspection
    INSPECTION_FINDING,         // Normalized severity
    MAINTENANCE_OVERDUE,        // Days overdue
    CONDITION_ASSESSMENT        // Normalized health score
}