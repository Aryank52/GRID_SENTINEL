package com.gridsentinel.incident;

import com.gridsentinel.common.CommonKernel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class IncidentTest {

    @Test
    void incidentCreation() {
        CommonKernel.IncidentId id = CommonKernel.IncidentId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();

        Incident incident = Incident.builder()
                .id(id)
                .primaryAssetId(assetId)
                .correlationId(correlationId)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Thermal camera detected anomaly on transformer T1")
                .detectedAt(Instant.now())
                .build();

        assertThat(incident.getId()).isEqualTo(id);
        assertThat(incident.getPrimaryAssetId()).isEqualTo(assetId);
        assertThat(incident.getCorrelationId()).isEqualTo(correlationId);
        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.DETECTED);
        assertThat(incident.getSeverity()).isEqualTo(CommonKernel.ThreatSeverity.HIGH);
        assertThat(incident.getDescription()).isEqualTo("Thermal camera detected anomaly on transformer T1");
        assertThat(incident.getDetectedAt()).isNotNull();
        assertThat(incident.getVersion()).isEqualTo(0);
    }

    @Test
    void incidentPrimaryAssetIdCannotBeNull() {
        // primaryAssetId is nullable in the builder, so this should not throw
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(null)
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        assertThat(incident.getPrimaryAssetId()).isNull();
    }

    @Test
    void incidentDescriptionCannotBeNull() {
        assertThatThrownBy(() -> Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description(null)
                .detectedAt(Instant.now())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void incidentInitialStateMustBeDetected() {
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.DETECTED);
    }

    // =========================================================================
    // VALID STATE TRANSITIONS
    // =========================================================================

    @Test
    void incidentValidTransitionDetectedToTriaged() {
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        int oldVersion = incident.getVersion();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.triage(operatorId, "Initial triage completed");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.TRIAGED);
        assertThat(incident.getVersion()).isEqualTo(oldVersion + 1);
        assertThat(incident.getTriagedAt()).isNotNull();
        assertThat(incident.getAssignedOperatorId()).isEqualTo(operatorId);
    }

    @Test
    void incidentValidTransitionTriagedToConfirmed() {
        Incident incident = createTriagedIncident();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.confirm(operatorId, "Confirmed by field crew");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.CONFIRMED);
        assertThat(incident.getConfirmedAt()).isNotNull();
    }

    @Test
    void incidentValidTransitionConfirmedToActiveResponse() {
        Incident incident = createConfirmedIncident();
        CommonKernel.UserId commanderId = CommonKernel.UserId.generate();
        incident.startResponse(commanderId, "Response team deployed");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.ACTIVE_RESPONSE);
        assertThat(incident.getResponseStartedAt()).isNotNull();
        assertThat(incident.getIncidentCommanderId()).isEqualTo(commanderId);
    }

    @Test
    void incidentValidTransitionActiveResponseToContained() {
        Incident incident = createActiveResponseIncident();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.contain(operatorId, "Fire contained");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.CONTAINED);
        assertThat(incident.getContainedAt()).isNotNull();
    }

    @Test
    void incidentValidTransitionContainedToRecovering() {
        Incident incident = createContainedIncident();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.startRecovery(operatorId, "Recovery started");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.RECOVERING);
        assertThat(incident.getRecoveringAt()).isNotNull();
    }

    @Test
    void incidentValidTransitionRecoveringToResolved() {
        Incident incident = createRecoveringIncident();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.resolve(operatorId, "All systems restored");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.RESOLVED);
        assertThat(incident.getResolvedAt()).isNotNull();
    }

    @Test
    void incidentValidTransitionResolvedToClosed() {
        Incident incident = createResolvedIncident();
        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.close(operatorId, "Incident closed after review");

        assertThat(incident.getState()).isEqualTo(CommonKernel.IncidentState.CLOSED);
        assertThat(incident.getClosedAt()).isNotNull();
    }

    // =========================================================================
    // INVALID STATE TRANSITIONS
    // =========================================================================

    @Test
    void incidentInvalidTransitionDetectedToConfirmed() {
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> incident.confirm(CommonKernel.UserId.generate(), "Skip triage"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from DETECTED to CONFIRMED");
    }

    @Test
    void incidentInvalidTransitionTriagedToActiveResponse() {
        Incident incident = createTriagedIncident();

        assertThatThrownBy(() -> incident.startResponse(CommonKernel.UserId.generate(), "Skip confirm"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from TRIAGED to ACTIVE_RESPONSE");
    }

    @Test
    void incidentInvalidTransitionConfirmedToContained() {
        Incident incident = createConfirmedIncident();

        assertThatThrownBy(() -> incident.contain(CommonKernel.UserId.generate(), "Skip response"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from CONFIRMED to CONTAINED");
    }

    @Test
    void incidentInvalidTransitionClosedToActiveResponse() {
        Incident incident = createClosedIncident();

        assertThatThrownBy(() -> incident.startResponse(CommonKernel.UserId.generate(), "Reopen"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from CLOSED to ACTIVE_RESPONSE");
    }

    @Test
    void incidentCannotTransitionFromClosed() {
        Incident incident = createClosedIncident();

        // Test that resolved incident cannot be resolved again
        assertThatThrownBy(() -> incident.resolve(CommonKernel.UserId.generate(), "Already resolved"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void incidentCancelOnlyFromConfirmedOrActiveResponse() {
        // Cancel from DETECTED should fail
        Incident detected = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        assertThatThrownBy(() -> detected.cancel(CommonKernel.UserId.generate(), "Cancel"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);

        // Cancel from TRIAGED should fail
        Incident triaged = createTriagedIncident();
        assertThatThrownBy(() -> triaged.cancel(CommonKernel.UserId.generate(), "Cancel"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);

        // Cancel from CONFIRMED throws exception (transitions to DETECTED which is invalid from CONFIRMED)
        Incident confirmed = createConfirmedIncident();
        assertThatThrownBy(() -> confirmed.cancel(CommonKernel.UserId.generate(), "Cancel"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from CONFIRMED to DETECTED");

        // Cancel from ACTIVE_RESPONSE throws exception (transitions to DETECTED which is invalid from ACTIVE_RESPONSE)
        Incident active = createActiveResponseIncident();
        assertThatThrownBy(() -> active.cancel(CommonKernel.UserId.generate(), "Cancel"))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class)
                .hasMessageContaining("Cannot transition from ACTIVE_RESPONSE to DETECTED");
    }

    @Test
    void incidentEquality() {
        CommonKernel.IncidentId id = CommonKernel.IncidentId.generate();
        CommonKernel.AssetId assetId = CommonKernel.AssetId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();
        Instant detectedAt = Instant.now();

        Incident incident1 = Incident.builder()
                .id(id)
                .primaryAssetId(assetId)
                .correlationId(correlationId)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(detectedAt)
                .build();

        Incident incident2 = Incident.builder()
                .id(id)
                .primaryAssetId(assetId)
                .correlationId(correlationId)
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(detectedAt)
                .build();

        assertThat(incident1).isEqualTo(incident2);
        assertThat(incident1.hashCode()).isEqualTo(incident2.hashCode());
    }

    @Test
    void incidentIsActive() {
        Incident detected = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        Incident resolved = createResolvedIncident();
        Incident closed = createClosedIncident();

        assertThat(detected.isActive()).isTrue();
        assertThat(resolved.isActive()).isFalse();
        assertThat(closed.isActive()).isFalse();
    }

    @Test
    void incidentIsTerminal() {
        Incident detected = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        Incident resolved = createResolvedIncident();
        Incident closed = createClosedIncident();

        assertThat(detected.isTerminal()).isFalse();
        assertThat(resolved.isTerminal()).isTrue();
        assertThat(closed.isTerminal()).isTrue();
    }

    @Test
    void incidentStateHistoryRecorded() {
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();

        assertThat(incident.getStateHistory()).hasSize(1); // Initial DETECTED state

        CommonKernel.UserId operatorId = CommonKernel.UserId.generate();
        incident.triage(operatorId, "Triage done");

        assertThat(incident.getStateHistory()).hasSize(2);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private Incident createTriagedIncident() {
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .primaryAssetId(CommonKernel.AssetId.generate())
                .correlationId(CommonKernel.CorrelationId.generate())
                .severity(CommonKernel.ThreatSeverity.HIGH)
                .description("Test")
                .detectedAt(Instant.now())
                .build();
        incident.triage(CommonKernel.UserId.generate(), "Triage done");
        return incident;
    }

    private Incident createConfirmedIncident() {
        Incident incident = createTriagedIncident();
        incident.confirm(CommonKernel.UserId.generate(), "Confirmed");
        return incident;
    }

    private Incident createActiveResponseIncident() {
        Incident incident = createConfirmedIncident();
        incident.startResponse(CommonKernel.UserId.generate(), "Response started");
        return incident;
    }

    private Incident createContainedIncident() {
        Incident incident = createActiveResponseIncident();
        incident.contain(CommonKernel.UserId.generate(), "Contained");
        return incident;
    }

    private Incident createRecoveringIncident() {
        Incident incident = createContainedIncident();
        incident.startRecovery(CommonKernel.UserId.generate(), "Recovery started");
        return incident;
    }

    private Incident createResolvedIncident() {
        Incident incident = createRecoveringIncident();
        incident.resolve(CommonKernel.UserId.generate(), "Resolved");
        return incident;
    }

    private Incident createClosedIncident() {
        Incident incident = createResolvedIncident();
        incident.close(CommonKernel.UserId.generate(), "Closed");
        return incident;
    }
}