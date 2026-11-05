package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseAction {

    @Id
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
    private CommonKernel.ResponsePriority priority;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ActionStatus status = ActionStatus.PENDING;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Set<CommonKernel.ResponseActionId> dependsOn = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId targetAssetId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId targetGridNodeId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId assignedTo;

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

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    private ResponseAction(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "Priority cannot be null");
        this.dependsOn = Set.copyOf(builder.dependsOn);
        this.targetAssetId = builder.targetAssetId;
        this.targetGridNodeId = builder.targetGridNodeId;
        this.assignedTo = builder.assignedTo;
        this.plannedStartAt = builder.plannedStartAt;
        this.plannedEndAt = builder.plannedEndAt;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
    }

    public void assign(CommonKernel.UserId userId) {
        this.assignedTo = Objects.requireNonNull(userId);
    }

    public void start(CommonKernel.UserId userId) {
        if (status != ActionStatus.PENDING && status != ActionStatus.ASSIGNED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_STARTABLE",
                    "Cannot start action in status: " + status
            );
        }
        this.status = ActionStatus.IN_PROGRESS;
        this.actualStartAt = Instant.now();
        this.reportedBy = userId;
    }

    public void complete(CommonKernel.UserId userId, String notes) {
        if (status != ActionStatus.IN_PROGRESS) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_IN_PROGRESS",
                    "Cannot complete action in status: " + status
            );
        }
        this.status = ActionStatus.COMPLETED;
        this.actualEndAt = Instant.now();
        this.resultNotes = notes;
        this.reportedBy = userId;
    }

    public void fail(CommonKernel.UserId userId, String notes) {
        if (status != ActionStatus.IN_PROGRESS) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_IN_PROGRESS",
                    "Cannot fail action in status: " + status
            );
        }
        this.status = ActionStatus.FAILED;
        this.actualEndAt = Instant.now();
        this.resultNotes = notes;
        this.reportedBy = userId;
    }

    public void skip(String reason) {
        if (status == ActionStatus.COMPLETED || status == ActionStatus.FAILED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_ALREADY_TERMINAL",
                    "Cannot skip completed/failed action"
            );
        }
        this.status = ActionStatus.SKIPPED;
        this.resultNotes = reason;
    }

    public boolean isCompleted() {
        return status == ActionStatus.COMPLETED;
    }

    public boolean isTerminal() {
        return status == ActionStatus.COMPLETED || status == ActionStatus.FAILED || status == ActionStatus.SKIPPED;
    }

    public boolean canStart(Set<CommonKernel.ResponseActionId> completedActionIds) {
        return dependsOn.stream().allMatch(completedActionIds::contains);
    }

    public Set<CommonKernel.ResponseActionId> getDependsOn() {
        return Collections.unmodifiableSet(dependsOn);
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ActionType {
        // Physical response
        ISOLATE_ASSET,
        SWITCH_LOAD,
        DEPLOY_CREW,
        DEPLOY_EQUIPMENT,
        INSTALL_TEMPORARY_FIX,
        EVACUATE_AREA,
        ESTABLISH_PERIMETER,

        // Operational
        SHED_LOAD,
        RECONFIGURE_NETWORK,
        ACTIVATE_BACKUP,
        COORDINATE_WITH_OPERATOR,
        NOTIFY_AUTHORITIES,
        NOTIFY_CUSTOMERS,

        // Assessment
        CONDITION_ASSESSMENT,
        DAMAGE_SURVEY,
        ENVIRONMENTAL_MONITORING,

        // Recovery
        REPAIR_ASSET,
        REPLACE_ASSET,
        RESTORE_SERVICE,
        VALIDATE_RESTORATION,

        // Communication
        SITUATION_REPORT,
        MEDIA_BRIEFING,
        STAKEHOLDER_UPDATE
    }

    public enum ActionStatus {
        PENDING,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED,
        BLOCKED
    }

    public static final class Builder {
        private CommonKernel.ResponseActionId id = CommonKernel.ResponseActionId.generate();
        private String name;
        private String description;
        private ActionType type;
        private CommonKernel.ResponsePriority priority;
        private Set<CommonKernel.ResponseActionId> dependsOn = new HashSet<>();
        private CommonKernel.AssetId targetAssetId;
        private CommonKernel.GridNodeId targetGridNodeId;
        private CommonKernel.UserId assignedTo;
        private Instant plannedStartAt;
        private Instant plannedEndAt;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.ResponseActionId id) {
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

        public Builder type(ActionType type) {
            this.type = type;
            return this;
        }

        public Builder priority(CommonKernel.ResponsePriority priority) {
            this.priority = priority;
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