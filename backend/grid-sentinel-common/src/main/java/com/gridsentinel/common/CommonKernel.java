package com.gridsentinel.common;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Shared kernel value objects, base types, and utilities used across all modules.
 */
public final class CommonKernel {

    private CommonKernel() {}

    // =========================================================================
    // Base Identifiers
    // =========================================================================

    /**
     * Base interface for all domain identifiers.
     * Uses UUID for global uniqueness and sharding safety.
     */
    public interface DomainId extends Serializable {
        UUID getValue();
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AssetId implements DomainId {
        @NotNull UUID value;

        public static AssetId of(UUID value) {
            return new AssetId(Objects.requireNonNull(value, "AssetId value cannot be null"));
        }

        public static AssetId generate() {
            return new AssetId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GridNodeId implements DomainId {
        @NotNull UUID value;

        public static GridNodeId of(UUID value) {
            return new GridNodeId(Objects.requireNonNull(value, "GridNodeId value cannot be null"));
        }

        public static GridNodeId generate() {
            return new GridNodeId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class GridEdgeId implements DomainId {
        @NotNull UUID value;

        public static GridEdgeId of(UUID value) {
            return new GridEdgeId(Objects.requireNonNull(value, "GridEdgeId value cannot be null"));
        }

        public static GridEdgeId generate() {
            return new GridEdgeId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ObservationId implements DomainId {
        @NotNull UUID value;

        public static ObservationId of(UUID value) {
            return new ObservationId(Objects.requireNonNull(value, "ObservationId value cannot be null"));
        }

        public static ObservationId generate() {
            return new ObservationId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RiskScoreId implements DomainId {
        @NotNull UUID value;

        public static RiskScoreId of(UUID value) {
            return new RiskScoreId(Objects.requireNonNull(value, "RiskScoreId value cannot be null"));
        }

        public static RiskScoreId generate() {
            return new RiskScoreId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ThreatEventId implements DomainId {
        @NotNull UUID value;

        public static ThreatEventId of(UUID value) {
            return new ThreatEventId(Objects.requireNonNull(value, "ThreatEventId value cannot be null"));
        }

        public static ThreatEventId generate() {
            return new ThreatEventId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class IncidentId implements DomainId {
        @NotNull UUID value;

        public static IncidentId of(UUID value) {
            return new IncidentId(Objects.requireNonNull(value, "IncidentId value cannot be null"));
        }

        public static IncidentId generate() {
            return new IncidentId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class SimulationId implements DomainId {
        @NotNull UUID value;

        public static SimulationId of(UUID value) {
            return new SimulationId(Objects.requireNonNull(value, "SimulationId value cannot be null"));
        }

        public static SimulationId generate() {
            return new SimulationId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ResponsePlanId implements DomainId {
        @NotNull UUID value;

        public static ResponsePlanId of(UUID value) {
            return new ResponsePlanId(Objects.requireNonNull(value, "ResponsePlanId value cannot be null"));
        }

        public static ResponsePlanId generate() {
            return new ResponsePlanId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ResponseActionId implements DomainId {
        @NotNull UUID value;

        public static ResponseActionId of(UUID value) {
            return new ResponseActionId(Objects.requireNonNull(value, "ResponseActionId value cannot be null"));
        }

        public static ResponseActionId fromString(String value) {
            return new ResponseActionId(UUID.fromString(value));
        }

        public static ResponseActionId generate() {
            return new ResponseActionId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class RecoveryStateId implements DomainId {
        @NotNull UUID value;

        public static RecoveryStateId of(UUID value) {
            return new RecoveryStateId(Objects.requireNonNull(value, "RecoveryStateId value cannot be null"));
        }

        public static RecoveryStateId generate() {
            return new RecoveryStateId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ProtectionPlanId implements DomainId {
        @NotNull UUID value;

        public static ProtectionPlanId of(UUID value) {
            return new ProtectionPlanId(Objects.requireNonNull(value, "ProtectionPlanId value cannot be null"));
        }

        public static ProtectionPlanId generate() {
            return new ProtectionPlanId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class AuditLogId implements DomainId {
        @NotNull UUID value;

        public static AuditLogId of(UUID value) {
            return new AuditLogId(Objects.requireNonNull(value, "AuditLogId value cannot be null"));
        }

        public static AuditLogId generate() {
            return new AuditLogId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UserId implements DomainId {
        @NotNull UUID value;

        public static UserId of(UUID value) {
            return new UserId(Objects.requireNonNull(value, "UserId value cannot be null"));
        }

        public static UserId generate() {
            return new UserId(UUID.randomUUID());
        }

        public UUID value() { return value; }
    }

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CorrelationId implements DomainId {
        @NotNull UUID value;

        public static CorrelationId of(UUID value) {
            return new CorrelationId(Objects.requireNonNull(value, "CorrelationId value cannot be null"));
        }

        public static CorrelationId generate() {
            return new CorrelationId(UUID.randomUUID());
        }

        public static CorrelationId fromString(String value) {
            return new CorrelationId(UUID.fromString(value));
        }

        public UUID value() { return value; }
    }

    // =========================================================================
    // Value Objects
    // =========================================================================

    /**
     * Immutable time range with inclusive start and exclusive end.
     */
    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class TimeRange implements Serializable {
        @NotNull Instant start;
        Instant end; // nullable for open-ended ranges

        public static TimeRange of(Instant start, Instant end) {
            Objects.requireNonNull(start, "Start cannot be null");
            if (end != null && end.isBefore(start)) {
                throw new IllegalArgumentException("End cannot be before start");
            }
            return new TimeRange(start, end);
        }

        public static TimeRange startingAt(Instant start) {
            return new TimeRange(Objects.requireNonNull(start, "Start cannot be null"), null);
        }

        public boolean contains(Instant instant) {
            if (instant.isBefore(start)) return false;
            return end == null || instant.isBefore(end);
        }

        public boolean overlaps(TimeRange other) {
            if (this.end == null && other.end == null) return true;
            if (this.end == null) return other.start.isBefore(this.start) || other.end == null || other.end.isAfter(this.start);
            if (other.end == null) return this.start.isBefore(other.start) || this.end == null || this.end.isAfter(other.start);
            return this.start.isBefore(other.end) && other.start.isBefore(this.end);
        }

        public Instant start() { return start; }
        public Instant end() { return end; }
    }

    /**
     * Geographic coordinate (longitude, latitude) in WGS84.
     */
    @Embeddable
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Coordinate implements Serializable {
        @NotNull Double longitude; // [-180, 180]
        @NotNull Double latitude;  // [-90, 90]

        public static Coordinate of(double longitude, double latitude) {
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude must be in [-180, 180]");
            }
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude must be in [-90, 90]");
            }
            return new Coordinate(longitude, latitude);
        }

        public Point toPoint() {
            // Implementation would use JTS GeometryFactory
            // Returned Point would have SRID 4326
            throw new UnsupportedOperationException("Requires GeometryFactory - use GeometryValueObject instead");
        }

        public Double longitude() { return longitude; }
        public Double latitude() { return latitude; }
    }

    /**
     * Wrapper for JTS Geometry to provide value semantics and validation.
     */
    @Getter
    @EqualsAndHashCode
    @ToString
    public static final class GeometryValueObject implements Serializable {
        Geometry geometry;

        GeometryValueObject(Geometry geometry) {
            this.geometry = Objects.requireNonNull(geometry, "Geometry cannot be null");
            if (geometry.getSRID() != 4326) {
                throw new IllegalArgumentException("Geometry must use SRID 4326 (WGS84)");
            }
            if (!geometry.isValid()) {
                throw new IllegalArgumentException("Geometry must be valid");
            }
        }

        public static GeometryValueObject of(Geometry geometry) {
            return new GeometryValueObject(geometry);
        }

        public static GeometryValueObject ofPoint(double longitude, double latitude) {
            // Would use GeometryFactory.createPoint(new Coordinate(longitude, latitude))
            throw new UnsupportedOperationException("Requires GeometryFactory");
        }

        public static GeometryValueObject ofPolygon(Coordinate... coordinates) {
            // Would use GeometryFactory.createPolygon(...)
            throw new UnsupportedOperationException("Requires GeometryFactory");
        }

        public boolean intersects(GeometryValueObject other) {
            return geometry.intersects(other.geometry);
        }

        public boolean contains(GeometryValueObject other) {
            return geometry.contains(other.geometry);
        }

        public double distance(GeometryValueObject other) {
            return geometry.distance(other.geometry);
        }

        public Geometry geometry() { return geometry; }
    }

    // =========================================================================
    // Enums / Sealed Interfaces
    // =========================================================================

    /**
     * Asset criticality levels per grid resilience standards.
     */
    public enum Criticality {
        LOW,          // Non-critical, redundant, easily replaceable
        MEDIUM,       // Important but with redundancy
        HIGH,         // Critical, limited redundancy
        MISSION_CRITICAL // No redundancy, cascading failure risk
    }

    /**
     * Asset health index (0.0 - 1.0).
     */
    @Getter
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HealthIndex implements Serializable {
        @NotNull Double value; // [0.0, 1.0]

        public static HealthIndex of(double value) {
            if (value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException("HealthIndex must be in [0.0, 1.0]");
            }
            return new HealthIndex(value);
        }

        public static HealthIndex healthy() {
            return new HealthIndex(1.0);
        }

        public static HealthIndex degraded() {
            return new HealthIndex(0.5);
        }

        public static HealthIndex critical() {
            return new HealthIndex(0.1);
        }

        public boolean isHealthy() {
            return value >= 0.8;
        }

        public boolean isDegraded() {
            return value >= 0.3 && value < 0.8;
        }

        public boolean isCritical() {
            return value < 0.3;
        }

        public Double value() { return value; }
    }

    /**
     * Risk score bands for consistent classification.
     */
    public enum RiskBand {
        NEGLIGIBLE(0.0, 0.1),
        LOW(0.1, 0.3),
        MEDIUM(0.3, 0.6),
        HIGH(0.6, 0.8),
        CRITICAL(0.8, 1.0);

        private final double minInclusive;
        private final double maxExclusive;

        RiskBand(double minInclusive, double maxExclusive) {
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
        }

        public static RiskBand fromScore(double score) {
            for (RiskBand band : values()) {
                if (score >= band.minInclusive && score < band.maxExclusive) {
                    return band;
                }
            }
            return CRITICAL; // score >= 1.0 or edge case
        }
    }

    // =========================================================================
    // Exception Hierarchy
    // =========================================================================

    public static class DomainException extends RuntimeException {
        private final String code;
        private final Object[] args;

        public DomainException(String code, String message) {
            super(message);
            this.code = code;
            this.args = new Object[0];
        }

        public DomainException(String code, String message, Object... args) {
            super(String.format(message, args));
            this.code = code;
            this.args = args;
        }

        public String getCode() { return code; }
        public Object[] getArgs() { return args; }
    }

    public static class ValidationException extends DomainException {
        public ValidationException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    public static class NotFoundException extends DomainException {
        public NotFoundException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    public static class ConflictException extends DomainException {
        public ConflictException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    public static class IllegalStateTransitionException extends DomainException {
        public IllegalStateTransitionException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    public static class SimulationException extends DomainException {
        public SimulationException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    public static class ModelInferenceException extends DomainException {
        public ModelInferenceException(String code, String message, Object... args) {
            super(code, message, args);
        }
    }

    // =========================================================================
    // Domain Event Base
    // =========================================================================

    /**
     * Base interface for all domain events.
     * Events are immutable, timestamped, and carry correlation ID for tracing.
     */
    public interface DomainEvent {
        Instant occurredAt();
        CorrelationId correlationId();
        String eventType();
    }

    /**
     * Abstract base for domain events with common fields.
     */
    @Getter
    @EqualsAndHashCode
    @ToString
    public abstract static class AbstractDomainEvent implements DomainEvent {
        private final Instant occurredAt;
        private final CorrelationId correlationId;

        protected AbstractDomainEvent(CorrelationId correlationId) {
            this.occurredAt = Instant.now();
            this.correlationId = Objects.requireNonNull(correlationId);
        }

        protected AbstractDomainEvent(Instant occurredAt, CorrelationId correlationId) {
            this.occurredAt = Objects.requireNonNull(occurredAt);
            this.correlationId = Objects.requireNonNull(correlationId);
        }

        @Override
        public Instant occurredAt() {
            return getOccurredAt();
        }

        @Override
        public CorrelationId correlationId() {
            return getCorrelationId();
        }

        @Override
        public abstract String eventType();
    }
}