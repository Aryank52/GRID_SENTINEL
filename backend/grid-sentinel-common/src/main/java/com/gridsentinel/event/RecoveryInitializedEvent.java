package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when recovery is initialized.
 */
public final class RecoveryInitializedEvent extends GridEvent<RecoveryInitializedEvent.Payload> {

    private final String recoveryStateId;
    private final String incidentId;
    private final String phase;
    private final double progress;
    private final String recoveryManagerId;

    public RecoveryInitializedEvent(CorrelationId correlationId,
                                    String recoveryStateId,
                                    String incidentId,
                                    String phase,
                                    double progress,
                                    String recoveryManagerId,
                                    String source) {
        super("RecoveryInitialized", correlationId, "RecoveryState", recoveryStateId, 1,
                EventSeverity.MEDIUM,
                "recovery-management");
        this.recoveryStateId = Objects.requireNonNull(recoveryStateId);
        this.incidentId = incidentId;
        this.phase = phase;
        this.progress = progress;
        this.recoveryManagerId = recoveryManagerId;
    }

    @Override
    public String eventType() {
        return "RecoveryInitialized";
    }

    @Override
    public Payload payload() {
        return new Payload(
                recoveryStateId,
                incidentId,
                phase,
                progress,
                recoveryManagerId
        );
    }

    public record Payload(
            String recoveryStateId,
            String incidentId,
            String phase,
            double progress,
            String recoveryManagerId
    ) {}
}