package com.gridsentinel.asset;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.CommonKernel.Criticality;

import java.util.Set;

/**
 * Controlled vocabulary of asset types in the grid infrastructure.
 * Each type has default criticality, typical redundancy expectations, and risk factors.
 */
public enum AssetType {
    // Transmission
    TRANSMISSION_TOWER("Transmission Tower", Criticality.HIGH, Set.of("structural", "foundation", "conductor")),
    TRANSMISSION_LINE("Transmission Line", Criticality.HIGH, Set.of("conductor", "insulator", "vegetation")),
    SUBSTATION("Substation", Criticality.MISSION_CRITICAL, Set.of("transformer", "switchgear", "protection", "control")),
    SWITCHING_STATION("Switching Station", Criticality.HIGH, Set.of("breaker", "busbar", "protection")),

    // Distribution
    DISTRIBUTION_POLE("Distribution Pole", Criticality.MEDIUM, Set.of("structural", "crossarm", "conductor")),
    DISTRIBUTION_LINE("Distribution Line", Criticality.MEDIUM, Set.of("conductor", "vegetation", "animal")),
    DISTRIBUTION_TRANSFORMER("Distribution Transformer", Criticality.MEDIUM, Set.of("oil", "insulation", "cooling")),
    RECLOSER("Recloser", Criticality.HIGH, Set.of("mechanical", "control", "communication")),
    SECTIONALIZER("Sectionalizer", Criticality.MEDIUM, Set.of("mechanical", "control")),
    CAPACITOR_BANK("Capacitor Bank", Criticality.LOW, Set.of("cell", "fuse", "controller")),
    VOLTAGE_REGULATOR("Voltage Regulator", Criticality.MEDIUM, Set.of("tap_changer", "control", "insulation")),

    // Generation
    GENERATOR("Generator", Criticality.MISSION_CRITICAL, Set.of("rotor", "stator", "excitation", "governor")),
    TURBINE("Turbine", Criticality.MISSION_CRITICAL, Set.of("blade", "bearing", "gearbox", "control")),
    SOLAR_ARRAY("Solar Array", Criticality.HIGH, Set.of("panel", "inverter", "tracker", "wiring")),
    WIND_TURBINE("Wind Turbine", Criticality.HIGH, Set.of("blade", "gearbox", "generator", "control")),
    BATTERY_STORAGE("Battery Storage", Criticality.HIGH, Set.of("cell", "bms", "thermal", "inverter")),
    HYDRO_PLANT("Hydro Plant", Criticality.MISSION_CRITICAL, Set.of("turbine", "governor", "penstock", "dam")),

    // Control & Communication
    CONTROL_CENTER("Control Center", Criticality.MISSION_CRITICAL, Set.of("scada", "ems", "communication", "power")),
    RTU("Remote Terminal Unit", Criticality.HIGH, Set.of("communication", "io", "power", "processor")),
    SMART_METER("Smart Meter", Criticality.LOW, Set.of("communication", "measurement", "power")),
    PROTECTION_RELAY("Protection Relay", Criticality.HIGH, Set.of("algorithm", "communication", "io", "power")),
    PMU("Phasor Measurement Unit", Criticality.HIGH, Set.of("gps", "communication", "measurement", "processor")),

    // Physical Security
    PERIMETER_FENCE("Perimeter Fence", Criticality.MEDIUM, Set.of("structural", "sensor", "gate")),
    CCTV_CAMERA("CCTV Camera", Criticality.MEDIUM, Set.of("lens", "sensor", "communication", "power", "housing")),
    ACCESS_CONTROL("Access Control System", Criticality.HIGH, Set.of("reader", "controller", "lock", "communication")),
    INTRUSION_DETECTION("Intrusion Detection System", Criticality.HIGH, Set.of("sensor", "processor", "communication")),

    // Environmental
    WEATHER_STATION("Weather Station", Criticality.MEDIUM, Set.of("sensor", "communication", "power", "mount")),
    SEISMIC_MONITOR("Seismic Monitor", Criticality.MEDIUM, Set.of("sensor", "communication", "power", "mount"));

    private final String displayName;
    private final CommonKernel.Criticality defaultCriticality;
    private final Set<String> typicalFailureModes;

    AssetType(String displayName, CommonKernel.Criticality defaultCriticality, Set<String> typicalFailureModes) {
        this.displayName = displayName;
        this.defaultCriticality = defaultCriticality;
        this.typicalFailureModes = Set.copyOf(typicalFailureModes);
    }

    public String getDisplayName() {
        return displayName;
    }

    public CommonKernel.Criticality getDefaultCriticality() {
        return defaultCriticality;
    }

    public Set<String> getTypicalFailureModes() {
        return typicalFailureModes;
    }

    /**
     * Get typical base risk factor weight for this asset type.
     * Higher = more inherently risky.
     */
    public double baseRiskWeight() {
        return switch (this) {
            case SUBSTATION, CONTROL_CENTER, GENERATOR, TURBINE, HYDRO_PLANT -> 1.0;
            case TRANSMISSION_TOWER, TRANSMISSION_LINE, SWITCHING_STATION, PROTECTION_RELAY, PMU, BATTERY_STORAGE -> 0.9;
            case DISTRIBUTION_TRANSFORMER, RECLOSER, VOLTAGE_REGULATOR, SOLAR_ARRAY, WIND_TURBINE, RTU -> 0.7;
            case DISTRIBUTION_POLE, DISTRIBUTION_LINE, SECTIONALIZER, ACCESS_CONTROL, INTRUSION_DETECTION -> 0.5;
            case CAPACITOR_BANK, SMART_METER, PERIMETER_FENCE, CCTV_CAMERA, WEATHER_STATION, SEISMIC_MONITOR -> 0.3;
        };
    }
}