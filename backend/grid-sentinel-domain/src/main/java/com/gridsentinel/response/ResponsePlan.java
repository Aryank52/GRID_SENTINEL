package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "response_plan_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ResponsePlanId id;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "incident_id", nullable = false, columnDefinition = "uuid"))
    })
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "response_actions", joinColumns = @JoinColumn(name = "response_plan_id"))
    @OrderColumn(name = "action_order")
    @Setter(AccessLevel.PRIVATE)
    private List<ResponseAction> actions = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "created_by", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId createdBy;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "approved_by", columnDefinition = "uuid"))
    })
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

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "correlation_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private ResponsePlan(Builder builder) {
        this.id = builder.id != null ? builder.id : CommonKernel.ResponsePlanId.generate();
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
        if (status != PlanStatus.DRAFT && status != PlanStatus.PENDING_APPROVAL) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_MODIFIABLE",
                    "Cannot add actions to plan in status: " + status
            );
        }
        actions.add(Objects.requireNonNull(action));
        this.version++;
    }

    public void removeAction(CommonKernel.ResponseActionId actionId) {
        if (status != PlanStatus.DRAFT && status != PlanStatus.PENDING_APPROVAL) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_MODIFIABLE",
                    "Cannot remove actions from plan in status: " + status
            );
        }
        actions.removeIf(a -> a.getId().equals(actionId));
        this.version++;
    }

    public void submitForApproval() {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot submit plan for approval in status: " + status
            );
        }
        this.status = PlanStatus.PENDING_APPROVAL;
        this.version++;
    }

    public void approve(CommonKernel.UserId approvedBy) {
        if (status != PlanStatus.DRAFT && status != PlanStatus.PENDING_APPROVAL) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_APPROVABLE",
                    "Cannot approve plan in status: " + status
            );
        }
        if (actions.isEmpty()) {
            throw new CommonKernel.ValidationException(
                    "PLAN_EMPTY",
                    "Cannot approve plan with no actions"
            );
        }
        this.status = PlanStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
        this.version++;
    }

    public void reject(CommonKernel.UserId rejectedBy, String reason) {
        if (status != PlanStatus.DRAFT && status != PlanStatus.PENDING_APPROVAL && status != PlanStatus.PENDING_REVIEW) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_REJECTABLE",
                    "Cannot reject plan in status: " + status
            );
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new CommonKernel.ValidationException(
                    "REJECT_REASON_REQUIRED",
                    "Rejection reason is required"
            );
        }
        this.status = PlanStatus.REJECTED;
        // Store rejection reason in description or a dedicated field
        this.description = (this.description != null ? this.description + " | " : "") + "REJECTED: " + reason;
        this.version++;
    }

    public void revise(CommonKernel.UserId revisedBy, String reason) {
        if (status != PlanStatus.PENDING_APPROVAL && status != PlanStatus.PENDING_REVIEW && status != PlanStatus.REJECTED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_REVISABLE",
                    "Cannot revise plan in status: " + status
            );
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new CommonKernel.ValidationException(
                    "REVISE_REASON_REQUIRED",
                    "Revision reason is required"
            );
        }
        this.status = PlanStatus.PENDING_REVIEW;
        this.description = (this.description != null ? this.description + " | " : "") + "REVISED: " + reason;
        this.version++;
    }

    public void startExecution() {
        if (status != PlanStatus.APPROVED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_APPROVED",
                    "Cannot start execution in status: " + status
            );
        }
        this.status = PlanStatus.EXECUTING;
        this.startedAt = Instant.now();
        this.version++;
    }

    // Alias for test compatibility
    public void start() {
        startExecution();
    }

    public void complete() {
        if (status != PlanStatus.EXECUTING) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_EXECUTING",
                    "Cannot complete plan in status: " + status
            );
        }
        boolean allCompleted = actions.stream().allMatch(a -> a.getStatus() == ResponseAction.ActionStatus.COMPLETED || a.getStatus() == ResponseAction.ActionStatus.VERIFIED || a.getStatus() == ResponseAction.ActionStatus.SKIPPED);
        if (!allCompleted) {
            throw new CommonKernel.ValidationException(
                    "PLAN_INCOMPLETE_ACTIONS",
                    "Cannot complete plan with incomplete actions"
            );
        }
        this.status = PlanStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.version++;
    }

    public List<ResponseAction> getActionsByPriority() {
        return actions.stream()
                .sorted(Comparator.comparingInt(a -> a.getPriority().ordinal()))
                .collect(Collectors.toList());
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
        PENDING_APPROVAL,
        PENDING_REVIEW,
        APPROVED,
        REJECTED,
        EXECUTING,
        COMPLETED,
        FAILED,
        CANCELLED,
        ARCHIVED
    }

    public static final class Builder {
        private CommonKernel.ResponsePlanId id;
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
            this.incidentId = Objects.requireNonNull(incidentId);
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