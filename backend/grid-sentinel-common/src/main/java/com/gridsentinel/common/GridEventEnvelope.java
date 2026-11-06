package com.gridsentinel.common;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable envelope wrapping all grid events for transport and audit.
 * Every grid event is wrapped in this envelope for consistent transport,
 * routing, and audit trail.
 */
public record GridEventEnvelope(
    String eventId,
    String eventType,
    Instant occurredAt,
    CorrelationId correlationId,
    String aggregateType,
    String aggregateId,
    Long version,
    EventSeverity severity,
    Object payload,
    String source
) {
    /**
     * Creates a new envelope with generated event ID and current timestamp.
     */
    public static <T> GridEventEnvelope of(
            String eventType,
            CorrelationId correlationId,
            String aggregateType,
            String aggregateId,
            Long version,
            EventSeverity severity,
            T payload,
            String source) {
        return new GridEventEnvelope(
                UUID.randomUUID().toString(),
                eventType,
                Instant.now(),
                correlationId,
                aggregateType,
                aggregateId,
                version,
                severity,
                payload,
                source
        );
    }

    /**
     * Creates a new envelope with all fields specified (for reconstruction/replay).
     */
    public static GridEventEnvelope of(
            String eventId,
            String eventType,
            Instant occurredAt,
            CorrelationId correlationId,
            String aggregateType,
            String aggregateId,
            Long version,
            EventSeverity severity,
            Object payload,
            String source) {
        return new GridEventEnvelope(
                eventId,
                eventType,
                occurredAt,
                correlationId,
                aggregateType,
                aggregateId,
                version,
                severity,
                payload,
                source
        );
    }

    /**
     * Checks if this envelope matches the given event type.
     */
    public boolean isType(String eventType) {
        return this.eventType.equals(eventType);
    }

    /**
     * Returns a new envelope with updated version (for replay/reprocessing).
     */
    public GridEventEnvelope withVersion(Long newVersion) {
        return new GridEventEnvelope(
                eventId, eventType, occurredAt, correlationId,
                aggregateType, aggregateId, newVersion, severity, payload, source
        );
    }

    /**
     * Returns a new envelope with updated correlation ID (for correlation chaining).
     */
    public GridEventEnvelope withCorrelationId(CorrelationId newCorrelationId) {
        return new GridEventEnvelope(
                eventId, eventType, occurredAt, correlationId,
                aggregateType, aggregateId, version, severity, payload, source
        );
    }

    /**
     * Returns a new envelope with updated source (for chained events).
     */
    public GridEventEnvelope withSource(String newSource) {
        return new GridEventEnvelope(
                eventId, eventType, occurredAt, correlationId,
                aggregateType, aggregateId, version, severity, payload, newSource
        );
    }
}