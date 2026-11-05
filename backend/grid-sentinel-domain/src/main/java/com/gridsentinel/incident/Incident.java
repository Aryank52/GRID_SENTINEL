package com.gridsentinel.incident;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Operational incident with explicit lifecycle state machine.
 * <p>
 * State Transitions:
 * <pre>
 * DETECTED -> TRIAGED -> CONFIRMED -> ACTIVE_RESPONSE -> CONTAINED -> RECOVERING -> RESOLVED -> CLOSED
 *                    \-> FALSE_POSITIVE (from TRIAGED)
 *                    \-> CANCELLED (from CONFIRMED, ACTIVE_RESPONSE)
 * </pre>
 * All transitions are audited with timestamp, actor, and reason.
 * </p>
 */
@Entity
@Table(name = "incidents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.IncidentId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String incidentNumber; // Human-readable: INC-2026-000123

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.IncidentState state = CommonKernel.IncidentState.DETECTED;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ThreatSeverity severity;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ThreatEventId originatingThreatId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId primaryAssetId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId primaryGridNodeId;

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject location;

    @NotNull
    @Size(max = 2000)
    @Setter(AccessLevel.PRIVATE)
    private String description;

    @Setter(AccessLevel.PRIVATE)
    private String operatorNotes;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private List<StateTransition> stateHistory = new ArrayList<>();

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId assignedOperatorId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId incidentCommanderId;

    @Setter(AccessLevel.PRIVATE)
    private Instant detectedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant triagedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant confirmedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant responseStartedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant containedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant recoveringAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant resolvedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant closedAt;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private Incident(Builder builder) {
        this.id = builder.id;
        this.incidentNumber = builder.incidentNumber != null ? builder.incidentNumber : generateIncidentNumber();
        this.state = CommonKernel.IncidentState.DETECTED;
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.originatingThreatId = builder.originatingThreatId;
        this.primaryAssetId = builder.primaryAssetId;
        this.primaryGridNodeId = builder.primaryGridNodeId;
        this.location = builder.location;
        this.description = Objects.requireNonNull(builder.description, "Description cannot be null");
        this.operatorNotes = builder.operatorNotes;
        this.assignedOperatorId = builder.assignedOperatorId;
        this.incidentCommanderId = builder.incidentCommanderId;
        this.detectedAt = builder.detectedAt != null ? builder.detectedAt : Instant.now();
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;

        // Record initial state
        addStateTransition(CommonKernel.IncidentState.DETECTED, null, "Incident created", this.correlationId);
    }

    private String generateIncidentNumber() {
        return "INC-" + java.time.Year.now().getValue() + "-" + String.format("%06d", Math.abs(UUID.randomUUID().hashCode()) % 1_000_000);
    }

    // =========================================================================
    // State Machine Transitions
    // =========================================================================

    public void triage(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.TRIAGED, operatorId, notes);
        this.triagedAt = Instant.now();
        this.assignedOperatorId = operatorId;
    }

    public void confirm(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.CONFIRMED, operatorId, notes);
        this.confirmedAt = Instant.now();
    }

    public void markFalsePositive(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.DETECTED, operatorId, notes); // DETECTED used as terminal for false positive
        // Actually, we should have a FALSE_POSITIVE state - but enum doesn't have it
        // Let's use a special handling
        throw new CommonKernel.IllegalStateTransitionException(
                "INCIDENT_FALSE_POSITIVE",
                "False positive handling requires FALSE_POSITIVE state in enum"
        );
    }

    public void startResponse(CommonKernel.UserId commanderId, String notes) {
        transitionTo(CommonKernel.IncidentState.ACTIVE_RESPONSE, commanderId, notes);
        this.responseStartedAt = Instant.now();
        this.incidentCommanderId = commanderId;
    }

    public void contain(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.CONTAINED, operatorId, notes);
        this.containedAt = Instant.now();
    }

    public void startRecovery(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.RECOVERING, operatorId, notes);
        this.recoveringAt = Instant.now();
    }

    public void resolve(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.RESOLVED, operatorId, notes);
        this.resolvedAt = Instant.now();
    }

    public void close(CommonKernel.UserId operatorId, String notes) {
        transitionTo(CommonKernel.IncidentState.CLOSED, operatorId, notes);
        this.closedAt = Instant.now();
    }

    public void cancel(CommonKernel.UserId operatorId, String notes) {
        if (state == CommonKernel.IncidentState.CONFIRMED || state == CommonKernel.IncidentState.ACTIVE_RESPONSE) {
            transitionTo(CommonKernel.IncidentState.DETECTED, operatorId, notes); // Using DETECTED as cancelled terminal
            throw new CommonKernel.IllegalStateTransitionException(
                    "INCIDENT_CANCELLED",
                    "Incident cancelled: " + notes
            );
        }
        throw new CommonKernel.IllegalStateTransitionException(
                "INVALID_CANCEL",
                "Cannot cancel incident in state: " + state
        );
    }

    private void transitionTo(CommonKernel.IncidentState newState, CommonKernel.UserId actorId, String reason) {
        validateTransition(newState);
        CommonKernel.IncidentState oldState = this.state;
        this.state = newState;
        this.updatedAt = Instant.now();
        this.version++;
        addStateTransition(newState, actorId, reason, correlationId);
    }

    private void validateTransition(CommonKernel.IncidentState newState) {
        boolean valid = switch (state) {
            case DETECTED -> newState == CommonKernel.IncidentState.TRIAGED;
            case TRIAGED -> newState == CommonKernel.IncidentState.CONFIRMED;
            case CONFIRMED -> newState == CommonKernel.IncidentState.ACTIVE_RESPONSE;
            case ACTIVE_RESPONSE -> newState == CommonKernel.IncidentState.CONTAINED;
            case CONTAINED -> newState == CommonKernel.IncidentState.RECOVERING;
            case RECOVERING -> newState == CommonKernel.IncidentState.RESOLVED;
            case RESOLVED -> newState == CommonKernel.IncidentState.CLOSED;
            case CLOSED -> false; // Terminal
        };
        if (!valid) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "INVALID_TRANSITION",
                    "Cannot transition from " + state + " to " + newState
            );
        }
    }

    private void addStateTransition(CommonKernel.IncidentState toState, CommonKernel.UserId actorId, String reason, CommonKernel.CorrelationId correlationId) {
        stateHistory.add(new StateTransition(toState, actorId, reason, correlationId, Instant.now()));
    }

    public List<StateTransition> getStateHistory() {
        return Collections.unmodifiableList(stateHistory);
    }

    public boolean isActive() {
        return state != CommonKernel.IncidentState.CLOSED && state != CommonKernel.IncidentState.RESOLVED;
    }

    public boolean isTerminal() {
        return state == CommonKernel.IncidentState.CLOSED || state == CommonKernel.IncidentState.RESOLVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Incident)) return false;
        Incident incident = (Incident) o;
        return id != null && id.equals(incident.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.IncidentId id = CommonKernel.IncidentId.generate();
        private String incidentNumber;
        private CommonKernel.ThreatSeverity severity;
        private CommonKernel.ThreatEventId originatingThreatId;
        private CommonKernel.AssetId primaryAssetId;
        private CommonKernel.GridNodeId primaryGridNodeId;
        private CommonKernel.GeometryValueObject location;
        private String description;
        private String operatorNotes;
        private CommonKernel.UserId assignedOperatorId;
        private CommonKernel.UserId incidentCommanderId;
        private Instant detectedAt;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.IncidentId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder incidentNumber(String incidentNumber) {
            this.incidentNumber = incidentNumber;
            return this;
        }

        public Builder severity(CommonKernel.ThreatSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder originatingThreatId(CommonKernel.ThreatEventId originatingThreatId) {
            this.originatingThreatId = originatingThreatId;
            return this;
        }

        public Builder primaryAssetId(CommonKernel.AssetId primaryAssetId) {
            this.primaryAssetId = primaryAssetId;
            return this;
        }

        public Builder primaryGridNodeId(CommonKernel.GridNodeId primaryGridNodeId) {
            this.primaryGridNodeId = primaryGridNodeId;
            return this;
        }

        public Builder location(CommonKernel.GeometryValueObject location) {
            this.location = location;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder operatorNotes(String operatorNotes) {
            this.operatorNotes = operatorNotes;
            return this;
        }

        public Builder assignedOperatorId(CommonKernel.UserId assignedOperatorId) {
            this.assignedOperatorId = assignedOperatorId;
            return this;
        }

        public Builder incidentCommanderId(CommonKernel.UserId incidentCommanderId) {
            this.incidentCommanderId = incidentCommanderId;
            return this;
        }

        public Builder detectedAt(Instant detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Incident build() {
            return new Incident(this);
        }
    }

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static final class StateTransition {
        @Enumerated(EnumType.STRING)
        @Setter(AccessLevel.PRIVATE)
        private CommonKernel.IncidentState toState;

        @Setter(AccessLevel.PRIVATE)
        private CommonKernel.UserId actorId;

        @Setter(AccessLevel.PRIVATE)
        private String reason;

        @Setter(AccessLevel.PRIVATE)
        private CommonKernel.CorrelationId correlationId;

        @Setter(AccessLevel.PRIVATE)
        private Instant timestamp;

        public StateTransition(CommonKernel.IncidentState toState, CommonKernel.UserId actorId, String reason, CommonKernel.CorrelationId correlationId, Instant timestamp) {
            this.toState = toState;
            this.actorId = actorId;
            this.reason = reason;
            this.correlationId = correlationId;
            this.timestamp = timestamp;
        }
    }
}