package com.gridsentinel.application.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.RecoveryStatus;
import com.gridsentinel.recovery.RecoveryState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Use case for starting a recovery milestone.
 */
@Service
@Transactional
public class StartRecoveryMilestoneUseCase {

    private final RecoveryStateRepository recoveryStateRepository;
    private final EventPublisher eventPublisher;

    public StartRecoveryMilestoneUseCase(
            RecoveryStateRepository recoveryStateRepository,
            EventPublisher eventPublisher
    ) {
        this.recoveryStateRepository = recoveryStateRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Start a recovery milestone.
     *
     * @param recoveryStateId the recovery state containing the milestone
     * @param milestoneId the milestone to start
     * @param operatorId the operator starting the milestone
     * @return the updated milestone
     */
    public com.gridsentinel.recovery.RecoveryMilestone start(CommonKernel.RecoveryStateId recoveryStateId,
                                                              String milestoneId,
                                                              CommonKernel.UserId operatorId) {
        // Load and validate recovery state
        RecoveryState recoveryState = recoveryStateRepository.findById(recoveryStateId)
                .orElseThrow(() -> new IllegalArgumentException("Recovery state not found: " + recoveryStateId));

        // Find the milestone
        RecoveryMilestone milestone = recoveryState.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));

        // Check dependencies
        if (!dependenciesSatisfied(recoveryState, milestoneId)) {
            throw new IllegalStateException("Dependencies not satisfied for milestone: " + milestoneId);
        }

        // Verify milestone can be started
        if (milestone.getStatus() != com.gridsentinel.recovery.RecoveryStatus.NOT_STARTED) {
            throw new IllegalStateException("Cannot start milestone in status: " + milestone.getStatus());
        }

        // Start the milestone
        RecoveryState recoveryStateRef = recoveryStateRepository.findById(recoveryStateId).orElseThrow();
        RecoveryMilestone milestoneRef = recoveryStateRef.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));
        milestoneRef.start(operatorId);

        // If recovery was NOT_STARTED, start it
        RecoveryState recoveryStateToUpdate = recoveryStateRepository.findById(recoveryStateId).orElseThrow();
        if (recoveryStateToUpdate.getPhase() == com.gridsentinel.recovery.RecoveryPhase.NOT_STARTED) {
            recoveryStateToUpdate.start(recoveryStateRef.getRecoveryManagerId());
        }

        // Save and publish event
        RecoveryState saved = recoveryStateRepository.save(recoveryStateToUpdate);
        eventPublisher.publishMilestoneStarted(saved, milestoneId, operatorId);

        // Return updated milestone
        return saved.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Milestone lost after save"));
    }

    private boolean dependenciesSatisfied(RecoveryState recoveryState, String milestoneId) {
        // Find the milestone's sequence order
        RecoveryMilestone targetMilestone = recoveryState.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElse(null);
        
        if (targetMilestone == null || targetMilestone.getSequenceOrder() == null) {
            return true;
        }

        // Check if all previous milestones (by sequence order) are completed
        return recoveryState.getMilestones().stream()
                .filter(m -> m.getSequenceOrder() != null && m.getSequenceOrder() < targetMilestone.getSequenceOrder())
                .allMatch(m -> m.isCompleted());
    }

    // Port interfaces
    public interface RecoveryStateRepository {
        Optional<com.gridsentinel.recovery.RecoveryState> findById(com.gridsentinel.common.CommonKernel.RecoveryStateId id);
        com.gridsentinel.recovery.RecoveryState save(com.gridsentinel.recovery.RecoveryState recoveryState);
    }

    public interface EventPublisher {
        void publishMilestoneStarted(com.gridsentinel.recovery.RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);
    }
}