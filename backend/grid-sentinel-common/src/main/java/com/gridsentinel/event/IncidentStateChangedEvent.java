package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when an incident's state changes.
 * Emitted on every state transition in the incident lifecycle.
 */
public final class IncidentStateChangedEvent extends GridEvent {

    private final String incidentId;
    private final String incidentNumber;
    private final String previousState;
    private final String newState;
    private final String actorId;
    private final String reason;
    private final String correlationId;

    public IncidentStateChangedEvent(CorrelationId correlationId,
                                     String incidentId,
                                     String incidentNumber,
                                     String previousState,
                                     String newState,
                                     String actorId,
                                     String reason,
                                     String source) {
        super("IncidentStateChanged", correlationId, "Incident", incidentId, 1,
                determineSeverity(previousState, newState),
                "incident-management");
        this.incidentId = Objects.requireNonNull(incidentId);
        this.incidentNumber = incidentNumber;
        this.previousState = Objects.requireNonNull(previousState);
        this.newState = Objects.requireNonNull(newState);
        this.actorId = actorId;
        this.reason = reason;
        this.correlationId = correlationId != null ? correlationId.toString() : null;
    }

    private static EventSeverity determineSeverity(String previous, String next) {
        return switch (next) {
            case "ACTIVE_RESPONSE", "CONTAINED" -> EventSeverity.HIGH;
            case "RESOLVED", "CLOSED" -> EventSeverity.MEDIUM;
            case "FALSE_POSITIVE", "CANCELLED" -> EventSeverity.LOW;
            default -> EventSeverity.MEDIUM;
        };
    }

    @Override
    public String eventType() {
        return "IncidentStateChanged";
    }

    @Override
    protected Object payload() {
        return new Payload(
                incidentId,
                incidentNumber,
                previousState,
                newState,
                actorId,
                reason,
                correlationId
        );
    }

    public record Payload(
            String incidentId,
            String incidentNumber,
            String previousState,
            String newState,
            String actorId,
            String reason,
            String correlationId
    ) {}
}