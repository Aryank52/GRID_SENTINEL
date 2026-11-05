package com.gridsentinel.recovery;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Individual recovery milestone with verification requirement.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecoveryMilestone {

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private MilestoneType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.RecoveryStatus status = CommonKernel.RecoveryStatus.NOT_STARTED;

    @Setter(AccessLevel.PRIVATE)
    private Integer sequenceOrder;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId targetAssetId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId targetGridNodeId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId assignedTo;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId verifiedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant plannedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant completedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant verifiedAt;

    @Setter(AccessLevel.PRIVATE)
    private String verificationNotes;

    @Setter(AccessLevel.PRIVATE)
    private String evidenceRef; // Reference to photos, documents, etc.

    private RecoveryMilestone(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "ID cannot be null");
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.sequenceOrder = builder.sequenceOrder;
        this.targetAssetId = builder.targetAssetId;
        this.targetGridNodeId = builder.targetGridNodeId;
        this.assignedTo = builder.assignedTo;
        this.plannedAt = builder.plannedAt;
    }

    public void start(CommonKernel.UserId userId) {
        if (status != CommonKernel.RecoveryStatus.NOT_STARTED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "MILESTONE_ALREADY_STARTED",
                    "Milestone already in status: " + status
            );
        }
        this.status = CommonKernel.RecoveryStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
        this.assignedTo = userId;
    }

    public void complete(CommonKernel.UserId verifiedBy, String verificationNotes) {
        if (status != CommonKernel.RecoveryStatus.IN_PROGRESS) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "MILESTONE_NOT_IN_PROGRESS",
                    "Cannot complete milestone in status: " + status
            );
        }
        this.status = CommonKernel.RecoveryStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.verifiedBy = verifiedBy;
        this.verificationNotes = verificationNotes;
    }

    public void verify(CommonKernel.UserId verifiedBy, String verificationNotes) {
        if (status != CommonKernel.RecoveryStatus.COMPLETED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "MILESTONE_NOT_COMPLETED",
                    "Cannot verify milestone in status: " + status
            );
        }
        this.status = CommonKernel.RecoveryStatus.VERIFIED;
        this.verifiedAt = Instant.now();
        this.verifiedBy = verifiedBy;
        this.verificationNotes = verificationNotes;
    }

    public void block(String reason) {
        if (status == CommonKernel.RecoveryStatus.COMPLETED || status == CommonKernel.RecoveryStatus.VERIFIED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "MILESTONE_ALREADY_COMPLETED",
                    "Cannot block completed milestone"
            );
        }
        this.status = CommonKernel.RecoveryStatus.BLOCKED;
        this.verificationNotes = "BLOCKED: " + reason;
    }

    public boolean isCompleted() {
        return status == CommonKernel.RecoveryStatus.COMPLETED || status == CommonKernel.RecoveryStatus.VERIFIED;
    }

    public boolean isVerified() {
        return status == CommonKernel.RecoveryStatus.VERIFIED;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum MilestoneType {
        DAMAGE_ASSESSMENT,
        SITE_SECURITY,
        TEMPORARY_RESTORATION,
        PERMANENT_REPAIR,
        EQUIPMENT_REPLACEMENT,
        TESTING_COMMISSIONING,
        LOAD_RESTORATION,
        CUSTOMER_NOTIFICATION,
        ENVIRONMENTAL_CLEANUP,
        DOCUMENTATION,
        AFTER_ACTION_REVIEW,
        LESSONS_LEARNED_CAPTURE
    }

    public static final class Builder {
        private String id = "MS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        private String name;
        private String description;
        private MilestoneType type;
        private Integer sequenceOrder;
        private CommonKernel.AssetId targetAssetId;
        private CommonKernel.GridNodeId targetGridNodeId;
        private CommonKernel.UserId assignedTo;
        private Instant plannedAt;

        private Builder() {}

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(MilestoneType type) {
            this.type = type;
            return this;
        }

        public Builder sequenceOrder(int sequenceOrder) {
            this.sequenceOrder = sequenceOrder;
            return this;
        }

        public Builder targetAssetId(CommonKernel.AssetId targetAssetId) {
            this.targetAssetId = targetAssetId;
            return this;
        }

        public Builder targetGridNodeId(CommonKernel.GridNodeId targetGridNodeId) {
            this.targetGridNodeId = targetGridNodeId;
            return this;
        }

        public Builder assignedTo(CommonKernel.UserId assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public Builder plannedAt(Instant plannedAt) {
            this.plannedAt = plannedAt;
            return this;
        }

        public RecoveryMilestone build() {
            return new RecoveryMilestone(this);
        }
    }
}