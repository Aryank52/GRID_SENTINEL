package com.gridsentinel.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
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
 * Explicit recovery state tracking for an incident.
 * Progress through defined milestones with verification.
 */
@Entity
@Table(name = "recovery_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecoveryState {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "recovery_state_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.RecoveryStateId id;

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
    private RecoveryPhase phase = RecoveryPhase.NOT_STARTED;

    @Setter(AccessLevel.PRIVATE)
    private Double overallProgress = 0.0; // [0.0, 1.0]

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recovery_milestones", joinColumns = @JoinColumn(name = "recovery_state_id"))
    @OrderColumn(name = "milestone_order")
    @Setter(AccessLevel.PRIVATE)
    private List<RecoveryMilestone> milestones = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "recovery_manager_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId recoveryManagerId;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant estimatedCompletionAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant actualCompletionAt;

    @Setter(AccessLevel.PRIVATE)
    private String lessonsLearned;

    @Setter(AccessLevel.PRIVATE)
    private String afterActionReport;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "correlation_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private RecoveryState(Builder builder) {
        this.id = builder.id;
        this.incidentId = Objects.requireNonNull(builder.incidentId, "Incident ID cannot be null");
        this.milestones = builder.milestones != null ? new ArrayList<>(builder.milestones) : new ArrayList<>();
        this.recoveryManagerId = builder.recoveryManagerId;
        this.estimatedCompletionAt = builder.estimatedCompletionAt;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    public void addMilestone(RecoveryMilestone milestone) {
        milestones.add(Objects.requireNonNull(milestone));
        recalculateProgress();
        this.version++;
    }

    public void start(CommonKernel.UserId managerId) {
        if (phase != RecoveryPhase.NOT_STARTED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "RECOVERY_ALREADY_STARTED",
                    "Recovery already in phase: " + phase
            );
        }
        this.phase = RecoveryPhase.IN_PROGRESS;
        this.recoveryManagerId = Objects.requireNonNull(managerId);
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void completeMilestone(String milestoneId, CommonKernel.UserId verifiedBy, String verificationNotes) {
        RecoveryMilestone milestone = milestones.stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new CommonKernel.NotFoundException(
                        "MILESTONE_NOT_FOUND",
                        "Milestone not found: " + milestoneId
                ));

        milestone.complete(verifiedBy, verificationNotes);
        recalculateProgress();

        if (overallProgress >= 1.0) {
            this.phase = RecoveryPhase.COMPLETED;
            this.actualCompletionAt = Instant.now();
        }
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void setLessonsLearned(String lessonsLearned) {
        this.lessonsLearned = lessonsLearned;
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void setAfterActionReport(String afterActionReport) {
        this.afterActionReport = afterActionReport;
        this.updatedAt = Instant.now();
        this.version++;
    }

    private void recalculateProgress() {
        if (milestones.isEmpty()) {
            this.overallProgress = 0.0;
            return;
        }
        long completed = milestones.stream().filter(RecoveryMilestone::isCompleted).count();
        this.overallProgress = (double) completed / milestones.size();
    }

    public List<RecoveryMilestone> getMilestones() {
        return Collections.unmodifiableList(milestones);
    }

    public List<RecoveryMilestone> getPendingMilestones() {
        return milestones.stream()
                .filter(m -> !m.isCompleted())
                .toList();
    }

    public List<RecoveryMilestone> getBlockedMilestones() {
        return milestones.stream()
                .filter(m -> m.getStatus() == RecoveryStatus.BLOCKED)
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecoveryState)) return false;
        RecoveryState that = (RecoveryState) o;
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
        private CommonKernel.RecoveryStateId id = CommonKernel.RecoveryStateId.generate();
        private CommonKernel.IncidentId incidentId;
        private List<RecoveryMilestone> milestones = new ArrayList<>();
        private CommonKernel.UserId recoveryManagerId;
        private Instant estimatedCompletionAt;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.RecoveryStateId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder incidentId(CommonKernel.IncidentId incidentId) {
            this.incidentId = incidentId;
            return this;
        }

        public Builder addMilestone(RecoveryMilestone milestone) {
            this.milestones.add(Objects.requireNonNull(milestone));
            return this;
        }

        public Builder recoveryManagerId(CommonKernel.UserId recoveryManagerId) {
            this.recoveryManagerId = recoveryManagerId;
            return this;
        }

        public Builder estimatedCompletionAt(Instant estimatedCompletionAt) {
            this.estimatedCompletionAt = estimatedCompletionAt;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public RecoveryState build() {
            return new RecoveryState(this);
        }
    }
}