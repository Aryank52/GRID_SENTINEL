package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ResponsePriority;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
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

    @Setter(AccessLevel.PRIVATE)
    private UUID id;

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

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<UUID> dependsOn = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    private UUID targetAssetId;

    @Setter(AccessLevel.PRIVATE)
    private UUID targetGridNodeId;

    @Setter(AccessLevel.PRIVATE)
    private UUID assignedTo;

    @Setter(AccessLevel.PRIVATE)
    private UUID reportedBy;

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
    private UUID correlationId;

    private ResponseAction(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.type = Objects.requireNonNull(builder.type, "Action type cannot be null");
        this.priority = Objects.requireNonNull(builder.priority, "Priority cannot be null");
        this.dependsOn = builder.dependsOn != null ? Set.copyOf(builder.dependsOn) : new HashSet<>();
        this.targetAssetId = builder.targetAssetId;
        this.targetGridNodeId = builder.targetGridNodeId;
        this.assignedTo = builder.assignedTo;
        this.reportedBy = builder.reportedBy;
        this.plannedStartAt = builder.plannedStartAt;
        this.plannedEndAt = builder.plannedEndAt;
        this.correlationId = builder.correlationId != null ? builder.correlationId : UUID.randomUUID();
    }

    public void addDependency(UUID actionId) {
        dependsOn.add(Objects.requireNonNull(actionId));
    }

    public void removeDependency(UUID actionId) {
        dependsOn.remove(actionId);
    }

    public void start() {
        if (status != ActionStatus.PENDING && status != ActionStatus.ASSIGNED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_STARTABLE",
                    "Cannot start action in status: " + status
            );
        }
        this.status = ActionStatus.IN_PROGRESS;
        this.actualStartAt = Instant.now();
    }

    public void complete(String resultNotes) {
        if (status != ActionStatus.IN_PROGRESS && status != ActionStatus.ON_HOLD) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "ACTION_NOT_COMPLETABLE",
                    "Cannot complete action in status: " + status
            );
        }
        this.status = ActionStatus.COMPLETED;
        this.actualEndAt = Instant.now();
        this.resultNotes = resultNotes;
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

    public static final class Builder {
        private UUID id;
        private String name;
        private String description;
        private ActionType type;
        private ResponsePriority priority;
        private Set<UUID> dependsOn = new HashSet<>();
        private UUID targetAssetId;
        private UUID targetGridNodeId;
        private UUID assignedTo;
        private UUID reportedBy;
        private Instant plannedStartAt;
        private Instant plannedEndAt;
        private UUID correlationId;

        private Builder() {}

        public Builder id(UUID id) {
            this.id = id;
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

        public Builder addDependency(UUID actionId) {
            this.dependsOn.add(Objects.requireNonNull(actionId));
            return this;
        }

        public Builder targetAssetId(UUID targetAssetId) {
            this.targetAssetId = targetAssetId;
            return this;
        }

        public Builder targetGridNodeId(UUID targetGridNodeId) {
            this.targetGridNodeId = targetGridNodeId;
            return this;
        }

        public Builder assignedTo(UUID assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public Builder reportedBy(UUID reportedBy) {
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

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ResponseAction build() {
            return new ResponseAction(this);
        }
    }
}