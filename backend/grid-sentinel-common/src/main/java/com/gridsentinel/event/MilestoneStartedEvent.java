package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a recovery milestone is started.
 */
public final class MilestoneStartedEvent extends GridEvent<MilestoneStartedEvent.Payload> {

    private final String recoveryStateId;
    private final String incidentId;
    private final String milestoneId;
    private final String milestoneName;
    private final String operatorId;

    public MilestoneStartedEvent(CorrelationId correlationId,
                                  String recoveryStateId,
                                  String incidentId,
                                  String milestoneId,
                                  String milestoneName,
                                  String operatorId,
                                  String source) {
        super("MilestoneStarted", correlationId, "RecoveryMilestone", milestoneId, 1,
                EventSeverity.MEDIUM,
                "recovery-management");
        this.recoveryStateId = Objects.requireNonNull(recoveryStateId);
        this.incidentId = incidentId;
        this.milestoneId = Objects.requireNonNull(milestoneId);
        this.milestoneName = Objects.requireNonNull(milestoneName);
        this.operatorId = Objects.requireNonNull(operatorId);
    }

    @Override
    public String eventType() {
        return "MilestoneStarted";
    }

    @Override
    public Payload payload() {
        return new Payload(
                recoveryStateId,
                incidentId,
                milestoneId,
                milestoneName,
                operatorId
        );
    }

    public record Payload(
            String recoveryStateId,
            String incidentId,
            String milestoneId,
            String milestoneName,
            String operatorId
    ) {}
}