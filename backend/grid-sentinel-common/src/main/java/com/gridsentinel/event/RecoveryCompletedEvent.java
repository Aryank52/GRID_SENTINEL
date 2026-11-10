package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when recovery is completed.
 */
public final class RecoveryCompletedEvent extends GridEvent<RecoveryCompletedEvent.Payload> {

    private final String recoveryStateId;
    private final String incidentId;
    private final String incidentIdFromRecovery;
    private final String actorId;
    private final String reason;

    public RecoveryCompletedEvent(CorrelationId correlationId,
                                  String recoveryStateId,
                                  String incidentIdFromRecovery,
                                  String incidentId,
                                  String actorId,
                                  String reason,
                                  String source) {
        super("RecoveryCompleted", correlationId, "RecoveryState", recoveryStateId, 1,
                EventSeverity.HIGH,
                "recovery-management");
        this.recoveryStateId = Objects.requireNonNull(recoveryStateId);
        this.incidentIdFromRecovery = incidentId;
        this.incidentId = Objects.requireNonNull(incidentId);
        this.actorId = Objects.requireNonNull(actorId);
        this.reason = reason;
    }

    @Override
    public String eventType() {
        return "RecoveryCompleted";
    }

    @Override
    public Payload payload() {
        return new Payload(
                recoveryStateId,
                incidentIdFromRecovery,
                incidentId,
                actorId,
                reason
        );
    }

    public record Payload(
            String recoveryStateId,
            String incidentIdFromRecovery,
            String incidentId,
            String actorId,
            String reason
    ) {}
}