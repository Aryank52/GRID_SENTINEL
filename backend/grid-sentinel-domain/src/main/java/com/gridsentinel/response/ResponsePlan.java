package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Prioritized response plan for an incident.
 * Contains ordered response actions with assignments and dependencies.
 */
@Entity
@Table(name = "response_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponsePlan {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ResponsePlanId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.IncidentId incidentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private PlanStatus status = PlanStatus.DRAFT;

    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private List<ResponseAction> actions = new ArrayList<>();

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId createdBy;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId approvedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant approvedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant completedAt;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private ResponsePlan(Builder builder) {
        this.id = builder.id;
        this.incidentId = Objects.requireNonNull(builder.incidentId, "Incident ID cannot be null");
        this.name = builder.name;
        this.description = builder.description;
        this.actions = builder.actions != null ? new ArrayList<>(builder.actions) : new ArrayList<>();
        this.createdBy = builder.createdBy;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.version = 0;
    }

    public void addAction(ResponseAction action) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot add actions to plan in status: " + status
            );
        }
        actions.add(Objects.requireNonNull(action));
        this.version++;
    }

    public void removeAction(CommonKernel.ResponseActionId actionId) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot remove actions from plan in status: " + status
            );
        }
        actions.removeIf(a -> a.getId().equals(actionId));
        this.version++;
    }

    public void approve(CommonKernel.UserId approverId) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot approve plan in status: " + status
            );
        }
        if (actions.isEmpty()) {
            throw new CommonKernel.ValidationException(
                    "PLAN_EMPTY",
                    "Cannot approve empty response plan"
            );
        }
        this.status = PlanStatus.APPROVED;
        this.approvedBy = Objects.requireNonNull(approverId);
        this.approvedAt = Instant.now();
        this.version++;
    }

    public void start() {
        if (status != PlanStatus.APPROVED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_APPROVED",
                    "Cannot start plan in status: " + status
            );
        }
        this.status = PlanStatus.ACTIVE;
        this.startedAt = Instant.now();
        this.version++;
    }

    public void complete() {
        if (status != PlanStatus.ACTIVE) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_ACTIVE",
                    "Cannot complete plan in status: " + status
            );
        }
        // Check all actions completed
        boolean allComplete = actions.stream().allMatch(ResponseAction::isCompleted);
        if (!allComplete) {
            throw new CommonKernel.ValidationException(
                    "ACTIONS_INCOMPLETE",
                    "Not all actions are completed"
            );
        }
        this.status = PlanStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.version++;
    }

    public List<ResponseAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<ResponseAction> getActionsByPriority() {
        return actions.stream()
                .sorted((a, b) -> a.getPriority().compareTo(b.getPriority()))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponsePlan)) return false;
        ResponsePlan that = (ResponsePlan) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum PlanStatus {
        DRAFT,
        APPROVED,
        ACTIVE,
        COMPLETED,
        SUPERSEDED,
        CANCELLED
    }

    public static final class Builder {
        private CommonKernel.ResponsePlanId id = CommonKernel.ResponsePlanId.generate();
        private CommonKernel.IncidentId incidentId;
        private String name;
        private String description;
        private List<ResponseAction> actions = new ArrayList<>();
        private CommonKernel.UserId createdBy;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.ResponsePlanId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder incidentId(CommonKernel.IncidentId incidentId) {
            this.incidentId = incidentId;
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

        public Builder addAction(ResponseAction action) {
            this.actions.add(Objects.requireNonNull(action));
            return this;
        }

        public Builder createdBy(CommonKernel.UserId createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ResponsePlan build() {
            return new ResponsePlan(this);
        }
    }
}