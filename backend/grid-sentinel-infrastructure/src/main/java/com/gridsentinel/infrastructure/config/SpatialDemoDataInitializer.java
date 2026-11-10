package com.gridsentinel.infrastructure.config;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.asset.AssetType;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatType;
import com.gridsentinel.detection.ThreatStatus;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.NodeType;
import com.gridsentinel.grid.EdgeType;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.risk.FactorBreakdown;
import com.gridsentinel.risk.FactorContribution;
import com.gridsentinel.infrastructure.persistence.repository.AssetRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridEdgeRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridNodeRepository;
import com.gridsentinel.infrastructure.persistence.repository.IncidentRepository;
import com.gridsentinel.infrastructure.persistence.repository.RiskScoreRepository;
import com.gridsentinel.infrastructure.persistence.repository.ThreatEventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Deterministic spatial demo data initialization for development/demo profile.
 * Creates a realistic synthetic grid topology with all spatial entities.
 * <p>
 * The dataset represents a realistic synthetic electricity grid in a fictional region
 * around -122.3 longitude, 47.6 latitude (Pacific Northwest area).
 * All identifiers are deterministic for reproducibility.
 */
@Configuration
@Profile("dev")
public class SpatialDemoDataInitializer {

    // Fixed reference timestamp for deterministic timestamps
    private static final Instant REFERENCE_TIME = Instant.parse("2026-01-15T00:00:00Z");
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    // Deterministic UUIDs for reproducibility
    // Nodes
    private static final UUID NODE_GENERATION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID NODE_TRANSMISSION_SUB_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID NODE_DISTRIBUTION_SUB_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID NODE_SWITCHING_1_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID NODE_SWITCHING_2_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID NODE_LOAD_CENTER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID NODE_INDUSTRIAL_LOAD_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID NODE_RURAL_SUB_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    // Edges
    private static final UUID EDGE_GEN_TO_TRANS_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID EDGE_TRANS_TO_DIST_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID EDGE_TRANS_TO_SWITCH1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID EDGE_SWITCH1_TO_SWITCH2_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID EDGE_SWITCH2_TO_LOAD_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    private static final UUID EDGE_SWITCH2_TO_INDUSTRIAL_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final UUID EDGE_DIST_TO_RURAL_ID = UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final UUID EDGE_SWITCH1_TO_RURAL_ID = UUID.fromString("23456789-2345-2345-2345-23456789abcd");

    // Assets
    private static final UUID ASSET_GENERATOR_1_ID = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    private static final UUID ASSET_GENERATOR_2_ID = UUID.fromString("b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2");
    private static final UUID ASSET_TRANSFORMER_TRANS_ID = UUID.fromString("c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3");
    private static final UUID ASSET_TRANSFORMER_DIST_ID = UUID.fromString("d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4");
    private static final UUID ASSET_TRANSFORMER_RURAL_ID = UUID.fromString("e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5");
    private static final UUID ASSET_BREAKER_1_ID = UUID.fromString("f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6");
    private static final UUID ASSET_BREAKER_2_ID = UUID.fromString("07070707-0707-0707-0707-070707070707");
    private static final UUID ASSET_RECLOSER_1_ID = UUID.fromString("18181818-1818-1818-1818-181818181818");
    private static final UUID ASSET_RECLOSER_2_ID = UUID.fromString("29292929-2929-2929-2929-292929292929");
    private static final UUID ASSET_CAPACITOR_BANK_ID = UUID.fromString("3a3a3a3a-3a3a-3a3a-3a3a-3a3a3a3a3a3a");
    private static final UUID ASSET_VOLTAGE_REGULATOR_ID = UUID.fromString("4b4b4b4b-4b4b-4b4b-4b4b-4b4b4b4b4b4b");
    private static final UUID ASSET_BATTERY_STORAGE_ID = UUID.fromString("5c5c5c5c-5c5c-5c5c-5c5c-5c5c5c5c5c5c");
    private static final UUID ASSET_SOLAR_ARRAY_ID = UUID.fromString("6d6d6d6d-6d6d-6d6d-6d6d-6d6d6d6d6d6d");
    private static final UUID ASSET_WIND_TURBINE_ID = UUID.fromString("7e7e7e7e-7e7e-7e7e-7e7e-7e7e7e7e7e7e");
    private static final UUID ASSET_TRANSFORMER_DIST_POLE_1_ID = UUID.fromString("8f8f8f8f-8f8f-8f8f-8f8f-8f8f8f8f8f8f");
    private static final UUID ASSET_TRANSFORMER_DIST_POLE_2_ID = UUID.fromString("9a9a9a9a-9a9a-9a9a-9a9a-9a9a9a9a9a9a");
    private static final UUID ASSET_CONTROL_CENTER_ID = UUID.fromString("abababab-abab-abab-abab-abababababab");
    private static final UUID ASSET_RTU_1_ID = UUID.fromString("bcbcbcbc-bcbc-bcbc-bcbc-bcbcbcbcbcbc");
    private static final UUID ASSET_RTU_2_ID = UUID.fromString("cdcdcdcd-cdcd-cdcd-cdcd-cdcdcdcdcdcd");
    private static final UUID ASSET_PROTECTION_RELAY_1_ID = UUID.fromString("dedeeded-dede-dede-dede-dedededede");
    private static final UUID ASSET_PMU_1_ID = UUID.fromString("efefefef-efef-efef-efef-efefefefefef");
    private static final UUID ASSET_PERIMETER_FENCE_ID = UUID.fromString("f0f0f0f0-f0f0-f0f0-f0f0-f0f0f0f0f0f0");
    private static final UUID ASSET_CCTV_1_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID ASSET_CCTV_2_ID = UUID.fromString("22222222-3333-4444-5555-666666666666");
    private static final UUID ASSET_ACCESS_CONTROL_ID = UUID.fromString("33333333-4444-5555-6666-777777777777");
    private static final UUID ASSET_INTRUSION_DETECTION_ID = UUID.fromString("44444444-5555-6666-7777-888888888888");
    private static final UUID ASSET_WEATHER_STATION_1_ID = UUID.fromString("55555555-6666-7777-8888-999999999999");
    private static final UUID ASSET_WEATHER_STATION_2_ID = UUID.fromString("66666666-7777-8888-9999-000000000000");
    private static final UUID ASSET_SEISMIC_MONITOR_ID = UUID.fromString("77777777-8888-9999-0000-111111111111");

