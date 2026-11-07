package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
import java.util.UUID;

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
    @Column(name = "response_plan_id", nullable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID id;

    @NotNull
    @Column(name = "incident_id", nullable = false, columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID incidentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private PlanStatus status = PlanStatus.DRAFT;

    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    // @ElementCollection - Temporarily disabled due to Hibernate enum issue
    // @Setter(AccessLevel.PRIVATE)
    // private List<ResponseAction> actions = new ArrayList<>();
    
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private List<ResponseAction> actions = new ArrayList<>();

    @Column(name = "created_by", columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID createdBy;

    @Column(name = "approved_by", columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID approvedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant approvedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant completedAt;

    @Column(name = "correlation_id", columnDefinition = "uuid")
    @Setter(AccessLevel.PRIVATE)
    private UUID correlationId;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private ResponsePlan(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.incidentId = Objects.requireNonNull(builder.incidentId, "Incident ID cannot be null");
        this.name = builder.name;
        this.description = builder.description;
        this.actions = builder.actions != null ? new ArrayList<>(builder.actions) : new ArrayList<>();
        this.createdBy = builder.createdBy;
        this.correlationId = builder.correlationId != null ? builder.correlationId : UUID.randomUUID();
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

    public void removeAction(String actionId) {
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

    public void approve(UUID approvedBy) {
        if (status != PlanStatus.PENDING_APPROVAL) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_PENDING_APPROVAL",
                    "Cannot approve plan in status: " + status
            );
        }
        this.status = PlanStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
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

    public void complete() {
        if (status != PlanStatus.EXECUTING) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_EXECUTING",
                    "Cannot complete plan in status: " + status
            );
        }
        this.status = PlanStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.version++;
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

    public static final class Builder {
        private UUID id;
        private UUID incidentId;
        private String name;
        private String description;
        private List<ResponseAction> actions = new ArrayList<>();
        private UUID createdBy;
        private UUID correlationId;

        private Builder() {}

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder incidentId(UUID incidentId) {
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

        public Builder createdBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ResponsePlan build() {
            return new ResponsePlan(this);
        }
    }
}