package com.gridsentinel.asset;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.test.TestGeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AssetTest {

    @Test
    void assetCreation() {
        CommonKernel.AssetId id = CommonKernel.AssetId.generate();
        CommonKernel.Criticality criticality = CommonKernel.Criticality.HIGH;
        CommonKernel.HealthIndex health = CommonKernel.HealthIndex.healthy();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(0, 0);

        Asset asset = Asset.builder()
                .id(id)
                .name("Test Asset")
                .type(AssetType.SUBSTATION)
                .criticality(criticality)
                .healthIndex(health)
                .redundancyLevel(1)
                .geometry(geometry)
                .description("Test asset")
                .installedAt(Instant.now().minusSeconds(86400))
                .build();

        assertThat(asset.id()).isEqualTo(id);
        assertThat(asset.name()).isEqualTo("Test Asset");
        assertThat(asset.type()).isEqualTo(AssetType.SUBSTATION);
        assertThat(asset.criticality()).isEqualTo(criticality);
        assertThat(asset.healthIndex()).isEqualTo(health);
        assertThat(asset.redundancyLevel()).isEqualTo(1);
        assertThat(asset.geometry()).isEqualTo(geometry);
        assertThat(asset.description()).isEqualTo("Test asset");
        assertThat(asset.version()).isEqualTo(0);
    }

    @Test
    void assetNameCannotBeNull() {
        assertThatThrownBy(() -> Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name(null)
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetTypeCannotBeNull() {
        assertThatThrownBy(() -> Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(null)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetCriticalityCannotBeNull() {
        assertThatThrownBy(() -> Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(null)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetHealthIndexCannotBeNull() {
        assertThatThrownBy(() -> Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(null)
                .geometry(TestGeometryFactory.point(0, 0))
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetGeometryCannotBeNull() {
        assertThatThrownBy(() -> Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetRedundancyLevelNegativeClampedToZero() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(-5)
                .build();

        assertThat(asset.redundancyLevel()).isEqualTo(0);
    }

    @Test
    void assetUpdateHealth() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        int oldVersion = asset.version();
        CommonKernel.HealthIndex newHealth = CommonKernel.HealthIndex.of(0.5);
        Instant inspectedAt = Instant.now();

        asset.updateHealth(newHealth, inspectedAt);

        assertThat(asset.healthIndex()).isEqualTo(newHealth);
        assertThat(asset.lastInspectedAt()).isEqualTo(inspectedAt);
        assertThat(asset.version()).isEqualTo(oldVersion + 1);
    }

    @Test
    void assetUpdateCriticality() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        int oldVersion = asset.version();

        asset.updateCriticality(CommonKernel.Criticality.MISSION_CRITICAL);

        assertThat(asset.criticality()).isEqualTo(CommonKernel.Criticality.MISSION_CRITICAL);
        assertThat(asset.version()).isEqualTo(oldVersion + 1);
    }

    @Test
    void assetUpdateGeometry() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        int oldVersion = asset.version();
        CommonKernel.GeometryValueObject newGeometry = TestGeometryFactory.point(10, 10);

        asset.updateGeometry(newGeometry);

        assertThat(asset.geometry()).isEqualTo(newGeometry);
        assertThat(asset.version()).isEqualTo(oldVersion + 1);
    }

    @Test
    void assetAssociateWithGridNode() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        CommonKernel.GridNodeId nodeId = CommonKernel.GridNodeId.generate();
        int oldVersion = asset.version();

        asset.associateWithGridNode(nodeId);

        assertThat(asset.gridNodeId()).isEqualTo(nodeId);
        assertThat(asset.version()).isEqualTo(oldVersion + 1);
    }

    @Test
    void assetIsHealthy() {
        Asset healthyAsset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Healthy")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.of(0.9))
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        assertThat(healthyAsset.isHealthy()).isTrue();
    }

    @Test
    void assetIsDegraded() {
        Asset degradedAsset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Degraded")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.of(0.5))
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        assertThat(degradedAsset.isHealthy()).isFalse();
        assertThat(degradedAsset.healthIndex().isDegraded()).isTrue();
    }

    @Test
    void assetIsCritical() {
        Asset criticalAsset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Critical")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.of(0.1))
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        assertThat(criticalAsset.isHealthy()).isFalse();
        assertThat(criticalAsset.healthIndex().isCritical()).isTrue();
    }

    @Test
    void assetHasRedundancy() {
        Asset withRedundancy = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("With Redundancy")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(1)
                .build();

        Asset withoutRedundancy = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("No Redundancy")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(0)
                .build();

        assertThat(withRedundancy.hasRedundancy()).isTrue();
        assertThat(withoutRedundancy.hasRedundancy()).isFalse();
    }

    @Test
    void assetRedundancyFactor() {
        Asset noRedundancy = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(0)
                .build();

        Asset nPlus1 = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(1)
                .build();

        Asset nPlus2 = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .redundancyLevel(2)
                .build();

        assertThat(noRedundancy.redundancyFactor()).isEqualTo(0.0);
        assertThat(nPlus1.redundancyFactor()).isEqualTo(0.33);
        assertThat(nPlus2.redundancyFactor()).isEqualTo(0.66);
    }

    @Test
    void assetEquality() {
        CommonKernel.AssetId id = CommonKernel.AssetId.generate();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(0, 0);

        Asset asset1 = Asset.builder()
                .id(id)
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        Asset asset2 = Asset.builder()
                .id(id)
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        assertThat(asset1).isEqualTo(asset2);
        assertThat(asset1.hashCode()).isEqualTo(asset2.hashCode());
    }

    @Test
    void assetBuilderDefaults() {
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.healthy())
                .geometry(TestGeometryFactory.point(0, 0))
                .build();

        assertThat(asset.redundancyLevel()).isEqualTo(0);
        assertThat(asset.description()).isNull();
        assertThat(asset.installedAt()).isNull();
        assertThat(asset.lastInspectedAt()).isNull();
        assertThat(asset.gridNodeId()).isNull();
    }
}