    // Threats
    private static final UUID THREAT_OVERHEATING_TRANSFORMER_ID = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffffffffff");
    private static final UUID THREAT_VEGETATION_LINE_ID = UUID.fromString("bbbbcccc-dddd-eeee-ffff-aaaaaaaaaaaa");
    private static final UUID THREAT_ICE_ACCRETION_ID = UUID.fromString("ccccdddd-eeee-ffff-aaaa-bbbbbbbbbbbb");
    private static final UUID THREAT_CORROSION_TOWER_ID = UUID.fromString("ddddeeee-ffff-aaaa-bbbb-cccccccccccc");
    private static final UUID THREAT_PHYSICAL_INTRUSION_ID = UUID.fromString("eeeeffff-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID THREAT_CYBER_INTRUSION_ID = UUID.fromString("ffffaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID THREAT_OVERLOAD_LINE_ID = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffff00000000");

    // Incidents
    private static final UUID INCIDENT_OVERHEATING_ID = UUID.fromString("11112222-3333-4444-5555-666666666666");
    private static final UUID INCIDENT_VEGETATION_ID = UUID.fromString("22223333-4444-5555-6666-777777777777");
    private static final UUID INCIDENT_ICE_ACCRETION_ID = UUID.fromString("33334444-5555-6666-7777-888888888888");
    private static final UUID INCIDENT_PHYSICAL_INTRUSION_ID = UUID.fromString("44445555-6666-7777-8888-999999999999");
    private static final UUID INCIDENT_CYBER_INTRUSION_ID = UUID.fromString("55556666-7777-8888-9999-000000000000");
    private static final UUID INCIDENT_OVERLOAD_ID = UUID.fromString("66667777-8888-9999-0000-111111111111");

    // Risk Scores
    private static final UUID RISK_GENERATOR_1_ID = UUID.fromString("aaaa1111-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID RISK_GENERATOR_2_ID = UUID.fromString("bbbb2222-cccc-dddd-eeee-ffffffffffff");
    private static final UUID RISK_TRANSFORMER_TRANS_ID = UUID.fromString("cccc3333-dddd-eeee-ffff-aaaaaaaaaaaa");
    private static final UUID RISK_TRANSFORMER_DIST_ID = UUID.fromString("dddd4444-eeee-ffff-aaaa-bbbbbbbbbbbb");
    private static final UUID RISK_TRANSFORMER_RURAL_ID = UUID.fromString("eeee5555-ffff-aaaa-bbbb-cccccccccccc");
    private static final UUID RISK_BATTERY_STORAGE_ID = UUID.fromString("ffff6666-aaaa-bbbb-cccc-dddddddddddd");
    private static final UUID RISK_SOLAR_ARRAY_ID = UUID.fromString("1111aaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID RISK_WIND_TURBINE_ID = UUID.fromString("2222bbbb-cccc-dddd-eeee-ffffffffffff");

    @Bean
    CommandLineRunner initSpatialDemoData(
            GridNodeRepository nodeRepository,
            GridEdgeRepository edgeRepository,
            AssetRepository assetRepository,
            ThreatEventRepository threatRepository,
            IncidentRepository incidentRepository,
            RiskScoreRepository riskScoreRepository
    ) {
        return args -> {
            // Only seed if no spatial data exists
            if (nodeRepository.count() > 0) {
                return; // Already seeded
            }

            // Fixed reference time for all timestamps
            Instant baseTime = REFERENCE_TIME;

            // =====================================================================
            // 1. GRID NODES - Create a realistic grid topology
            // =====================================================================

            // Generation node (hydro plant) - North
            GridNode generationNode = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .name("Cedar Falls Hydro Generation")
                    .type(NodeType.GENERATION)
                    .voltageLevelKv(500.0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3200, 47.6500))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(generationNode);

            // Transmission Substation - Central North
            GridNode transmissionSubstation = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .name("Cedar Falls 500/230kV Substation")
                    .type(NodeType.SUBSTATION)
                    .voltageLevelKv(230.0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3300, 47.6200))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(transmissionSubstation);

            // Distribution Substation - Central
            GridNode distributionSubstation = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .name("Everett 230/115kV Substation")
                    .type(NodeType.SUBSTATION)
                    .voltageLevelKv(115.0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3100, 47.5900))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(distributionSubstation);

            // Switching Station 1 - West
            GridNode switchingStation1 = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .name("Snohomish Switching Station")
                    .type(NodeType.SWITCHING_STATION)
                    .voltageLevelKv(115.0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2800, 47.5800))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(switchingStation1);

            // Switching Station 2 - East
            GridNode switchingStation2 = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .name("Mukilteo Switching Station")
                    .type(NodeType.SWITCHING_STATION)
                    .voltageLevelKv(115.0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2500, 47.6000))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(switchingStation2);

