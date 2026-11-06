package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when recovery progress is updated (milestone completed, phase changed, etc.).
 */
public final class RecoveryProgressUpdatedEvent extends GridEvent<RecoveryProgressUpdatedEvent.Payload> {

    private final String recoveryStateId;
    private final String incidentId;
    private final String previousPhase;
    private final String newPhase;
    private final double previousProgress;
    private final double newProgress;
    private final String milestoneId;
    private final String milestoneName;
    private final boolean milestoneCompleted;

    public RecoveryProgressUpdatedEvent(CorrelationId correlationId,
                                        String recoveryStateId,
                                        String incidentId,
                                        String previousPhase,
                                        String newPhase,
                                        double previousProgress,
                                        double newProgress,
                                        String milestoneId,
                                        String milestoneName,
                                        boolean milestoneCompleted,
                                        String source) {
        super("RecoveryProgressUpdated", correlationId, "RecoveryState", recoveryStateId, 1,
                determineSeverity(newPhase),
                "recovery-management");
        this.recoveryStateId = Objects.requireNonNull(recoveryStateId);
        this.incidentId = incidentId;
        this.previousPhase = previousPhase;
        this.newPhase = newPhase;
        this.previousProgress = previousProgress;
        this.newProgress = newProgress;
        this.milestoneId = milestoneId;
        this.milestoneName = milestoneName;
        this.milestoneCompleted = milestoneCompleted;
    }

    private static EventSeverity determineSeverity(String phase) {
        return switch (phase) {
            case "COMPLETED" -> EventSeverity.HIGH;
            case "ON_HOLD", "CANCELLED" -> EventSeverity.LOW;
            default -> EventSeverity.MEDIUM;
        };
    }

    @Override
    public String eventType() {
        return "RecoveryProgressUpdated";
    }

    @Override
    public Payload payload() {
        return new Payload(
                recoveryStateId,
                incidentId,
                previousPhase,
                newPhase,
                previousProgress,
                newProgress,
                milestoneId,
                milestoneName,
                milestoneCompleted
        );
    }

    public record Payload(
            String recoveryStateId,
            String incidentId,
            String previousPhase,
            String newPhase,
            double previousProgress,
            double newProgress,
            String milestoneId,
            String milestoneName,
            boolean milestoneCompleted
    ) {}
}