package com.gridsentinel.grid;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.test.TestGeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class GridNodeTest {

    @Test
    void gridNodeCreation() {
        CommonKernel.GridNodeId id = CommonKernel.GridNodeId.generate();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(10.5, 20.3);

        GridNode node = GridNode.builder()
                .id(id)
                .name("Test Substation")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(geometry)
                .voltageLevelKv(220)
                .build();

        assertThat(node.getId()).isEqualTo(id);
        assertThat(node.getName()).isEqualTo("Test Substation");
        assertThat(node.getType()).isEqualTo(GridNode.NodeType.SUBSTATION);
        assertThat(node.getGeometry()).isEqualTo(geometry);
        assertThat(node.getVoltageLevelKv()).isEqualTo(220);
        assertThat(node.getVersion()).isEqualTo(0);
    }

    @Test
    void gridNodeNameCannotBeNull() {
        assertThatThrownBy(() -> GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name(null)
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridNodeTypeCannotBeNull() {
        assertThatThrownBy(() -> GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Test")
                .type(null)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridNodeGeometryCannotBeNull() {
        assertThatThrownBy(() -> GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Test")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(null)
                .voltageLevelKv(220)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, -100, 0})
    void gridNodeInvalidVoltageThrows(double voltage) {
        assertThatThrownBy(() -> GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Test")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(voltage)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void gridNodeEquality() {
        CommonKernel.GridNodeId id = CommonKernel.GridNodeId.generate();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(0, 0);

        GridNode node1 = GridNode.builder()
                .id(id)
                .name("Test")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(geometry)
                .voltageLevelKv(220)
                .build();

        GridNode node2 = GridNode.builder()
                .id(id)
                .name("Test")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(geometry)
                .voltageLevelKv(220)
                .build();

        assertThat(node1).isEqualTo(node2);
        assertThat(node1.hashCode()).isEqualTo(node2.hashCode());
    }

    @Test
    void gridNodeBuilderDefaults() {
        GridNode node = GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Test")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build();

        assertThat(node.getPrimaryAssetId()).isNull();
        assertThat(node.getOperator()).isNull();
    }

    @Test
    void gridNodeAllTypes() {
        for (GridNode.NodeType type : GridNode.NodeType.values()) {
            GridNode node = GridNode.builder()
                    .id(CommonKernel.GridNodeId.generate())
                    .name("Test " + type)
                    .type(type)
                    .geometry(TestGeometryFactory.point(0, 0))
                    .voltageLevelKv(110)
                    .build();

            assertThat(node.getType()).isEqualTo(type);
        }
    }

    @Test
    void gridNodeIsSubstation() {
        GridNode substation = GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Substation")
                .type(GridNode.NodeType.SUBSTATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build();

        GridNode switching = GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Switching")
                .type(GridNode.NodeType.SWITCHING_STATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build();

        GridNode generation = GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Generation")
                .type(GridNode.NodeType.GENERATION)
                .geometry(TestGeometryFactory.point(0, 0))
                .voltageLevelKv(220)
                .build();

        assertThat(substation.isSubstation()).isTrue();
        assertThat(switching.isSubstation()).isTrue();
        assertThat(generation.isSubstation()).isFalse();
    }
}