            // Load Center - South
            GridNode loadCenter = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_LOAD_CENTER_ID))
                    .name("Everett Downtown Load Center")
                    .type(NodeType.LOAD)
                    .voltageLevelKv(12.47)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2900, 47.5700))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(loadCenter);

            // Industrial Load - Southeast
            GridNode industrialLoad = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_INDUSTRIAL_LOAD_ID))
                    .name("Boeing Everett Industrial Complex")
                    .type(NodeType.LOAD)
                    .voltageLevelKv(34.5)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2600, 47.5500))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(industrialLoad);

            // Rural Substation - North
            GridNode ruralSubstation = GridNode.builder()
                    .id(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .name("Granite Falls 115/12.47kV Substation")
                    .type(NodeType.SUBSTATION)
                    .voltageLevelKv(12.47)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.1500, 47.7000))))
                    .operator("Pacific Northwest Utilities")
                    .build();
            nodeRepository.save(ruralSubstation);

            // =====================================================================
            // 2. GRID EDGES - Create connectivity
            // =====================================================================

            // Generation to Transmission Substation (500kV line)
            GridEdge genToTrans = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_GEN_TO_TRANS_ID))
                    .name("Cedar Falls 500kV Line 1")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .voltageLevelKv(500.0)
                    .thermalRatingAmps(2000.0)
                    .lengthKm(15.2)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.3200, 47.6500),
                                    new Coordinate(-122.3250, 47.6450),
                                    new Coordinate(-122.3300, 47.6200)
                            })
                    ))
                    .conductorType("ACSR 795 kcmil")
                    .circuitCount(2)
                    .yearInstalled(1995)
                    .build();
            edgeRepository.save(genToTrans);

            // Transmission to Distribution Substation (230kV line)
            GridEdge transToDist = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_TRANS_TO_DIST_ID))
                    .name("Cedar-Everett 230kV Line 2")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .voltageLevelKv(230.0)
                    .thermalRatingAmps(1200.0)
                    .lengthKm(8.5)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.3300, 47.6200),
                                    new Coordinate(-122.3200, 47.6050),
                                    new Coordinate(-122.3100, 47.5900)
                            })
                    ))
                    .conductorType("ACSR 795 kcmil")
                    .circuitCount(2)
                    .yearInstalled(1998)
                    .build();
            edgeRepository.save(transToDist);

            // Transmission to Switching Station 1 (230kV line)
            GridEdge transToSwitch1 = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_TRANS_TO_SWITCH1_ID))
                    .name("Cedar-Snohomish 230kV Line 3")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .voltageLevelKv(230.0)
                    .thermalRatingAmps(1000.0)
                    .lengthKm(12.8)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.3300, 47.6200),
                                    new Coordinate(-122.3000, 47.6000),
                                    new Coordinate(-122.2800, 47.5800)
                            })
                    ))
                    .conductorType("ACSR 795 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2002)
                    .build();
            edgeRepository.save(transToSwitch1);

            // Switching Station 1 to Switching Station 2 (115kV line)
            GridEdge switch1ToSwitch2 = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_SWITCH1_TO_SWITCH2_ID))
                    .name("Snohomish-Mukilteo 115kV Line")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .voltageLevelKv(115.0)
                    .thermalRatingAmps(800.0)
                    .lengthKm(5.5)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.2800, 47.5800),
                                    new Coordinate(-122.2650, 47.5900),
                                    new Coordinate(-122.2500, 47.6000)
                            })
                    ))
                    .conductorType("ACSR 477 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2005)
                    .build();
            edgeRepository.save(switch1ToSwitch2);

            // Switching Station 2 to Load Center (115kV line)
            GridEdge switch2ToLoad = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_SWITCH2_TO_LOAD_ID))
                    .name("Mukilteo-Everett 115kV Line")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_LOAD_CENTER_ID))
                    .voltageLevelKv(115.0)
                    .thermalRatingAmps(800.0)
                    .lengthKm(4.2)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.2500, 47.6000),
                                    new Coordinate(-122.2700, 47.5850),
                                    new Coordinate(-122.2900, 47.5700)
                            })
                    ))
                    .conductorType("ACSR 477 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2008)
                    .build();
            edgeRepository.save(switch2ToLoad);

            // Switching Station 2 to Industrial Load (34.5kV line)
            GridEdge switch2ToIndustrial = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_SWITCH2_TO_INDUSTRIAL_ID))
                    .name("Mukilteo-Boeing 34.5kV Line")
                    .type(EdgeType.OVERHEAD_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_INDUSTRIAL_LOAD_ID))
                    .voltageLevelKv(34.5)
                    .thermalRatingAmps(400.0)
                    .lengthKm(3.1)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.2500, 47.6000),
                                    new Coordinate(-122.2550, 47.5750),
                                    new Coordinate(-122.2600, 47.5500)
                            })
                    ))
                    .conductorType("ACSR 336 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2010)
                    .build();
            edgeRepository.save(switch2ToIndustrial);

            // Distribution Substation to Rural Substation (115kV line)
            GridEdge distToRural = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_DIST_TO_RURAL_ID))
                    .name("Everett-Granite Falls 115kV Line")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .voltageLevelKv(115.0)
                    .thermalRatingAmps(600.0)
                    .lengthKm(25.3)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.3100, 47.5900),
                                    new Coordinate(-122.2500, 47.6300),
                                    new Coordinate(-122.2000, 47.6600),
                                    new Coordinate(-122.1500, 47.7000)
                            })
                    ))
                    .conductorType("ACSR 477 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2000)
                    .build();
            edgeRepository.save(distToRural);

            // Switching Station 1 to Rural Substation (115kV line - alternate path)
            GridEdge switch1ToRural = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.of(EDGE_SWITCH1_TO_RURAL_ID))
                    .name("Snohomish-Granite Falls 115kV Line")
                    .type(EdgeType.TRANSMISSION_LINE)
                    .fromNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .toNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .voltageLevelKv(115.0)
                    .thermalRatingAmps(600.0)
                    .lengthKm(18.7)
                    .geometry(CommonKernel.GeometryValueObject.of(
                            GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                                    new Coordinate(-122.2800, 47.5800),
                                    new Coordinate(-122.2300, 47.6200),
                                    new Coordinate(-122.1800, 47.6600),
                                    new Coordinate(-122.1500, 47.7000)
                            })
                    ))
                    .conductorType("ACSR 477 kcmil")
                    .circuitCount(1)
                    .yearInstalled(2012)
                    .build();
            edgeRepository.save(switch1ToRural);

            // =====================================================================
            // 3. ASSETS - Create realistic assets at nodes
            // =====================================================================

            // Generator 1 at Generation Node
            Asset generator1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_GENERATOR_1_ID))
                    .name("Cedar Falls Hydro Generator 1")
                    .type(AssetType.GENERATOR)
                    .criticality(CommonKernel.Criticality.MISSION_CRITICAL)
                    .healthIndex(CommonKernel.HealthIndex.of(0.92))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3200, 47.6500))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("Francis turbine, 120 MW, commissioned 1994")
                    .installedAt(Instant.parse("1994-06-15T00:00:00Z"))
                    .build();
            assetRepository.save(generator1);

            // Generator 2 at Generation Node
            Asset generator2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_GENERATOR_2_ID))
                    .name("Cedar Falls Hydro Generator 2")
                    .type(AssetType.GENERATOR)
                    .criticality(CommonKernel.Criticality.MISSION_CRITICAL)
                    .healthIndex(CommonKernel.HealthIndex.of(0.88))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3210, 47.6505))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("Francis turbine, 120 MW, commissioned 1995")
                    .installedAt(Instant.parse("1995-03-20T00:00:00Z"))
                    .build();
            assetRepository.save(generator2);

            // Main Transformer at Transmission Substation
            Asset transformerTrans = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_TRANSFORMER_TRANS_ID))
                    .name("Cedar Falls 500/230kV Main Transformer")
                    .type(AssetType.SUBSTATION)
                    .criticality(CommonKernel.Criticality.MISSION_CRITICAL)
                    .healthIndex(CommonKernel.HealthIndex.of(0.85))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3300, 47.6200))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("3-phase, 1500 MVA, ONAN/ONAF cooling, commissioned 1998")
                    .installedAt(Instant.parse("1998-11-10T00:00:00Z"))
                    .build();
            assetRepository.save(transformerTrans);

            // Main Transformer at Distribution Substation
            Asset transformerDist = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_TRANSFORMER_DIST_ID))
                    .name("Everett 230/115kV Main Transformer")
                    .type(AssetType.SUBSTATION)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.82))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3100, 47.5900))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("3-phase, 500 MVA, ONAN/ONAF cooling, commissioned 2001")
                    .installedAt(Instant.parse("2001-08-15T00:00:00Z"))
                    .build();
            assetRepository.save(transformerDist);

            // Transformer at Rural Substation
            Asset transformerRural = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_TRANSFORMER_RURAL_ID))
                    .name("Granite Falls 115/12.47kV Transformer")
                    .type(AssetType.DISTRIBUTION_TRANSFORMER)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.78))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.1500, 47.7000))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .description("3-phase, 50 MVA, ONAN cooling, commissioned 2003")
                    .installedAt(Instant.parse("2003-04-22T00:00:00Z"))
                    .build();
            assetRepository.save(transformerRural);

            // Breaker 1 at Switching Station 1
            Asset breaker1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_BREAKER_1_ID))
                    .name("Snohomish 230kV Breaker 101")
                    .type(AssetType.RECLOSER)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.90))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2800, 47.5800))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .description("SF6 circuit breaker, 230kV, 40kA, commissioned 2005")
                    .installedAt(Instant.parse("2005-07-12T00:00:00Z"))
                    .build();
            assetRepository.save(breaker1);

            // Breaker 2 at Switching Station 2
            Asset breaker2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_BREAKER_2_ID))
                    .name("Mukilteo 115kV Breaker 201")
                    .type(AssetType.RECLOSER)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.87))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2500, 47.6000))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .description("SF6 circuit breaker, 115kV, 31.5kA, commissioned 2008")
                    .installedAt(Instant.parse("2008-09-18T00:00:00Z"))
                    .build();
            assetRepository.save(breaker2);

            // Recloser 1 at Distribution Substation
            Asset recloser1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_RECLOSER_1_ID))
                    .name("Everett 115kV Recloser R-101")
                    .type(AssetType.RECLOSER)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.85))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3110, 47.5910))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("Electronic recloser, 115kV, commissioned 2015")
                    .installedAt(Instant.parse("2015-03-10T00:00:00Z"))
                    .build();
            assetRepository.save(recloser1);

            // Recloser 2 at Rural Substation
            Asset recloser2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_RECLOSER_2_ID))
                    .name("Granite Falls 12.47kV Recloser R-201")
                    .type(AssetType.RECLOSER)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.75))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.1510, 47.7010))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .description("Hydraulic recloser, 12.47kV, commissioned 2005")
                    .installedAt(Instant.parse("2005-11-20T00:00:00Z"))
                    .build();
            assetRepository.save(recloser2);

            // Capacitor Bank at Distribution Substation
            Asset capacitorBank = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_CAPACITOR_BANK_ID))
                    .name("Everett 12.47kV Capacitor Bank")
                    .type(AssetType.CAPACITOR_BANK)
                    .criticality(CommonKernel.Criticality.LOW)
                    .healthIndex(CommonKernel.HealthIndex.of(0.80))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3120, 47.5890))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("12.47kV, 10 MVAr, commissioned 2018")
                    .installedAt(Instant.parse("2018-06-15T00:00:00Z"))
                    .build();
            assetRepository.save(capacitorBank);

            // Voltage Regulator at Rural Substation
            Asset voltageRegulator = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_VOLTAGE_REGULATOR_ID))
                    .name("Granite Falls Voltage Regulator")
                    .type(AssetType.VOLTAGE_REGULATOR)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.72))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.1520, 47.7020))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .description("12.47kV, 32-step, commissioned 2010")
                    .installedAt(Instant.parse("2010-04-05T00:00:00Z"))
                    .build();
            assetRepository.save(voltageRegulator);

            // Battery Storage at Switching Station 1
            Asset batteryStorage = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_BATTERY_STORAGE_ID))
                    .name("Snohomish 10 MW Battery Storage")
                    .type(AssetType.BATTERY_STORAGE)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.95))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2790, 47.5790))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .description("Li-ion, 10 MW / 40 MWh, commissioned 2022")
                    .installedAt(Instant.parse("2022-01-15T00:00:00Z"))
                    .build();
            assetRepository.save(batteryStorage);

            // Solar Array at Load Center
            Asset solarArray = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_SOLAR_ARRAY_ID))
                    .name("Everett Community Solar Array")
                    .type(AssetType.SOLAR_ARRAY)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.98))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2910, 47.5690))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_LOAD_CENTER_ID))
                    .description("5 MW community solar, commissioned 2021")
                    .installedAt(Instant.parse("2021-09-20T00:00:00Z"))
                    .build();
            assetRepository.save(solarArray);

            // Wind Turbine at Generation Node
            Asset windTurbine = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_WIND_TURBINE_ID))
                    .name("Cedar Falls Wind Turbine 1")
                    .type(AssetType.WIND_TURBINE)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.90))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3190, 47.6490))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("3 MW onshore turbine, commissioned 2019")
                    .installedAt(Instant.parse("2019-10-01T00:00:00Z"))
                    .build();
            assetRepository.save(windTurbine);

            // Distribution Pole Transformers
            Asset distPoleTransformer1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_TRANSFORMER_DIST_POLE_1_ID))
                    .name("Everett Residential Pole Transformer T-1001")
                    .type(AssetType.DISTRIBUTION_TRANSFORMER)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.70))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2950, 47.5720))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_LOAD_CENTER_ID))
                    .description("25 kVA, pole-mount, commissioned 2005")
                    .installedAt(Instant.parse("2005-08-20T00:00:00Z"))
                    .build();
            assetRepository.save(distPoleTransformer1);

            Asset distPoleTransformer2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_TRANSFORMER_DIST_POLE_2_ID))
                    .name("Everett Commercial Pole Transformer T-1002")
                    .type(AssetType.DISTRIBUTION_TRANSFORMER)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.68))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2880, 47.5750))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_LOAD_CENTER_ID))
                    .description("75 kVA, pad-mount, commissioned 2008")
                    .installedAt(Instant.parse("2008-03-14T00:00:00Z"))
                    .build();
            assetRepository.save(distPoleTransformer2);

            // Control Center Asset
            Asset controlCenter = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_CONTROL_CENTER_ID))
                    .name("Seattle Grid Operations Control Center")
                    .type(AssetType.CONTROL_CENTER)
                    .criticality(CommonKernel.Criticality.MISSION_CRITICAL)
                    .healthIndex(CommonKernel.HealthIndex.of(0.98))
                    .redundancyLevel(2)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3320, 47.6060))))
                    .description("Primary grid operations center, SCADA/EMS, commissioned 2015")
                    .installedAt(Instant.parse("2015-12-01T00:00:00Z"))
                    .build();
            assetRepository.save(controlCenter);

            // RTU 1 at Transmission Substation
            Asset rtu1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_RTU_1_ID))
                    .name("Cedar Falls RTU-101")
                    .type(AssetType.RTU)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.93))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3310, 47.6190))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("SCADA RTU, IEC 60870-5-104, commissioned 2016")
                    .installedAt(Instant.parse("2016-02-20T00:00:00Z"))
                    .build();
            assetRepository.save(rtu1);

            // RTU 2 at Distribution Substation
            Asset rtu2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_RTU_2_ID))
                    .name("Everett RTU-201")
                    .type(AssetType.RTU)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.91))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3090, 47.5890))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("SCADA RTU, DNP3, commissioned 2017")
                    .installedAt(Instant.parse("2017-05-12T00:00:00Z"))
                    .build();
            assetRepository.save(rtu2);

            // Protection Relay at Transmission Substation
            Asset protectionRelay1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_PROTECTION_RELAY_1_ID))
                    .name("Cedar Falls 500kV Line Protection Relay")
                    .type(AssetType.PROTECTION_RELAY)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.94))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3305, 47.6195))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Distance protection relay, IEC 61850, commissioned 2018")
                    .installedAt(Instant.parse("2018-07-25T00:00:00Z"))
                    .build();
            assetRepository.save(protectionRelay1);

            // PMU at Generation Node
            Asset pmu1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_PMU_1_ID))
                    .name("Cedar Falls PMU-101")
                    .type(AssetType.PMU)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.96))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3195, 47.6495))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("Phasor Measurement Unit, 60 samples/sec, commissioned 2020")
                    .installedAt(Instant.parse("2020-03-10T00:00:00Z"))
                    .build();
            assetRepository.save(pmu1);

            // Physical Security Assets
            Asset perimeterFence = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_PERIMETER_FENCE_ID))
                    .name("Cedar Falls Substation Perimeter Fence")
                    .type(AssetType.PERIMETER_FENCE)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.85))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3290, 47.6180))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Anti-climb fence with sensors, commissioned 2019")
                    .installedAt(Instant.parse("2019-04-15T00:00:00Z"))
                    .build();
            assetRepository.save(perimeterFence);

            Asset cctv1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_CCTV_1_ID))
                    .name("Cedar Falls Substation CCTV Camera 1")
                    .type(AssetType.CCTV_CAMERA)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.88))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3300, 47.6175))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("PTZ camera with IR, commissioned 2020")
                    .installedAt(Instant.parse("2020-01-20T00:00:00Z"))
                    .build();
            assetRepository.save(cctv1);

            Asset cctv2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_CCTV_2_ID))
                    .name("Everett Substation CCTV Camera 1")
                    .type(AssetType.CCTV_CAMERA)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.86))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3105, 47.5885))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("Fixed dome camera, commissioned 2020")
                    .installedAt(Instant.parse("2020-02-15T00:00:00Z"))
                    .build();
            assetRepository.save(cctv2);

            Asset accessControl = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_ACCESS_CONTROL_ID))
                    .name("Cedar Falls Substation Access Control")
                    .type(AssetType.ACCESS_CONTROL)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.92))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3315, 47.6185))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Biometric + card access, commissioned 2021")
                    .installedAt(Instant.parse("2021-06-01T00:00:00Z"))
                    .build();
            assetRepository.save(accessControl);

            Asset intrusionDetection = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_INTRUSION_DETECTION_ID))
                    .name("Cedar Falls Substation Intrusion Detection")
                    .type(AssetType.INTRUSION_DETECTION)
                    .criticality(CommonKernel.Criticality.HIGH)
                    .healthIndex(CommonKernel.HealthIndex.of(0.90))
                    .redundancyLevel(1)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3325, 47.6170))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Fiber optic perimeter intrusion detection, commissioned 2021")
                    .installedAt(Instant.parse("2021-08-10T00:00:00Z"))
                    .build();
            assetRepository.save(intrusionDetection);

            // Environmental Monitoring
            Asset weatherStation1 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_WEATHER_STATION_1_ID))
                    .name("Cedar Falls Weather Station")
                    .type(AssetType.WEATHER_STATION)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.80))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3220, 47.6480))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("Full weather station: wind, temp, humidity, precip, commissioned 2018")
                    .installedAt(Instant.parse("2018-11-01T00:00:00Z"))
                    .build();
            assetRepository.save(weatherStation1);

            Asset weatherStation2 = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_WEATHER_STATION_2_ID))
                    .name("Everett Weather Station")
                    .type(AssetType.WEATHER_STATION)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.78))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3080, 47.5870))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("Compact weather station, commissioned 2019")
                    .installedAt(Instant.parse("2019-03-20T00:00:00Z"))
                    .build();
            assetRepository.save(weatherStation2);

            Asset seismicMonitor = Asset.builder()
                    .id(CommonKernel.AssetId.of(ASSET_SEISMIC_MONITOR_ID))
                    .name("Cedar Falls Seismic Monitor")
                    .type(AssetType.SEISMIC_MONITOR)
                    .criticality(CommonKernel.Criticality.MEDIUM)
                    .healthIndex(CommonKernel.HealthIndex.of(0.82))
                    .redundancyLevel(0)
                    .geometry(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3250, 47.6450))))
                    .gridNodeId(CommonKernel.GridNodeId.of(NODE_GENERATION_ID))
                    .description("Strong motion accelerometer, commissioned 2020")
                    .installedAt(Instant.parse("2020-09-01T00:00:00Z"))
                    .build();
            assetRepository.save(seismicMonitor);

            // =====================================================================
            // Overheating Transformer at Transmission Substation
            ThreatEvent overheatingTransformer = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_OVERHEATING_TRANSFORMER_ID))
                    .type(ThreatType.OVERHEATING_TRANSFORMER)
                    .severity(ThreatSeverity.HIGH)
                    .confidence(0.87)
                    .detectedAt(baseTime.minusSeconds(3600))
                    .firstObservedAt(baseTime.minusSeconds(7200))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3300, 47.6200))))
                    .affectedAssetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_TRANS_ID))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Main transformer top-oil temperature exceeding 105°C threshold")
                    .rationale("DGA shows increased acetylene; thermal imaging confirms hotspot on bushing")
                    .detectionStrategy("ThermalMonitoringStrategy")
                    .strategyVersion("2.1")
                    .build();
            threatRepository.save(overheatingTransformer);

            // Vegetation Encroachment on Transmission Line
            ThreatEvent vegetationLine = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_VEGETATION_LINE_ID))
                    .type(ThreatType.VEGETATION_ENCROACHMENT)
                    .severity(ThreatSeverity.MEDIUM)
                    .confidence(0.92)
                    .detectedAt(baseTime.minusSeconds(10800))
                    .firstObservedAt(baseTime.minusSeconds(86400))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3000, 47.6100))))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Vegetation within 3m of 230kV transmission line span")
                    .rationale("LiDAR survey shows vegetation within minimum approach distance on span 45-46")
                    .detectionStrategy("VegetationMonitoringStrategy")
                    .strategyVersion("1.5")
                    .build();
            threatRepository.save(vegetationLine);

            // Ice Accretion on Transmission Line
            ThreatEvent iceAccretion = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_ICE_ACCRETION_ID))
                    .type(ThreatType.ICE_ACCRETION)
                    .severity(ThreatSeverity.HIGH)
                    .confidence(0.81)
                    .detectedAt(baseTime.minusSeconds(7200))
                    .firstObservedAt(baseTime.minusSeconds(18000))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2000, 47.6600))))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .description("Ice accretion exceeding 15mm on 115kV line to rural substation")
                    .rationale("Weather station reports freezing rain; line tension sensors show 15% increase")
                    .detectionStrategy("WeatherImpactStrategy")
                    .strategyVersion("1.2")
                    .build();
            threatRepository.save(iceAccretion);

            // Corrosion on Transmission Tower
            ThreatEvent corrosionTower = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_CORROSION_TOWER_ID))
                    .type(ThreatType.STRUCTURAL_CORROSION)
                    .severity(ThreatSeverity.MEDIUM)
                    .confidence(0.76)
                    .detectedAt(baseTime.minusSeconds(86400))
                    .firstObservedAt(baseTime.minusSeconds(172800))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2800, 47.5750))))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_1_ID))
                    .description("Corrosion detected on transmission tower leg at base plate")
                    .rationale("Drone inspection shows 15% section loss on leg 3 base connection")
                    .detectionStrategy("DroneInspectionStrategy")
                    .strategyVersion("2.0")
                    .build();
            threatRepository.save(corrosionTower);

            // Physical Intrusion at Substation
            ThreatEvent physicalIntrusion = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_PHYSICAL_INTRUSION_ID))
                    .type(ThreatType.PHYSICAL_INTRUSION)
                    .severity(ThreatSeverity.HIGH)
                    .confidence(0.95)
                    .detectedAt(baseTime.minusSeconds(1800))
                    .firstObservedAt(baseTime.minusSeconds(1800))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3295, 47.6185))))
                    .affectedAssetId(CommonKernel.AssetId.of(ASSET_PERIMETER_FENCE_ID))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .description("Perimeter fence breach detected at Sector 3")
                    .rationale("Fiber optic intrusion detection triggered; CCTV shows two individuals cutting fence")
                    .detectionStrategy("PhysicalSecurityStrategy")
                    .strategyVersion("1.8")
                    .build();
            threatRepository.save(physicalIntrusion);

            // Cyber Intrusion Attempt on RTU
            ThreatEvent cyberIntrusion = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_CYBER_INTRUSION_ID))
                    .type(ThreatType.CYBER_INTRUSION)
                    .severity(ThreatSeverity.CRITICAL)
                    .confidence(0.73)
                    .detectedAt(baseTime.minusSeconds(3600))
                    .firstObservedAt(baseTime.minusSeconds(7200))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3100, 47.5900))))
                    .affectedAssetId(CommonKernel.AssetId.of(ASSET_RTU_2_ID))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .description("Anomalous Modbus traffic pattern on RTU-201")
                    .rationale("IDS detected reconnaissance scan from unknown IP; potential credential stuffing")
                    .detectionStrategy("CyberSecurityStrategy")
                    .strategyVersion("3.1")
                    .build();
            threatRepository.save(cyberIntrusion);

            // Overload on Distribution Line
            ThreatEvent overloadLine = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.of(THREAT_OVERLOAD_LINE_ID))
                    .type(ThreatType.OVERLOAD)
                    .severity(ThreatSeverity.HIGH)
                    .confidence(0.89)
                    .detectedAt(baseTime.minusSeconds(5400))
                    .firstObservedAt(baseTime.minusSeconds(10800))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2650, 47.5875))))
                    .nearbyGridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .description("34.5kV line to industrial load at 115% thermal rating")
                    .rationale("Boeing facility startup caused 15-min overload; line sag monitoring triggered")
                    .detectionStrategy("ThermalMonitoringStrategy")
                    .strategyVersion("2.1")
                    .build();
            threatRepository.save(overloadLine);

            // =====================================================================
            // 5. INCIDENTS - Create incidents linked to threats
            // =====================================================================

            // Incident for Overheating Transformer
            Incident incidentOverheating = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_OVERHEATING_ID))
                    .incidentNumber("INC-2026-000123")
                    .severity(ThreatSeverity.HIGH)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_OVERHEATING_TRANSFORMER_ID))
                    .primaryAssetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_TRANS_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3300, 47.6200))))
                    .description("Main transformer overheating - emergency cooling initiated")
                    .detectedAt(baseTime.minusSeconds(3600))
                    .assignedOperatorId(CommonKernel.UserId.generate()) // Will be set by test
                    .incidentCommanderId(CommonKernel.UserId.generate())
                    .build();
            incidentRepository.save(incidentOverheating);

            // Incident for Vegetation Encroachment
            Incident incidentVegetation = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_VEGETATION_ID))
                    .incidentNumber("INC-2026-000124")
                    .severity(ThreatSeverity.MEDIUM)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_VEGETATION_LINE_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3000, 47.6100))))
                    .description("Vegetation encroachment on 230kV line - crew dispatched for clearance")
                    .detectedAt(baseTime.minusSeconds(10800))
                    .build();
            incidentRepository.save(incidentVegetation);

            // Incident for Ice Accretion
            Incident incidentIce = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_ICE_ACCRETION_ID))
                    .incidentNumber("INC-2026-000125")
                    .severity(ThreatSeverity.HIGH)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_ICE_ACCRETION_ID))
                    .primaryAssetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_RURAL_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_RURAL_SUB_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2000, 47.6600))))
                    .description("Ice accretion on 115kV line - de-icing crew dispatched")
                    .detectedAt(baseTime.minusSeconds(7200))
                    .build();
            incidentRepository.save(incidentIce);

            // Incident for Physical Intrusion
            Incident incidentPhysical = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_PHYSICAL_INTRUSION_ID))
                    .incidentNumber("INC-2026-000126")
                    .severity(ThreatSeverity.HIGH)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_PHYSICAL_INTRUSION_ID))
                    .primaryAssetId(CommonKernel.AssetId.of(ASSET_PERIMETER_FENCE_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_TRANSMISSION_SUB_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3295, 47.6185))))
                    .description("Perimeter fence breach - security and law enforcement responding")
                    .detectedAt(baseTime.minusSeconds(1800))
                    .build();
            incidentRepository.save(incidentPhysical);

            // Incident for Cyber Intrusion
            Incident incidentCyber = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_CYBER_INTRUSION_ID))
                    .incidentNumber("INC-2026-000127")
                    .severity(ThreatSeverity.CRITICAL)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_CYBER_INTRUSION_ID))
                    .primaryAssetId(CommonKernel.AssetId.of(ASSET_RTU_2_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_DISTRIBUTION_SUB_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.3100, 47.5900))))
                    .description("Cyber intrusion attempt on RTU-201 - SOC activated")
                    .detectedAt(baseTime.minusSeconds(3600))
                    .build();
            incidentRepository.save(incidentCyber);

            // Incident for Overload
            Incident incidentOverload = Incident.builder()
                    .id(CommonKernel.IncidentId.of(INCIDENT_OVERLOAD_ID))
                    .incidentNumber("INC-2026-000128")
                    .severity(ThreatSeverity.HIGH)
                    .originatingThreatId(CommonKernel.ThreatEventId.of(THREAT_OVERLOAD_LINE_ID))
                    .primaryGridNodeId(CommonKernel.GridNodeId.of(NODE_SWITCHING_2_ID))
                    .location(CommonKernel.GeometryValueObject.of(GEOMETRY_FACTORY.createPoint(new Coordinate(-122.2650, 47.5875))))
                    .description("34.5kV line overload - load shedding initiated for Boeing facility")
                    .detectedAt(baseTime.minusSeconds(5400))
                    .build();
            incidentRepository.save(incidentOverload);

            // =====================================================================
            // 6. RISK SCORES - Create meaningful risk scores for key assets
            // =====================================================================

            // Risk score for Generator 1
            RiskScore riskGenerator1 = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_GENERATOR_1_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_GENERATOR_1_ID))
                    .score(0.15)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.35, 1.0, 0.35, "Mission critical asset"))
                            .addFactor("health", FactorContribution.of(0.10, 0.92, 0.10, "Excellent health (0.92)"))
                            .addFactor("redundancy", FactorContribution.of(0.20, 1.0, 0.20, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.15, 0.1, 0.15, "Low threat exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.20, 0.2, 0.20, "Low vulnerability"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskGenerator1);

            // Risk score for Generator 2
            RiskScore riskGenerator2 = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_GENERATOR_2_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_GENERATOR_2_ID))
                    .score(0.18)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.35, 1.0, 0.35, "Mission critical asset"))
                            .addFactor("health", FactorContribution.of(0.15, 0.88, 0.15, "Good health (0.88)"))
                            .addFactor("redundancy", FactorContribution.of(0.20, 1.0, 0.20, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.15, 0.1, 0.15, "Low threat exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.15, 0.25, 0.15, "Low vulnerability"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskGenerator2);

            // Risk score for Transmission Transformer
            RiskScore riskTransformerTrans = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_TRANSFORMER_TRANS_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_TRANS_ID))
                    .score(0.68)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.30, 1.0, 0.30, "Mission critical asset"))
                            .addFactor("health", FactorContribution.of(0.15, 0.85, 0.15, "Good health (0.85)"))
                            .addFactor("redundancy", FactorContribution.of(0.15, 1.0, 0.15, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.25, 1.0, 0.25, "Active overheating threat"))
                            .addFactor("vulnerability", FactorContribution.of(0.15, 0.5, 0.15, "Aging transformer"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskTransformerTrans);

            // Risk score for Distribution Transformer
            RiskScore riskTransformerDist = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_TRANSFORMER_DIST_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_DIST_ID))
                    .score(0.42)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.30, 0.8, 0.30, "High criticality asset"))
                            .addFactor("health", FactorContribution.of(0.20, 0.82, 0.20, "Fair health (0.82)"))
                            .addFactor("redundancy", FactorContribution.of(0.15, 1.0, 0.15, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.15, 0.2, 0.15, "Low threat exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.20, 0.4, 0.20, "Aging transformer"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskTransformerDist);

            // Risk score for Rural Transformer
            RiskScore riskTransformerRural = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_TRANSFORMER_RURAL_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_TRANSFORMER_RURAL_ID))
                    .score(0.55)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.25, 0.8, 0.25, "High criticality asset"))
                            .addFactor("health", FactorContribution.of(0.20, 0.78, 0.20, "Degraded health (0.78)"))
                            .addFactor("redundancy", FactorContribution.of(0.25, 0.0, 0.25, "No redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.20, 0.8, 0.20, "Ice accretion threat"))
                            .addFactor("vulnerability", FactorContribution.of(0.10, 0.4, 0.10, "Aging, no redundancy"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskTransformerRural);

            // Risk score for Battery Storage
            RiskScore riskBattery = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_BATTERY_STORAGE_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_BATTERY_STORAGE_ID))
                    .score(0.12)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.30, 0.8, 0.30, "High criticality asset"))
                            .addFactor("health", FactorContribution.of(0.25, 0.95, 0.25, "Excellent health (0.95)"))
                            .addFactor("redundancy", FactorContribution.of(0.20, 1.0, 0.20, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.15, 0.1, 0.15, "Low threat exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.10, 0.1, 0.10, "New installation"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskBattery);

            // Risk score for Solar Array
            RiskScore riskSolar = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_SOLAR_ARRAY_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_SOLAR_ARRAY_ID))
                    .score(0.18)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.30, 0.8, 0.30, "High criticality asset"))
                            .addFactor("health", FactorContribution.of(0.25, 0.98, 0.25, "Excellent health (0.98)"))
                            .addFactor("redundancy", FactorContribution.of(0.20, 1.0, 0.20, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.15, 0.1, 0.15, "Low threat exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.10, 0.3, 0.10, "Weather dependent"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskSolar);

            // Risk score for Wind Turbine
            RiskScore riskWind = RiskScore.builder()
                    .id(CommonKernel.RiskScoreId.of(RISK_WIND_TURBINE_ID))
                    .assetId(CommonKernel.AssetId.of(ASSET_WIND_TURBINE_ID))
                    .score(0.22)
                    .modelName("GridRiskModel")
                    .modelVersion("2.3")
                    .strategyName("CriticalityHealthRedundancyStrategy")
                    .factorBreakdown(FactorBreakdown.builder()
                            .addFactor("criticality", FactorContribution.of(0.30, 0.8, 0.30, "High criticality asset"))
                            .addFactor("health", FactorContribution.of(0.15, 0.90, 0.15, "Good health (0.90)"))
                            .addFactor("redundancy", FactorContribution.of(0.20, 1.0, 0.20, "N+1 redundancy"))
                            .addFactor("threatExposure", FactorContribution.of(0.20, 0.3, 0.20, "Weather exposure"))
                            .addFactor("vulnerability", FactorContribution.of(0.15, 0.4, 0.15, "Mechanical wear"))
                            .build())
                    .computedAt(baseTime)
                    .build();
            riskScoreRepository.save(riskWind);

            System.out.println("=== Spatial Demo Data Initialization Complete ===");
            System.out.println("Nodes: " + nodeRepository.count());
            System.out.println("Edges: " + edgeRepository.count());
            System.out.println("Assets: " + assetRepository.count());
            System.out.println("Threats: " + threatRepository.count());
            System.out.println("Incidents: " + incidentRepository.count());
            System.out.println("Risk Scores: " + riskScoreRepository.count());
        };
    }
}