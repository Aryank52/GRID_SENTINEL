package com.gridsentinel.user;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Operator assignment to assets, regions, or shifts.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class OperatorAssignment {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String assignmentId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private AssignmentType type;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId assetId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId gridNodeId;

    @Setter(AccessLevel.PRIVATE)
    private String regionId;

    @Setter(AccessLevel.PRIVATE)
    private String shiftId;

    @Setter(AccessLevel.PRIVATE)
    private Instant validFrom;

    @Setter(AccessLevel.PRIVATE)
    private Instant validUntil;

    @Setter(AccessLevel.PRIVATE)
    private String notes;

    private OperatorAssignment(Builder builder) {
        this.assignmentId = Objects.requireNonNull(builder.assignmentId, "Assignment ID cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.assetId = builder.assetId;
        this.gridNodeId = builder.gridNodeId;
        this.regionId = builder.regionId;
        this.shiftId = builder.shiftId;
        this.validFrom = builder.validFrom != null ? builder.validFrom : Instant.now();
        this.validUntil = builder.validUntil;
        this.notes = builder.notes;
    }

    public boolean isActive(Instant at) {
        if (at.isBefore(validFrom)) return false;
        return validUntil == null || !at.isAfter(validUntil);
    }

    public boolean isActiveNow() {
        return isActive(Instant.now());
    }

    public enum AssignmentType {
        ASSET_RESPONSIBLE,    // Primary responsible for asset
        ASSET_BACKUP,         // Backup for asset
        REGION_OPERATOR,      // Covers geographic region
        SHIFT_DISPATCHER,     // Dispatcher for shift
        INCIDENT_RESPONDER,   // On-call for incidents
        INSPECTION_TEAM,      // Scheduled inspections
        EMERGENCY_CONTACT     // Emergency contact
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String assignmentId = "ASG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private AssignmentType type;
        private CommonKernel.AssetId assetId;
        private CommonKernel.GridNodeId gridNodeId;
        private String regionId;
        private String shiftId;
        private Instant validFrom;
        private Instant validUntil;
        private String notes;

        private Builder() {}

        public Builder assignmentId(String assignmentId) {
            this.assignmentId = Objects.requireNonNull(assignmentId);
            return this;
        }

        public Builder type(AssignmentType type) {
            this.type = type;
            return this;
        }

        public Builder assetId(CommonKernel.AssetId assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder gridNodeId(CommonKernel.GridNodeId gridNodeId) {
            this.gridNodeId = gridNodeId;
            return this;
        }

        public Builder regionId(String regionId) {
            this.regionId = regionId;
            return this;
        }

        public Builder shiftId(String shiftId) {
            this.shiftId = shiftId;
            return this;
        }

        public Builder validFrom(Instant validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder validUntil(Instant validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public OperatorAssignment build() {
            return new OperatorAssignment(this);
        }
    }
}