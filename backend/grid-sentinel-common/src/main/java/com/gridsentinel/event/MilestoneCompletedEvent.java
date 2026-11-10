package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a recovery milestone is completed.
 */
public final class MilestoneCompletedEvent extends GridEvent<MilestoneCompletedEvent.Payload> {

    private final String recoveryStateId;
    private final String incidentId;
    private final String milestoneId;
    private final String milestoneName;
    private final String operatorId;
    private final boolean completed;

    public MilestoneCompletedEvent(CorrelationId correlationId,
                                   String recoveryStateId,
                                   String incidentId,
                                   String milestoneId,
                                   String milestoneName,
                                   String operatorId,
                                   boolean completed,
                                   String source) {
        super("MilestoneCompleted", correlationId, "RecoveryMilestone", milestoneId, 1,
                EventSeverity.HIGH,
                "recovery-management");
        this.recoveryStateId = Objects.requireNonNull(recoveryStateId);
        this.incidentId = incidentId;
        this.milestoneId = Objects.requireNonNull(milestoneId);
        this.milestoneName = Objects.requireNonNull(milestoneName);
        this.operatorId = Objects.requireNonNull(operatorId);
        this.completed = completed;
    }

    @Override
    public String eventType() {
        return "MilestoneCompleted";
    }

    @Override
    public Payload payload() {
        return new Payload(
                recoveryStateId,
                incidentId,
                milestoneId,
                milestoneName,
                operatorId,
                completed
        );
    }

    public record Payload(
            String recoveryStateId,
            String incidentId,
            String milestoneId,
            String milestoneName,
            String operatorId,
            boolean completed
    ) {}
}