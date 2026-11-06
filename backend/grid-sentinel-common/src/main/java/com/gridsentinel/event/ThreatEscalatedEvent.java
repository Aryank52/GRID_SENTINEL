package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a threat's severity is escalated.
 * Emitted when ThreatEvent.escalateSeverity() is called.
 */
public final class ThreatEscalatedEvent extends GridEvent {

    private final String threatEventId;
    private final String previousSeverity;
    private final String newSeverity;
    private final String escalationReason;

    public ThreatEscalatedEvent(CorrelationId correlationId,
                                String threatEventId,
                                String previousSeverity,
                                String newSeverity,
                                String escalationReason,
                                String source) {
        super("ThreatEscalated", correlationId, "ThreatEvent", threatEventId, 1,
                EventSeverity.HIGH, "threat-pipeline");
        this.threatEventId = Objects.requireNonNull(threatEventId);
        this.previousSeverity = Objects.requireNonNull(previousSeverity);
        this.newSeverity = Objects.requireNonNull(newSeverity);
        this.escalationReason = escalationReason;
    }

    @Override
    public String eventType() {
        return "ThreatEscalated";
    }

    @Override
    protected Object payload() {
        return new Payload(threatEventId, previousSeverity, newSeverity, escalationReason);
    }

    public record Payload(
            String threatEventId,
            String previousSeverity,
            String newSeverity,
            String escalationReason
    ) {}
}