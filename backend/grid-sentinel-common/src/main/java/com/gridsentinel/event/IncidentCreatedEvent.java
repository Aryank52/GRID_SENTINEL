package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a new incident is created from a threat or manually.
 * Emitted when an Incident entity is first created and persisted.
 */
public final class IncidentCreatedEvent extends GridEvent {

    private final String incidentId;
    private final String incidentNumber;
    private final String state;
    private final String severity;
    private final String originatingThreatId;
    private final String primaryAssetId;
    private final String primaryGridNodeId;
    private final String description;
    private final String assignedOperatorId;
    private final String incidentCommanderId;

    public IncidentCreatedEvent(CorrelationId correlationId,
                                String incidentId,
                                String incidentNumber,
                                String state,
                                String severity,
                                String originatingThreatId,
                                String primaryAssetId,
                                String primaryGridNodeId,
                                String description,
                                String assignedOperatorId,
                                String incidentCommanderId,
                                String source) {
        super("IncidentCreated", correlationId, "Incident", incidentId, 0,
                severity != null && (severity.equals("HIGH") || severity.equals("CRITICAL"))
                        ? EventSeverity.HIGH : EventSeverity.MEDIUM,
                "incident-management");
        this.incidentId = Objects.requireNonNull(incidentId);
        this.incidentNumber = incidentNumber;
        this.state = state;
        this.severity = severity;
        this.originatingThreatId = originatingThreatId;
        this.primaryAssetId = primaryAssetId;
        this.primaryGridNodeId = primaryGridNodeId;
        this.description = description;
        this.assignedOperatorId = assignedOperatorId;
        this.incidentCommanderId = incidentCommanderId;
    }

    @Override
    public String eventType() {
        return "IncidentCreated";
    }

    @Override
    protected Object payload() {
        return new Payload(
                incidentId,
                incidentNumber,
                state,
                severity,
                originatingThreatId,
                primaryAssetId,
                primaryGridNodeId,
                description,
                assignedOperatorId,
                incidentCommanderId
        );
    }

    public record Payload(
            String incidentId,
            String incidentNumber,
            String state,
            String severity,
            String originatingThreatId,
            String primaryAssetId,
            String primaryGridNodeId,
            String description,
            String assignedOperatorId,
            String incidentCommanderId
    ) {}
}