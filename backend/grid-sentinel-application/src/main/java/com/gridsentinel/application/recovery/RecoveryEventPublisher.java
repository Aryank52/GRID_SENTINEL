package com.gridsentinel.application.recovery;

import com.gridsentinel.incident.Incident;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.RecoveryState;
import com.gridsentinel.common.CommonKernel;

/**
 * Event publisher for recovery-related events.
 */
public interface RecoveryEventPublisher {

    void publishRecoveryInitialized(RecoveryState recoveryState, Incident incident);

    void publishMilestoneAssigned(RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);

    void publishMilestoneStarted(RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);

    void publishMilestoneCompleted(RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);

    void publishRecoveryCompleted(RecoveryState recoveryState, CommonKernel.IncidentId incidentId);

    void publishIncidentStateChanged(Incident incident, com.gridsentinel.common.IncidentState previousState, String actorId, String reason);
}