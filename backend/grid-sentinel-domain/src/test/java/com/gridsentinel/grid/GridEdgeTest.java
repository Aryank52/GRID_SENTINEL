package com.gridsentinel.grid;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.EdgeType;
import com.gridsentinel.test.TestGeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class GridEdgeTest {

    @Test
    void gridEdgeCreation() {
        CommonKernel.GridEdgeId id = CommonKernel.GridEdgeId.generate();
        CommonKernel.GridNodeId sourceId = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId targetId = CommonKernel.GridNodeId.generate();
        // GridEdge requires LineString geometry
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.lineString(new double[][]{
                {0, 0}, {10, 0}, {10, 10}, {0, 10}
        });

        GridEdge edge = GridEdge.builder()
                .id(id)
                .name("Test Line")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(sourceId)
                .toNodeId(targetId)
                .geometry(geometry)
                .lengthKm(50.5)
                .thermalRatingAmps(1000)
                .voltageLevelKv(220)
                .build();

        assertThat(edge.getId()).isEqualTo(id);
        assertThat(edge.getName()).isEqualTo("Test Line");
        assertThat(edge.getType()).isEqualTo(EdgeType.TRANSMISSION_LINE);
        assertThat(edge.getFromNodeId()).isEqualTo(sourceId);
        assertThat(edge.getToNodeId()).isEqualTo(targetId);
        assertThat(edge.getGeometry()).isEqualTo(geometry);
        assertThat(edge.getLengthKm()).isEqualTo(50.5);
        assertThat(edge.getThermalRatingAmps()).isEqualTo(1000);
        assertThat(edge.getVoltageLevelKv()).isEqualTo(220);
        assertThat(edge.getVersion()).isEqualTo(0);
    }

    @Test
    void gridEdgeNameCannotBeNull() {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name(null)
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridEdgeTypeCannotBeNull() {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(null)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridEdgeSourceCannotBeNull() {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(null)
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridEdgeTargetCannotBeNull() {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(null)
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridEdgeGeometryCannotBeNull() {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(null)
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -100, 0})
    void gridEdgeLengthClampedToZero(double length) {
        GridEdge edge = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(length)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        // Length is clamped to 0 for negative values
        assertThat(edge.getLengthKm()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -100, 0})
    void gridEdgeInvalidThermalRatingThrows(double rating) {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(rating)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -100, 0})
    void gridEdgeInvalidVoltageThrows(double voltage) {
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(voltage)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gridEdgeSameSourceAndTargetThrows() {
        CommonKernel.GridNodeId nodeId = CommonKernel.GridNodeId.generate();
        assertThatThrownBy(() -> GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(nodeId)
                .toNodeId(nodeId)
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gridEdgeEquality() {
        CommonKernel.GridEdgeId id = CommonKernel.GridEdgeId.generate();
        CommonKernel.GridNodeId sourceId = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId targetId = CommonKernel.GridNodeId.generate();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}});

        GridEdge edge1 = GridEdge.builder()
                .id(id)
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(sourceId)
                .toNodeId(targetId)
                .geometry(geometry)
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        GridEdge edge2 = GridEdge.builder()
                .id(id)
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(sourceId)
                .toNodeId(targetId)
                .geometry(geometry)
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        assertThat(edge1).isEqualTo(edge2);
        assertThat(edge1.hashCode()).isEqualTo(edge2.hashCode());
    }

    @Test
    void gridEdgeAllTypes() {
        for (EdgeType type : EdgeType.values()) {
            GridEdge edge = GridEdge.builder()
                    .id(CommonKernel.GridEdgeId.generate())
                    .name("Test " + type)
                    .type(type)
                    .fromNodeId(CommonKernel.GridNodeId.generate())
                    .toNodeId(CommonKernel.GridNodeId.generate())
                    .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                    .lengthKm(10)
                    .thermalRatingAmps(500)
                    .voltageLevelKv(220)
                    .build();

            assertThat(edge.getType()).isEqualTo(type);
        }
    }

    @Test
    void gridEdgeConnects() {
        CommonKernel.GridNodeId nodeA = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId nodeB = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId nodeC = CommonKernel.GridNodeId.generate();

        GridEdge edge = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(nodeA)
                .toNodeId(nodeB)
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        assertThat(edge.connects(nodeA)).isTrue();
        assertThat(edge.connects(nodeB)).isTrue();
        assertThat(edge.connects(nodeC)).isFalse();
    }

    @Test
    void gridEdgeGetOtherNode() {
        CommonKernel.GridNodeId nodeA = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId nodeB = CommonKernel.GridNodeId.generate();

        GridEdge edge = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(nodeA)
                .toNodeId(nodeB)
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        assertThat(edge.getOtherNode(nodeA)).isEqualTo(nodeB);
        assertThat(edge.getOtherNode(nodeB)).isEqualTo(nodeA);
    }

    @Test
    void gridEdgeIsTransformer() {
        GridEdge transformer = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSFORMER)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        GridEdge line = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(500)
                .voltageLevelKv(220)
                .build();

        assertThat(transformer.isTransformer()).isTrue();
        assertThat(line.isTransformer()).isFalse();
    }

    @Test
    void gridEdgeMvaRating() {
        GridEdge edge = GridEdge.builder()
                .id(CommonKernel.GridEdgeId.generate())
                .name("Test")
                .type(EdgeType.TRANSMISSION_LINE)
                .fromNodeId(CommonKernel.GridNodeId.generate())
                .toNodeId(CommonKernel.GridNodeId.generate())
                .geometry(TestGeometryFactory.lineString(new double[][]{{0,0},{1,0},{1,1},{0,1}}))
                .lengthKm(10)
                .thermalRatingAmps(1000)
                .voltageLevelKv(220)
                .build();

        // MVA = sqrt(3) * kV * A / 1000
        double expectedMva = Math.sqrt(3) * 220 * 1000 / 1000.0;
        assertThat(edge.getMvaRating()).isCloseTo(expectedMva, within(0.01));
    }
}