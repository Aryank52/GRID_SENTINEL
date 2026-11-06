package com.gridsentinel.protection;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.risk.ProtectionAction;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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

/**
 * Prevention plan with prioritized protection measures.
 * Includes cost/risk-reduction ranking for budget optimization.
 */
@Entity
@Table(name = "protection_plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtectionPlan {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "protection_plan_id", nullable = false))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ProtectionPlanId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Setter(AccessLevel.PRIVATE)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private PlanStatus status = PlanStatus.DRAFT;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private List<ProtectionAction> measures = new ArrayList<>();

    @Setter(AccessLevel.PRIVATE)
    private Double totalEstimatedCost = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double totalExpectedRiskReduction = 0.0;

    @Setter(AccessLevel.PRIVATE)
    private Double budgetLimit;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId createdBy;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId approvedBy;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant approvedAt;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private ProtectionPlan(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.description = builder.description;
        this.measures = builder.measures != null ? new ArrayList<>(builder.measures) : new ArrayList<>();
        this.budgetLimit = builder.budgetLimit;
        this.createdBy = builder.createdBy;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.version = 0;
        recalculateTotals();
    }

    public void addMeasure(ProtectionAction measure) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot add measures to plan in status: " + status
            );
        }
        measures.add(Objects.requireNonNull(measure));
        recalculateTotals();
        this.version++;
    }

    public void removeMeasure(String actionId) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot remove measures from plan in status: " + status
            );
        }
        measures.removeIf(m -> m.getActionId().equals(actionId));
        recalculateTotals();
        this.version++;
    }

    public void approve(CommonKernel.UserId approverId) {
        if (status != PlanStatus.DRAFT) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "PLAN_NOT_DRAFT",
                    "Cannot approve plan in status: " + status
            );
        }
        if (measures.isEmpty()) {
            throw new CommonKernel.ValidationException(
                    "PLAN_EMPTY",
                    "Cannot approve empty protection plan"
            );
        }
        if (budgetLimit != null && totalEstimatedCost > budgetLimit) {
            throw new CommonKernel.ValidationException(
                    "PLAN_OVER_BUDGET",
                    "Plan cost %.2f exceeds budget %.2f".formatted(totalEstimatedCost, budgetLimit)
            );
        }
        this.status = PlanStatus.APPROVED;
        this.approvedBy = Objects.requireNonNull(approverId);
        this.approvedAt = Instant.now();
        this.version++;
    }

    private void recalculateTotals() {
        this.totalEstimatedCost = measures.stream()
                .mapToDouble(ProtectionAction::getEstimatedCost)
                .sum();
        this.totalExpectedRiskReduction = measures.stream()
                .mapToDouble(ProtectionAction::getExpectedRiskReduction)
                .sum();
    }

    public List<ProtectionAction> getMeasures() {
        return Collections.unmodifiableList(measures);
    }

    public List<ProtectionAction> getMeasuresByCostEffectiveness() {
        return measures.stream()
                .sorted((a, b) -> Double.compare(b.adjustedCostEffectiveness(), a.adjustedCostEffectiveness()))
                .toList();
    }

    public boolean isWithinBudget() {
        return budgetLimit == null || totalEstimatedCost <= budgetLimit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtectionPlan)) return false;
        ProtectionPlan that = (ProtectionPlan) o;
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
        private CommonKernel.ProtectionPlanId id = CommonKernel.ProtectionPlanId.generate();
        private String name;
        private String description;
        private List<ProtectionAction> measures = new ArrayList<>();
        private Double budgetLimit;
        private CommonKernel.UserId createdBy;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.ProtectionPlanId id) {
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

        public Builder addMeasure(ProtectionAction measure) {
            this.measures.add(Objects.requireNonNull(measure));
            return this;
        }

        public Builder budgetLimit(double budgetLimit) {
            this.budgetLimit = budgetLimit;
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

        public ProtectionPlan build() {
            return new ProtectionPlan(this);
        }
    }
}