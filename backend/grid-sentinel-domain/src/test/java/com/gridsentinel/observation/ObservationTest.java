package com.gridsentinel.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.test.TestGeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class ObservationTest {

    @Test
    void observationCreation() {
        CommonKernel.ObservationId id = CommonKernel.ObservationId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(10.5, 20.3);

        Observation obs = Observation.builder()
                .id(id)
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(geometry)
                .payload("Temperature anomaly detected at phase A")
                .associatedAssetId(assetId)
                .build();

        assertThat(obs.getId()).isEqualTo(id);
        assertThat(obs.getAssociatedAssetId()).isEqualTo(assetId);
        assertThat(obs.getSourceType()).isEqualTo(CommonKernel.ObservationSourceType.THERMAL_CAMERA);
        assertThat(obs.getSourceIdentifier()).isEqualTo("CAM-001");
        assertThat(obs.getTimestamp()).isNotNull();
        assertThat(obs.getGeometry()).isEqualTo(geometry);
        assertThat(obs.getPayload()).isEqualTo("Temperature anomaly detected at phase A");
        assertThat(obs.getVersion()).isEqualTo(0);
    }

    @Test
    void observationAssetIdCannotBeNull() {
        // associatedAssetId is nullable, so this should not throw
        Observation obs = Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test")
                .build();

        assertThat(obs.getAssociatedAssetId()).isNull();
    }

    @Test
    void observationSourceTypeCannotBeNull() {
        assertThatThrownBy(() -> Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(null)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void observationSourceIdentifierCannotBeNull() {
        assertThatThrownBy(() -> Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier(null)
                .timestamp(Instant.now())
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void observationTimestampCannotBeNull() {
        assertThatThrownBy(() -> Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(null)
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void observationGeometryCannotBeNull() {
        // Geometry is nullable
        Observation obs = Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(null)
                .payload("Test")
                .build();

        assertThat(obs.getGeometry()).isNull();
    }

    @Test
    void observationPayloadCannotBeNull() {
        assertThatThrownBy(() -> Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(TestGeometryFactory.point(0, 0))
                .payload(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void observationFutureTimestampThrows() {
        assertThatThrownBy(() -> Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now().plusSeconds(600)) // 10 minutes in future
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void observationAllSourceTypes() {
        for (CommonKernel.ObservationSourceType type : CommonKernel.ObservationSourceType.values()) {
            Observation obs = Observation.builder()
                    .id(CommonKernel.ObservationId.generate())
                    .sourceType(type)
                    .sourceIdentifier("SRC-001")
                    .timestamp(Instant.now())
                    .geometry(TestGeometryFactory.point(0, 0))
                    .payload("Test")
                    .build();

            assertThat(obs.getSourceType()).isEqualTo(type);
        }
    }

    @Test
    void observationEquality() {
        CommonKernel.ObservationId id = CommonKernel.ObservationId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        Instant timestamp = Instant.now();
        CommonKernel.GeometryValueObject geometry = TestGeometryFactory.point(0, 0);

        Observation obs1 = Observation.builder()
                .id(id)
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(timestamp)
                .geometry(geometry)
                .payload("Test")
                .associatedAssetId(assetId)
                .build();

        Observation obs2 = Observation.builder()
                .id(id)
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(timestamp)
                .geometry(geometry)
                .payload("Test")
                .associatedAssetId(assetId)
                .build();

        assertThat(obs1).isEqualTo(obs2);
        assertThat(obs1.hashCode()).isEqualTo(obs2.hashCode());
    }

    @Test
    void observationIntegrityVerification() {
        Observation obs = Observation.builder()
                .id(CommonKernel.ObservationId.generate())
                .sourceType(CommonKernel.ObservationSourceType.THERMAL_CAMERA)
                .sourceIdentifier("CAM-001")
                .timestamp(Instant.now())
                .geometry(TestGeometryFactory.point(0, 0))
                .payload("Test payload")
                .build();

        assertThat(obs.verifyIntegrity()).isTrue();
    }
}