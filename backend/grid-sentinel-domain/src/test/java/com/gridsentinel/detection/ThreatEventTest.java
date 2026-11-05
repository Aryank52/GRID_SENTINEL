package com.gridsentinel.detection;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.test.TestGeometryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class ThreatEventTest {

    @Test
    void threatEventCreation() {
        CommonKernel.ThreatEventId id = CommonKernel.ThreatEventId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();

        ThreatEvent event = ThreatEvent.builder()
                .id(id)
                .affectedAssetId(assetId)
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.88)
                .detectedAt(Instant.now())
                .location(TestGeometryFactory.point(0, 0))
                .description("Fire detected by satellite")
                .build();

        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getAffectedAssetId()).isEqualTo(assetId);
        assertThat(event.getType()).isEqualTo(ThreatEvent.ThreatType.FIRE);
        assertThat(event.getSeverity()).isEqualTo(CommonKernel.ThreatSeverity.HIGH);
        assertThat(event.getConfidence()).isEqualTo(0.88);
        assertThat(event.getDetectedAt()).isNotNull();
        assertThat(event.getLocation()).isNotNull();
        assertThat(event.getDescription()).isEqualTo("Fire detected by satellite");
        assertThat(event.getVersion()).isEqualTo(0);
    }

    @Test
    void threatEventWithoutGeometry() {
        CommonKernel.ThreatEventId id = CommonKernel.ThreatEventId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();

        ThreatEvent event = ThreatEvent.builder()
                .id(id)
                .affectedAssetId(assetId)
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.88)
                .detectedAt(Instant.now())
                .description("Fire detected by satellite")
                .build();

        assertThat(event.getLocation()).isNull();
    }

    @Test
    void threatEventAssetIdIsOptional() {
        // affectedAssetId is nullable in the builder
        ThreatEvent event = ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.9)
                .detectedAt(Instant.now())
                .description("Test")
                .build();

        assertThat(event.getAffectedAssetId()).isNull();
    }

    @Test
    void threatEventTypeCannotBeNull() {
        assertThatThrownBy(() -> ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(null)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.9)
                .detectedAt(Instant.now())
                .description("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void threatEventSeverityCannotBeNull() {
        assertThatThrownBy(() -> ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(null)
                .confidence(0.9)
                .detectedAt(Instant.now())
                .description("Test")
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1})
    void threatEventConfidenceOutOfRangeThrows(double confidence) {
        assertThatThrownBy(() -> ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(confidence)
                .detectedAt(Instant.now())
                .description("Test")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void threatEventDescriptionCannotBeNull() {
        assertThatThrownBy(() -> ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.9)
                .detectedAt(Instant.now())
                .description(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void threatEventAllTypes() {
        for (ThreatEvent.ThreatType type : ThreatEvent.ThreatType.values()) {
            ThreatEvent event = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.generate())
                    .affectedAssetId(CommonKernel.AssetId.generate())
                    .type(type)
                    .severity(CommonKernel.ThreatSeverity.MEDIUM)
                    .confidence(0.9)
                    .detectedAt(Instant.now())
                    .description("Test")
                    .build();

            assertThat(event.getType()).isEqualTo(type);
        }
    }

    @Test
    void threatEventAllSeverities() {
        for (CommonKernel.ThreatSeverity severity : CommonKernel.ThreatSeverity.values()) {
            ThreatEvent event = ThreatEvent.builder()
                    .id(CommonKernel.ThreatEventId.generate())
                    .affectedAssetId(CommonKernel.AssetId.generate())
                    .type(ThreatEvent.ThreatType.FIRE)
                    .severity(severity)
                    .confidence(0.9)
                    .detectedAt(Instant.now())
                    .description("Test")
                    .build();

            assertThat(event.getSeverity()).isEqualTo(severity);
        }
    }

    @Test
    void threatEventEquality() {
        CommonKernel.ThreatEventId id = CommonKernel.ThreatEventId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        Instant detectedAt = Instant.now();

        ThreatEvent event1 = ThreatEvent.builder()
                .id(id)
                .affectedAssetId(assetId)
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.88)
                .detectedAt(detectedAt)
                .description("Test")
                .build();

        ThreatEvent event2 = ThreatEvent.builder()
                .id(id)
                .affectedAssetId(assetId)
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.88)
                .detectedAt(detectedAt)
                .description("Test")
                .build();

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void threatEventIsActionable() {
        ThreatEvent actionable = ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.8)
                .detectedAt(Instant.now())
                .description("Test")
                .build();

        ThreatEvent notActionableLowConfidence = ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .confidence(0.5)
                .detectedAt(Instant.now())
                .description("Test")
                .build();

        ThreatEvent notActionableLowSeverity = ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.LOW)
                .confidence(0.9)
                .detectedAt(Instant.now())
                .description("Test")
                .build();

        assertThat(actionable.isActionable()).isTrue();
        assertThat(notActionableLowConfidence.isActionable()).isFalse();
        assertThat(notActionableLowSeverity.isActionable()).isFalse();
    }

    @Test
    void threatEventEscalateSeverity() {
        ThreatEvent event = ThreatEvent.builder()
                .id(CommonKernel.ThreatEventId.generate())
                .affectedAssetId(CommonKernel.AssetId.generate())
                .type(ThreatEvent.ThreatType.FIRE)
                .severity(CommonKernel.ThreatSeverity.MEDIUM)
                .confidence(0.8)
                .detectedAt(Instant.now())
                .description("Test")
                .build();

        event.escalateSeverity(CommonKernel.ThreatSeverity.HIGH);
        assertThat(event.getSeverity()).isEqualTo(CommonKernel.ThreatSeverity.HIGH);
        assertThat(event.getVersion()).isEqualTo(1);

        // Cannot de-escalate
        event.escalateSeverity(CommonKernel.ThreatSeverity.LOW);
        assertThat(event.getSeverity()).isEqualTo(CommonKernel.ThreatSeverity.HIGH);
    }
}