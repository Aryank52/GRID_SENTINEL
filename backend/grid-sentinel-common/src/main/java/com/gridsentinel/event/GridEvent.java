package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;
import com.gridsentinel.common.GridEventEnvelope;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all grid events.
 * Events are immutable and carry all information needed for transport, routing, and audit.
 * 
 * @param <P> the type of the event payload
 */
public abstract sealed class GridEvent<P> permits
        ThreatDetectedEvent,
        ThreatEscalatedEvent,
        IncidentCreatedEvent,
        IncidentStateChangedEvent,
        RiskScoreUpdatedEvent,
        SimulationCompletedEvent,
        ResponsePlanUpdatedEvent,
        RecoveryProgressUpdatedEvent,
        GridAssetStatusChangedEvent {

    private final String eventId;
    private final Instant occurredAt;
    private final CorrelationId correlationId;
    private final String aggregateType;
    private final String aggregateId;
    private final long version;
    private final EventSeverity severity;
    private final String source;

    protected GridEvent(String eventType, CorrelationId correlationId,
                        String aggregateType, String aggregateId,
                        long version, EventSeverity severity, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
        this.correlationId = Objects.requireNonNull(correlationId);
        this.aggregateType = Objects.requireNonNull(aggregateType);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.version = version;
        this.severity = severity;
        this.source = source;
    }

    protected GridEvent(String eventId, Instant occurredAt, CorrelationId correlationId,
                        String aggregateType, String aggregateId, long version,
                        EventSeverity severity, String source) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.correlationId = Objects.requireNonNull(correlationId);
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.version = version;
        this.severity = severity;
        this.source = source;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public CorrelationId correlationId() {
        return correlationId;
    }

    public String aggregateType() {
        return aggregateType;
    }

    public String aggregateId() {
        return aggregateId;
    }

    public long version() {
        return version;
    }

    public EventSeverity severity() {
        return severity;
    }

    public String source() {
        return source;
    }

    public abstract String eventType();

    public abstract P payload();

    public GridEventEnvelope<P> toEnvelope() {
        return GridEventEnvelope.of(
                eventId,
                eventType(),
                occurredAt,
                correlationId(),
                aggregateType(),
                aggregateId(),
                version,
                severity(),
                payload(),
                source
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridEvent)) return false;
        GridEvent<?> that = (GridEvent<?>) o;
        return eventId != null && eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return eventId != null ? eventId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + eventId + "]";
    }
}