package com.gridsentinel.observation;

import com.gridsentinel.common.CommonKernel;
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
import java.util.Objects;
import java.util.UUID;

/**
 * Time-bound evidence from an observation source.
 * <p>
 * Invariants:
 * <ul>
 *   <li>Timestamp must not be in the future (beyond clock skew tolerance)</li>
 *   <li>Source type must be specified</li>
 *   <li>Payload must be valid for the source type</li>
 *   <li>Geometry (if present) must be SRID 4326</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "observations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Observation {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ObservationId id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ObservationSourceType sourceType;

    @NotNull
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String sourceIdentifier; // e.g., camera ID, sensor ID, operator badge

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Instant timestamp; // When the observation was made

    @Setter(AccessLevel.PRIVATE)
    private Instant receivedAt; // When the system received it

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String payload; // JSON payload, structure depends on sourceType

    @Setter(AccessLevel.PRIVATE)
    private String contentHash; // SHA-256 of payload for integrity

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId associatedAssetId; // Asset directly observed

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId nearbyGridNodeId; // Nearest grid node

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject geometry; // Location of observation

    @Setter(AccessLevel.PRIVATE)
    private ValidationStatus validationStatus = ValidationStatus.PENDING;

    @Setter(AccessLevel.PRIVATE)
    private String validationNotes;

    @Setter(AccessLevel.PRIVATE)
    private Instant validatedAt;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId validatedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private Observation(Builder builder) {
        this.id = builder.id;
        this.sourceType = Objects.requireNonNull(builder.sourceType, "Source type cannot be null");
        this.sourceIdentifier = Objects.requireNonNull(builder.sourceIdentifier, "Source identifier cannot be null");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp cannot be null");
        this.receivedAt = builder.receivedAt != null ? builder.receivedAt : Instant.now();
        this.payload = Objects.requireNonNull(builder.payload, "Payload cannot be null");
        this.contentHash = builder.contentHash != null ? builder.contentHash : computeHash(builder.payload);
        this.associatedAssetId = builder.associatedAssetId;
        this.nearbyGridNodeId = builder.nearbyGridNodeId;
        this.geometry = builder.geometry;
        this.createdAt = Instant.now();
        this.version = 0;

        // Validate timestamp not significantly in future
        Instant now = Instant.now();
        if (this.timestamp.isAfter(now.plusSeconds(300))) { // 5 min clock skew tolerance
            throw new IllegalArgumentException("Observation timestamp cannot be in the future");
        }
    }

    private static String computeHash(String payload) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute content hash", e);
        }
    }

    public void markValidated(ValidationStatus status, String notes, CommonKernel.UserId validatedBy) {
        this.validationStatus = Objects.requireNonNull(status);
        this.validationNotes = notes;
        this.validatedAt = Instant.now();
        this.validatedBy = Objects.requireNonNull(validatedBy);
        this.version++;
    }

    public boolean isValidated() {
        return validationStatus == ValidationStatus.VALID;
    }

    public boolean isRejected() {
        return validationStatus == ValidationStatus.REJECTED;
    }

    public boolean verifyIntegrity() {
        return contentHash.equals(computeHash(payload));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Observation)) return false;
        Observation that = (Observation) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ValidationStatus {
        PENDING,
        VALID,
        REJECTED,
        REQUIRES_REVIEW
    }

    public static final class Builder {
        private CommonKernel.ObservationId id = CommonKernel.ObservationId.generate();
        private CommonKernel.ObservationSourceType sourceType;
        private String sourceIdentifier;
        private Instant timestamp;
        private Instant receivedAt;
        private String payload;
        private String contentHash;
        private CommonKernel.AssetId associatedAssetId;
        private CommonKernel.GridNodeId nearbyGridNodeId;
        private CommonKernel.GeometryValueObject geometry;

        private Builder() {}

        public Builder id(CommonKernel.ObservationId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder sourceType(CommonKernel.ObservationSourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder sourceIdentifier(String sourceIdentifier) {
            this.sourceIdentifier = sourceIdentifier;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder receivedAt(Instant receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder contentHash(String contentHash) {
            this.contentHash = contentHash;
            return this;
        }

        public Builder associatedAssetId(CommonKernel.AssetId associatedAssetId) {
            this.associatedAssetId = associatedAssetId;
            return this;
        }

        public Builder nearbyGridNodeId(CommonKernel.GridNodeId nearbyGridNodeId) {
            this.nearbyGridNodeId = nearbyGridNodeId;
            return this;
        }

        public Builder geometry(CommonKernel.GeometryValueObject geometry) {
            this.geometry = geometry;
            return this;
        }

        public Observation build() {
            return new Observation(this);
        }
    }
}