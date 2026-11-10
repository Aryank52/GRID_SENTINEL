package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ResponsePriority;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Individual response action within a plan.
 * Trackable, assignable, with dependencies and status.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseAction {

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "action_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ResponseActionId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ActionType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ResponsePriority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ActionStatus status = ActionStatus.PENDING;

    @Convert(converter = ResponseActionIdSetConverter.class)
    @Column(name = "depends_on", length = 4000)
    @Setter(AccessLevel.PRIVATE)
    private Set<CommonKernel.ResponseActionId> dependsOn = new HashSet<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "target_asset_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId targetAssetId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "target_grid_node_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId targetGridNodeId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "assigned_to", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId assignedTo;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "reported_by", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId reportedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant plannedStartAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant plannedEndAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant actualStartAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant actualEndAt;

    @Setter(AccessLevel.PRIVATE)
    private String resultNotes;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "correlation_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    private ResponseAction(Builder builder) {
        this.id = builder.id != null ? builder.id : CommonKernel.ResponseActionId.generate();
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.type = Objects.requireNonNull(builder.type, "Action type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "Priority cannot be null");
        this.dependsOn = builder.dependsOn != null ? new HashSet<>(builder.dependsOn) : new HashSet<>();
        this.targetAssetId = builder.targetAssetId;
        this.targetGridNodeId = builder.targetGridNodeId;
        this.assignedTo = builder.assignedTo;
        this.reportedBy = builder.reportedBy;
        this.plannedStartAt = builder.plannedStartAt;
        this.plannedEndAt = builder.plannedEndAt;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
    }

    public void addDependency(CommonKernel.ResponseActionId actionId) {
        dependsOn.add(Objects.requireNonNull(actionId));
    }

    public void removeDependency(CommonKernel.ResponseActionId actionId) {
        dependsOn.remove(actionId);
    }

    public void start(CommonKernel.UserId startedBy) {
        if (status != ActionStatus.PENDING && status != ActionStatus.ASSIGNED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_STARTABLE",
                    "Cannot start action in status: " + status
            );
        }
        this.status = ActionStatus.IN_PROGRESS;
        this.actualStartAt = Instant.now();
        this.reportedBy = startedBy;
    }

    public void complete(CommonKernel.UserId completedBy, String resultNotes) {
        if (status != ActionStatus.IN_PROGRESS && status != ActionStatus.ON_HOLD) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_COMPLETABLE",
                    "Cannot complete action in status: " + status
            );
        }
        this.status = ActionStatus.COMPLETED;
        this.actualEndAt = Instant.now();
        this.resultNotes = resultNotes;
        this.reportedBy = completedBy;
    }

    public void hold() {
        if (status != ActionStatus.IN_PROGRESS) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_HOLDABLE",
                    "Cannot hold action in status: " + status
            );
        }
        this.status = ActionStatus.ON_HOLD;
    }

    public void resume() {
        if (status != ActionStatus.ON_HOLD) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_RESUMABLE",
                    "Cannot resume action in status: " + status
            );
        }
        this.status = ActionStatus.IN_PROGRESS;
    }

    public void skip(String reason) {
        if (status == ActionStatus.COMPLETED || status == ActionStatus.VERIFIED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_ALREADY_COMPLETED",
                    "Cannot skip action in status: " + status
            );
        }
        this.status = ActionStatus.SKIPPED;
        this.resultNotes = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseAction)) return false;
        ResponseAction that = (ResponseAction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ActionType {
        DEPLOY_CREW,
        ISOLATE_SECTION,
        RESTORE_POWER,
        ASSESS_DAMAGE,
        EVACUATE_AREA,
        SECURE_PERIMETER,
        REPAIR_EQUIPMENT,
        REPLACE_EQUIPMENT,
        VERIFY_RESTORATION,
        COMMUNICATE_PUBLIC,
        COORDINATE_AGENCIES,
        LOG_INCIDENT
    }

    public enum ActionStatus {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        VERIFIED,
        SKIPPED,
        FAILED
    }

    public static final class Builder {
        private CommonKernel.ResponseActionId id;
        private String name;
        private String description;
        private ActionType type;
        private ResponsePriority priority;
        private Set<CommonKernel.ResponseActionId> dependsOn = new HashSet<>();
        private CommonKernel.AssetId targetAssetId;
        private CommonKernel.GridNodeId targetGridNodeId;
        private CommonKernel.UserId assignedTo;
        private CommonKernel.UserId reportedBy;
        private Instant plannedStartAt;
        private Instant plannedEndAt;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.ResponseActionId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(ActionType type) {
            this.type = Objects.requireNonNull(type);
            return this;
        }

        public Builder priority(ResponsePriority priority) {
            this.priority = Objects.requireNonNull(priority);
            return this;
        }

        public Builder addDependency(CommonKernel.ResponseActionId actionId) {
            this.dependsOn.add(Objects.requireNonNull(actionId));
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

        public Builder reportedBy(CommonKernel.UserId reportedBy) {
            this.reportedBy = reportedBy;
            return this;
        }

        public Builder plannedStartAt(Instant plannedStartAt) {
            this.plannedStartAt = plannedStartAt;
            return this;
        }

        public Builder plannedEndAt(Instant plannedEndAt) {
            this.plannedEndAt = plannedEndAt;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ResponseAction build() {
            return new ResponseAction(this);
        }
    }
}