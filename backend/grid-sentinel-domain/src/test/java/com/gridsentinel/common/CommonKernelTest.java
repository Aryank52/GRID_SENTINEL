package com.gridsentinel.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CommonKernelTest {

    @Test
    void assetIdGeneration() {
        CommonKernel.AssetId id1 = CommonKernel.AssetId.generate();
        CommonKernel.AssetId id2 = CommonKernel.AssetId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    void assetIdOf() {
        UUID uuid = UUID.randomUUID();
        CommonKernel.AssetId id = CommonKernel.AssetId.of(uuid);

        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void assetIdOfNullThrows() {
        assertThatThrownBy(() -> CommonKernel.AssetId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gridNodeIdGeneration() {
        CommonKernel.GridNodeId id1 = CommonKernel.GridNodeId.generate();
        CommonKernel.GridNodeId id2 = CommonKernel.GridNodeId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void gridEdgeIdGeneration() {
        CommonKernel.GridEdgeId id1 = CommonKernel.GridEdgeId.generate();
        CommonKernel.GridEdgeId id2 = CommonKernel.GridEdgeId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void observationIdGeneration() {
        CommonKernel.ObservationId id1 = CommonKernel.ObservationId.generate();
        CommonKernel.ObservationId id2 = CommonKernel.ObservationId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void riskScoreIdGeneration() {
        CommonKernel.RiskScoreId id1 = CommonKernel.RiskScoreId.generate();
        CommonKernel.RiskScoreId id2 = CommonKernel.RiskScoreId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void threatEventIdGeneration() {
        CommonKernel.ThreatEventId id1 = CommonKernel.ThreatEventId.generate();
        CommonKernel.ThreatEventId id2 = CommonKernel.ThreatEventId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void incidentIdGeneration() {
        CommonKernel.IncidentId id1 = CommonKernel.IncidentId.generate();
        CommonKernel.IncidentId id2 = CommonKernel.IncidentId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void simulationIdGeneration() {
        CommonKernel.SimulationId id1 = CommonKernel.SimulationId.generate();
        CommonKernel.SimulationId id2 = CommonKernel.SimulationId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void responsePlanIdGeneration() {
        CommonKernel.ResponsePlanId id1 = CommonKernel.ResponsePlanId.generate();
        CommonKernel.ResponsePlanId id2 = CommonKernel.ResponsePlanId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void recoveryStateIdGeneration() {
        CommonKernel.RecoveryStateId id1 = CommonKernel.RecoveryStateId.generate();
        CommonKernel.RecoveryStateId id2 = CommonKernel.RecoveryStateId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void protectionPlanIdGeneration() {
        CommonKernel.ProtectionPlanId id1 = CommonKernel.ProtectionPlanId.generate();
        CommonKernel.ProtectionPlanId id2 = CommonKernel.ProtectionPlanId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void auditLogIdGeneration() {
        CommonKernel.AuditLogId id1 = CommonKernel.AuditLogId.generate();
        CommonKernel.AuditLogId id2 = CommonKernel.AuditLogId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void userIdGeneration() {
        CommonKernel.UserId id1 = CommonKernel.UserId.generate();
        CommonKernel.UserId id2 = CommonKernel.UserId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void correlationIdGeneration() {
        CommonKernel.CorrelationId id1 = CommonKernel.CorrelationId.generate();
        CommonKernel.CorrelationId id2 = CommonKernel.CorrelationId.generate();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void correlationIdFromString() {
        String uuidStr = UUID.randomUUID().toString();
        CommonKernel.CorrelationId id = CommonKernel.CorrelationId.fromString(uuidStr);

        assertThat(id.value().toString()).isEqualTo(uuidStr);
    }

    @Test
    void timeRangeCreation() {
        java.time.Instant start = java.time.Instant.now();
        java.time.Instant end = start.plusSeconds(3600);

        CommonKernel.TimeRange range = CommonKernel.TimeRange.of(start, end);

        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isEqualTo(end);
    }

    @Test
    void timeRangeStartingAt() {
        java.time.Instant start = java.time.Instant.now();

        CommonKernel.TimeRange range = CommonKernel.TimeRange.startingAt(start);

        assertThat(range.start()).isEqualTo(start);
        assertThat(range.end()).isNull();
    }

    @Test
    void timeRangeEndBeforeStartThrows() {
        java.time.Instant start = java.time.Instant.now();
        java.time.Instant end = start.minusSeconds(3600);

        assertThatThrownBy(() -> CommonKernel.TimeRange.of(start, end))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void timeRangeContains() {
        java.time.Instant start = java.time.Instant.now();
        java.time.Instant middle = start.plusSeconds(1800);
        java.time.Instant end = start.plusSeconds(3600);

        CommonKernel.TimeRange range = CommonKernel.TimeRange.of(start, end);

        assertThat(range.contains(middle)).isTrue();
        assertThat(range.contains(start)).isTrue();
        assertThat(range.contains(end)).isFalse();
    }

    @Test
    void timeRangeOverlaps() {
        java.time.Instant start1 = java.time.Instant.now();
        java.time.Instant end1 = start1.plusSeconds(3600);
        java.time.Instant start2 = start1.plusSeconds(1800);
        java.time.Instant end2 = start1.plusSeconds(5400);

        CommonKernel.TimeRange range1 = CommonKernel.TimeRange.of(start1, end1);
        CommonKernel.TimeRange range2 = CommonKernel.TimeRange.of(start2, end2);

        assertThat(range1.overlaps(range2)).isTrue();
        assertThat(range2.overlaps(range1)).isTrue();
    }

    @Test
    void coordinateCreation() {
        CommonKernel.Coordinate coord = CommonKernel.Coordinate.of(10.5, 20.3);

        assertThat(coord.longitude()).isEqualTo(10.5);
        assertThat(coord.latitude()).isEqualTo(20.3);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-181, 181})
    void coordinateInvalidLongitudeThrows(double lon) {
        assertThatThrownBy(() -> CommonKernel.Coordinate.of(lon, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-91, 91})
    void coordinateInvalidLatitudeThrows(double lat) {
        assertThatThrownBy(() -> CommonKernel.Coordinate.of(0, lat))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void criticalityValues() {
        assertThat(CommonKernel.Criticality.LOW).isNotNull();
        assertThat(CommonKernel.Criticality.MEDIUM).isNotNull();
        assertThat(CommonKernel.Criticality.HIGH).isNotNull();
        assertThat(CommonKernel.Criticality.MISSION_CRITICAL).isNotNull();
    }

    @Test
    void healthIndexCreation() {
        CommonKernel.HealthIndex healthy = CommonKernel.HealthIndex.healthy();
        CommonKernel.HealthIndex degraded = CommonKernel.HealthIndex.degraded();
        CommonKernel.HealthIndex critical = CommonKernel.HealthIndex.critical();

        assertThat(healthy.value()).isEqualTo(1.0);
        assertThat(degraded.value()).isEqualTo(0.5);
        assertThat(critical.value()).isEqualTo(0.1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.1})
    void healthIndexOutOfRangeThrows(double value) {
        assertThatThrownBy(() -> CommonKernel.HealthIndex.of(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void healthIndexStates() {
        assertThat(CommonKernel.HealthIndex.healthy().isHealthy()).isTrue();
        assertThat(CommonKernel.HealthIndex.of(0.9).isHealthy()).isTrue();
        assertThat(CommonKernel.HealthIndex.of(0.5).isDegraded()).isTrue();
        assertThat(CommonKernel.HealthIndex.of(0.2).isCritical()).isTrue();
    }

    @Test
    void riskBandFromScore() {
        assertThat(CommonKernel.RiskBand.fromScore(0.05)).isEqualTo(CommonKernel.RiskBand.NEGLIGIBLE);
        assertThat(CommonKernel.RiskBand.fromScore(0.15)).isEqualTo(CommonKernel.RiskBand.LOW);
        assertThat(CommonKernel.RiskBand.fromScore(0.4)).isEqualTo(CommonKernel.RiskBand.MEDIUM);
        assertThat(CommonKernel.RiskBand.fromScore(0.7)).isEqualTo(CommonKernel.RiskBand.HIGH);
        assertThat(CommonKernel.RiskBand.fromScore(0.9)).isEqualTo(CommonKernel.RiskBand.CRITICAL);
        assertThat(CommonKernel.RiskBand.fromScore(1.0)).isEqualTo(CommonKernel.RiskBand.CRITICAL);
    }

    @Test
    void domainExceptionCreation() {
        CommonKernel.DomainException ex = new CommonKernel.DomainException("TEST_CODE", "Test message");

        assertThat(ex.getCode()).isEqualTo("TEST_CODE");
        assertThat(ex.getMessage()).isEqualTo("Test message");
        assertThat(ex.getArgs()).isEmpty();
    }

    @Test
    void domainExceptionWithArgs() {
        CommonKernel.DomainException ex = new CommonKernel.DomainException("TEST_CODE", "Test %s %d", "arg1", 42);

        assertThat(ex.getCode()).isEqualTo("TEST_CODE");
        assertThat(ex.getMessage()).isEqualTo("Test arg1 42");
        assertThat(ex.getArgs()).containsExactly("arg1", 42);
    }

    @Test
    void validationException() {
        CommonKernel.ValidationException ex = new CommonKernel.ValidationException("VALIDATION_ERROR", "Invalid value");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
        assertThat(ex.getCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void notFoundException() {
        CommonKernel.NotFoundException ex = new CommonKernel.NotFoundException("NOT_FOUND", "Entity not found");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
    }

    @Test
    void conflictException() {
        CommonKernel.ConflictException ex = new CommonKernel.ConflictException("CONFLICT", "Already exists");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
    }

    @Test
    void illegalStateTransitionException() {
        CommonKernel.IllegalStateTransitionException ex = new CommonKernel.IllegalStateTransitionException(
                "INVALID_TRANSITION", "Cannot transition from A to B");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
    }

    @Test
    void simulationException() {
        CommonKernel.SimulationException ex = new CommonKernel.SimulationException("SIM_ERROR", "Simulation failed");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
    }

    @Test
    void modelInferenceException() {
        CommonKernel.ModelInferenceException ex = new CommonKernel.ModelInferenceException("MODEL_ERROR", "Inference failed");

        assertThat(ex).isInstanceOf(CommonKernel.DomainException.class);
    }

    @Test
    void domainEventTimestamp() {
        java.time.Instant before = java.time.Instant.now();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();
        CommonKernel.AbstractDomainEvent event = new CommonKernel.AbstractDomainEvent(correlationId) {
            @Override
            public String eventType() {
                return "TestEvent";
            }
        };
        java.time.Instant after = java.time.Instant.now();

        assertThat(event.occurredAt()).isBetween(before, after);
        assertThat(event.correlationId()).isEqualTo(correlationId);
    }
}