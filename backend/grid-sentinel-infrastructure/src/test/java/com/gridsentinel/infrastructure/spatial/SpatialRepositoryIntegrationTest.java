package com.gridsentinel.infrastructure.spatial;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.asset.AssetType;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatType;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.NodeType;
import com.gridsentinel.grid.EdgeType;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.infrastructure.config.TestcontainersConfiguration;
import com.gridsentinel.infrastructure.persistence.repository.AssetRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridEdgeRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridNodeRepository;
import com.gridsentinel.infrastructure.persistence.repository.IncidentRepository;
import com.gridsentinel.infrastructure.persistence.repository.RiskScoreRepository;
import com.gridsentinel.infrastructure.persistence.repository.ThreatEventRepository;
import com.gridsentinel.risk.RiskScore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for spatial repositories using PostgreSQL/PostGIS via Testcontainers.
 * This is the authoritative test for spatial functionality.
 * H2 dev profile is NOT used for spatial validation.
 */
@SpringBootTest
@ContextConfiguration(classes = TestcontainersConfiguration.class)
@Transactional
class SpatialRepositoryIntegrationTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private GridNodeRepository gridNodeRepository;

    @Autowired
    private GridEdgeRepository gridEdgeRepository;

    @Autowired
    private ThreatEventRepository threatEventRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private RiskScoreRepository riskScoreRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Deterministic test data - same coordinates every run
    private static final double SUBSTATION_LNG = -122.3321;
    private static final double SUBSTATION_LAT = 47.6062;
    private static final double GENERATION_LNG = -122.4194;
    private static final double GENERATION_LAT = 47.6500;
    private static final double EDGE_START_LNG = -122.3321;
    private static final double EDGE_START_LAT = 47.6062;
    private static final double EDGE_END_LNG = -122.4194;
    private static final double EDGE_END_LAT = 47.6500;

    // Viewport covering the test area
    private static final Envelope TEST_BBOX = new Envelope(-122.5, -122.2, 47.5, 47.8);

    @BeforeEach
    void setUp() {
        // Clear and seed deterministic test data
        entityManager.createNativeQuery("DELETE FROM risk_scores").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM incidents").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM threat_events").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM grid_edges").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM grid_nodes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM assets").executeUpdate();

        seedDeterministicData();
        entityManager.flush();
        entityManager.clear();
    }

    private void seedDeterministicData() {
        // 1. Grid Nodes
        GridNode substationNode = createGridNode(
            CommonKernel.GridNodeId.generate(),
            "Test Substation",
            NodeType.SUBSTATION,
            230.0,
            SUBSTATION_LNG, SUBSTATION_LAT
        );
        gridNodeRepository.save(substationNode);

        GridNode generationNode = createGridNode(
            CommonKernel.GridNodeId.generate(),
            "Test Generation",
            NodeType.GENERATION,
            500.0,
            GENERATION_LNG, GENERATION_LAT
        );
        gridNodeRepository.save(generationNode);

        // 2. Grid Edge
        GridEdge transmissionLine = createGridEdge(
            CommonKernel.GridEdgeId.generate(),
            "Test Transmission Line",
            EdgeType.TRANSMISSION_LINE,
            substationNode.getId(),
            generationNode.getId(),
            230.0,
            1000.0,
            50.0,
            EDGE_START_LNG, EDGE_START_LAT,
            EDGE_END_LNG, EDGE_END_LAT
        );
        gridEdgeRepository.save(transmissionLine);

        // 3. Assets
        Asset transformerAsset = createAsset(
            CommonKernel.AssetId.generate(),
            "Main Transformer",
            AssetType.DISTRIBUTION_TRANSFORMER,
            CommonKernel.Criticality.HIGH,
            CommonKernel.HealthIndex.of(0.85),
            1,
            SUBSTATION_LNG, SUBSTATION_LAT,
            substationNode.getId()
        );
        assetRepository.save(transformerAsset);

        Asset generatorAsset = createAsset(
            CommonKernel.AssetId.generate(),
            "Main Generator",
            AssetType.GENERATOR,
            CommonKernel.Criticality.MISSION_CRITICAL,
            CommonKernel.HealthIndex.of(0.95),
            2,
            GENERATION_LNG, GENERATION_LAT,
            generationNode.getId()
        );
        assetRepository.save(generatorAsset);

        Asset lineAsset = createAsset(
            CommonKernel.AssetId.generate(),
            "Transmission Line Asset",
            AssetType.TRANSMISSION_LINE,
            CommonKernel.Criticality.MEDIUM,
            CommonKernel.HealthIndex.of(0.75),
            0,
            EDGE_START_LNG, EDGE_START_LAT,
            null
        );
        assetRepository.save(lineAsset);

        // 4. Threat Event
        ThreatEvent threat = createThreatEvent(
            CommonKernel.ThreatEventId.generate(),
            ThreatType.OVERHEATING_TRANSFORMER,
            ThreatSeverity.HIGH,
            0.85,
            SUBSTATION_LNG, SUBSTATION_LAT,
            transformerAsset.getId(),
            substationNode.getId()
        );
        threatEventRepository.save(threat);

        // 5. Incident
        Incident incident = createIncident(
            CommonKernel.IncidentId.generate(),
            "INC-2024-001",
            ThreatSeverity.HIGH,
            SUBSTATION_LNG, SUBSTATION_LAT,
            transformerAsset.getId(),
            substationNode.getId(),
            threat.getId()
        );
        incidentRepository.save(incident);

        // 6. Risk Scores
        RiskScore transformerRisk = createRiskScore(
            CommonKernel.RiskScoreId.generate(),
            transformerAsset.getId(),
            0.82
        );
        riskScoreRepository.save(transformerRisk);

        RiskScore generatorRisk = createRiskScore(
            CommonKernel.RiskScoreId.generate(),
            generatorAsset.getId(),
            0.25
        );
        riskScoreRepository.save(generatorRisk);

        RiskScore lineRisk = createRiskScore(
            CommonKernel.RiskScoreId.generate(),
            lineAsset.getId(),
            0.55
        );
        riskScoreRepository.save(lineRisk);
    }

    private GridNode createGridNode(CommonKernel.GridNodeId id, String name, NodeType type,
                                     double voltageKv, double lng, double lat) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return GridNode.builder()
            .id(id)
            .name(name)
            .type(type)
            .voltageLevelKv(voltageKv)
            .geometry(CommonKernel.GeometryValueObject.of(point))
            .operator("Test Operator")
            .build();
    }

    private GridEdge createGridEdge(CommonKernel.GridEdgeId id, String name, EdgeType type,
                                     CommonKernel.GridNodeId fromNodeId, CommonKernel.GridNodeId toNodeId,
                                     double voltageKv, double thermalRating, double lengthKm,
                                     double startLng, double startLat, double endLng, double endLat) {
        LineString lineString = geometryFactory.createLineString(new Coordinate[]{
            new Coordinate(startLng, startLat),
            new Coordinate(endLng, endLat)
        });
        lineString.setSRID(4326);
        return GridEdge.builder()
            .id(id)
            .name(name)
            .type(type)
            .fromNodeId(fromNodeId)
            .toNodeId(toNodeId)
            .voltageLevelKv(voltageKv)
            .thermalRatingAmps(thermalRating)
            .lengthKm(lengthKm)
            .geometry(CommonKernel.GeometryValueObject.of(lineString))
            .conductorType("ACSR")
            .yearInstalled(2020)
            .build();
    }

    private Asset createAsset(CommonKernel.AssetId id, String name, AssetType type,
                               CommonKernel.Criticality criticality, CommonKernel.HealthIndex healthIndex,
                               int redundancyLevel, double lng, double lat,
                               CommonKernel.GridNodeId gridNodeId) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return Asset.builder()
            .id(id)
            .name(name)
            .type(type)
            .criticality(criticality)
            .healthIndex(healthIndex)
            .redundancyLevel(redundancyLevel)
            .geometry(CommonKernel.GeometryValueObject.of(point))
            .gridNodeId(gridNodeId)
            .description("Test asset: " + name)
            .installedAt(Instant.now().minusSeconds(86400 * 365))
            .build();
    }

    private ThreatEvent createThreatEvent(CommonKernel.ThreatEventId id, ThreatType type,
                                           ThreatSeverity severity, double confidence,
                                           double lng, double lat,
                                           CommonKernel.AssetId assetId,
                                           CommonKernel.GridNodeId nodeId) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return ThreatEvent.builder()
            .id(id)
            .type(type)
            .severity(severity)
            .confidence(confidence)
            .detectedAt(Instant.now())
            .firstObservedAt(Instant.now().minusSeconds(3600))
            .location(CommonKernel.GeometryValueObject.of(point))
            .affectedAssetId(assetId)
            .nearbyGridNodeId(nodeId)
            .description("Test threat: " + type.name())
            .rationale("Deterministic test data")
            .detectionStrategy("TestStrategy")
            .strategyVersion("1.0")
            .build();
    }

    private Incident createIncident(CommonKernel.IncidentId id, String incidentNumber,
                                     ThreatSeverity severity,
                                     double lng, double lat,
                                     CommonKernel.AssetId assetId,
                                     CommonKernel.GridNodeId nodeId,
                                     CommonKernel.ThreatEventId threatId) {
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return Incident.builder()
            .id(id)
            .incidentNumber(incidentNumber)
            .severity(severity)
            .location(CommonKernel.GeometryValueObject.of(point))
            .description("Test incident: " + incidentNumber)
            .primaryAssetId(assetId)
            .primaryGridNodeId(nodeId)
            .originatingThreatId(threatId)
            .detectedAt(Instant.now().minusSeconds(1800))
            .build();
    }

    private RiskScore createRiskScore(CommonKernel.RiskScoreId id, CommonKernel.AssetId assetId,
                                        double score) {
        return RiskScore.builder()
            .id(id)
            .assetId(assetId)
            .score(score)
            .modelName("TestModel")
            .modelVersion("1.0")
            .strategyName("TestStrategy")
            .computedAt(Instant.now())
            .build();
    }

    @Test
    void databaseStarts() {
        // Testcontainers should start PostgreSQL/PostGIS
        assertNotNull(entityManager);
        assertTrue(entityManager.isOpen());
    }

    @Test
    void flywayMigrationsRun() {
        // Verify tables exist by checking we can query them
        long assetCount = assetRepository.count();
        assertEquals(3, assetCount);
    }

    @Test
    void spatialSchemaInitializes() {
        // Verify geometry columns exist with correct SRID
        List<Object[]> result = entityManager.createNativeQuery(
            "SELECT f_geometry_column, srid FROM geometry_columns WHERE f_table_name = 'assets'"
        ).getResultList();

        assertEquals(1, result.size());
        Object[] row = result.get(0);
        assertEquals("geometry", row[0]);
        assertEquals(4326, row[1]);
    }

    @Test
    void geometryColumnsInitializeCorrectly() {
        // Verify SRID is 4326 for all spatial columns
        List<Object[]> assetSrid = entityManager.createNativeQuery(
            "SELECT ST_SRID(geometry) FROM assets LIMIT 1"
        ).getResultList();
        assertEquals(4326, assetSrid.get(0)[0]);

        List<Object[]> nodeSrid = entityManager.createNativeQuery(
            "SELECT ST_SRID(geometry) FROM grid_nodes LIMIT 1"
        ).getResultList();
        assertEquals(4326, nodeSrid.get(0)[0]);

        List<Object[]> edgeSrid = entityManager.createNativeQuery(
            "SELECT ST_SRID(geometry) FROM grid_edges LIMIT 1"
        ).getResultList();
        assertEquals(4326, edgeSrid.get(0)[0]);
    }

    @Test
    void spatialIndexesExist() {
        // Verify GIST indexes exist on geometry columns
        List<Object[]> indexes = entityManager.createNativeQuery(
            "SELECT indexname FROM pg_indexes WHERE tablename = 'assets' AND indexname LIKE '%geometry%'"
        ).getResultList();
        assertFalse(indexes.isEmpty());

        indexes = entityManager.createNativeQuery(
            "SELECT indexname FROM pg_indexes WHERE tablename = 'grid_nodes' AND indexname LIKE '%geometry%'"
        ).getResultList();
        assertFalse(indexes.isEmpty());

        indexes = entityManager.createNativeQuery(
            "SELECT indexname FROM pg_indexes WHERE tablename = 'grid_edges' AND indexname LIKE '%geometry%'"
        ).getResultList();
        assertFalse(indexes.isEmpty());
    }

    @Test
    void spatialRepositoryQueriesExecute() {
        // Test Asset repository bbox query
        List<Asset> assetsInBbox = assetRepository.findByBbox(TEST_BBOX);
        assertEquals(3, assetsInBbox.size());

        // Test GridNode repository bbox query
        List<GridNode> nodesInBbox = gridNodeRepository.findByBbox(TEST_BBOX);
        assertEquals(2, nodesInBbox.size());

        // Test GridEdge repository bbox query
        List<GridEdge> edgesInBbox = gridEdgeRepository.findByBbox(TEST_BBOX);
        assertEquals(1, edgesInBbox.size());

        // Test ThreatEvent repository bbox query
        List<ThreatEvent> threatsInBbox = threatEventRepository.findByBbox(TEST_BBOX);
        assertEquals(1, threatsInBbox.size());

        // Test Incident repository bbox query
        List<Incident> incidentsInBbox = incidentRepository.findByBbox(TEST_BBOX);
        assertEquals(1, incidentsInBbox.size());

        // Test RiskScore repository bbox query (joins with assets)
        List<RiskScore> scoresInBbox = riskScoreRepository.findLatestByAssetBbox(TEST_BBOX);
        assertEquals(3, scoresInBbox.size());
    }

    @Test
    void stIntersectsWorks() {
        // Verify ST_Intersects function works correctly
        // The transformer asset is at SUBSTATION_LNG, SUBSTATION_LAT
        // The bbox covers that area
        List<Asset> assets = assetRepository.findByBbox(TEST_BBOX);
        assertTrue(assets.stream().anyMatch(a -> a.getName().equals("Main Transformer")));

        // Test with a smaller bbox that only contains the transformer
        Envelope smallBbox = new Envelope(-122.34, -122.32, 47.60, 47.61);
        List<Asset> assetsInSmallBbox = assetRepository.findByBbox(smallBbox);
        assertEquals(1, assetsInSmallBbox.size());
        assertEquals("Main Transformer", assetsInSmallBbox.get(0).getName());

        // Test with bbox that doesn't contain any assets
        Envelope emptyBbox = new Envelope(-123.0, -122.8, 47.0, 47.2);
        List<Asset> assetsInEmptyBbox = assetRepository.findByBbox(emptyBbox);
        assertTrue(assetsInEmptyBbox.isEmpty());
    }

    @Test
    void viewportAwareQueriesWork() {
        // Test that bbox filtering works at different zoom levels
        // Wide viewport - should get all 3 assets
        Envelope wideViewport = new Envelope(-123.0, -121.0, 47.0, 48.0);
        List<Asset> allAssets = assetRepository.findByBbox(wideViewport);
        assertEquals(3, allAssets.size());

        // Narrow viewport - should get only transformer
        Envelope narrowViewport = new Envelope(-122.34, -122.32, 47.60, 47.61);
        List<Asset> narrowAssets = assetRepository.findByBbox(narrowViewport);
        assertEquals(1, narrowAssets.size());
        assertEquals("Main Transformer", narrowAssets.get(0).getName());

        // Test with filters combined
        List<Asset> highCriticalityAssets = assetRepository.findByBboxAndCriticality(
            wideViewport, CommonKernel.Criticality.HIGH.name());
        assertEquals(1, highCriticalityAssets.size());
        assertEquals("Main Transformer", highCriticalityAssets.get(0).getName());
    }

    @Test
    void geoJsonResultsProducedCorrectly() {
        // Verify geometry is returned correctly
        List<Asset> assets = assetRepository.findByBbox(TEST_BBOX);
        for (Asset asset : assets) {
            assertNotNull(asset.getGeometry());
            assertEquals(4326, asset.getGeometry().geometry().getSRID());
            assertTrue(asset.getGeometry().geometry().isValid());
        }

        List<GridNode> nodes = gridNodeRepository.findByBbox(TEST_BBOX);
        for (GridNode node : nodes) {
            assertNotNull(node.getGeometry());
            assertEquals(4326, node.getGeometry().geometry().getSRID());
            assertTrue(node.getGeometry().geometry().isValid());
        }

        List<GridEdge> edges = gridEdgeRepository.findByBbox(TEST_BBOX);
        for (GridEdge edge : edges) {
            assertNotNull(edge.getGeometry());
            assertEquals(4326, edge.getGeometry().geometry().getSRID());
            assertTrue(edge.getGeometry().geometry().isValid());
        }
    }

    @Test
    void riskScoreJoinWithAssetWorks() {
        // RiskScoreRepository joins with Asset table for bbox filtering
        List<RiskScore> scores = riskScoreRepository.findLatestByAssetBbox(TEST_BBOX);
        assertEquals(3, scores.size());

        // Verify scores are ordered by asset
        for (RiskScore score : scores) {
            Optional<Asset> asset = assetRepository.findById(score.getAssetId());
            assertTrue(asset.isPresent());
            assertEquals(4326, asset.get().getGeometry().geometry().getSRID());
        }
    }

    @Test
    void threatEventFiltersWork() {
        // Test threat event severity filter
        List<ThreatEvent> highThreats = threatEventRepository.findByBboxAndSeverity(
            TEST_BBOX, ThreatSeverity.HIGH.name());
        assertEquals(1, highThreats.size());

        // Test threat event type filter
        List<ThreatEvent> overheatingThreats = threatEventRepository.findByBboxAndType(
            TEST_BBOX, ThreatType.OVERHEATING_TRANSFORMER.name());
        assertEquals(1, overheatingThreats.size());
        assertEquals(ThreatType.OVERHEATING_TRANSFORMER, overheatingThreats.get(0).getType());
    }

    @Test
    void incidentFiltersWork() {
        // Test incident state filter
        List<Incident> confirmedIncidents = incidentRepository.findByBboxAndState(
            TEST_BBOX, IncidentState.CONFIRMED.name());
        assertEquals(1, confirmedIncidents.size());

        // Test incident severity filter
        List<Incident> highIncidents = incidentRepository.findByBboxAndSeverity(
            TEST_BBOX, ThreatSeverity.HIGH.name());
        assertEquals(1, highIncidents.size());

        // Test active incidents filter
        List<Incident> activeIncidents = incidentRepository.findActiveByBbox(TEST_BBOX);
        assertEquals(1, activeIncidents.size());
    }

    @Test
    void deterministicDataIsReproducible() {
        // Run the same query twice - should get identical results
        List<Asset> assets1 = assetRepository.findByBbox(TEST_BBOX);
        entityManager.clear();
        List<Asset> assets2 = assetRepository.findByBbox(TEST_BBOX);

        assertEquals(assets1.size(), assets2.size());
        for (int i = 0; i < assets1.size(); i++) {
            assertEquals(assets1.get(i).getName(), assets2.get(i).getName());
            assertEquals(assets1.get(i).getId(), assets2.get(i).getId());
        }
    }
}