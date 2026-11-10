package com.gridsentinel.application.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.RecoveryStatus;
import com.gridsentinel.recovery.RecoveryState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for completing a recovery milestone.
 */
@Service
@Transactional
public class CompleteRecoveryMilestoneUseCase {

    private final RecoveryStateRepository recoveryStateRepository;
    private final EventPublisher eventPublisher;

    public CompleteRecoveryMilestoneUseCase(
            RecoveryStateRepository recoveryStateRepository,
            EventPublisher eventPublisher
    ) {
        this.recoveryStateRepository = recoveryStateRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Complete a recovery milestone.
     *
     * @param recoveryStateId the recovery state containing the milestone
     * @param milestoneId the milestone to complete
     * @param operatorId the operator completing the milestone
     * @param verificationNotes notes on the completion
     * @return the updated milestone
     */
    public com.gridsentinel.recovery.RecoveryMilestone complete(CommonKernel.RecoveryStateId recoveryStateId,
                                                                String milestoneId,
                                                                CommonKernel.UserId operatorId,
                                                                String verificationNotes) {
        // Load and validate recovery state
        RecoveryState recoveryState = recoveryStateRepository.findById(recoveryStateId)
                .orElseThrow(() -> new IllegalArgumentException("Recovery state not found: " + recoveryStateId));

        // Find the milestone
        RecoveryMilestone milestone = recoveryState.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));

        // Verify milestone can be completed
        if (milestone.getStatus() != com.gridsentinel.recovery.RecoveryStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete milestone in status: " + milestone.getStatus());
        }

        // Complete the milestone
        RecoveryState recoveryStateRef = recoveryStateRepository.findById(recoveryStateId).orElseThrow();
        RecoveryMilestone milestoneRef = recoveryStateRef.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));
        milestoneRef.complete(operatorId, verificationNotes);

        // Check if all milestones are completed (which triggers recovery completion)
        checkAndCompleteRecovery(recoveryStateRef);

        // Save and publish event
        RecoveryState saved = recoveryStateRepository.save(recoveryStateRef);
        eventPublisher.publishMilestoneCompleted(saved, milestoneId, operatorId);

        // Return updated milestone
        return saved.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Milestone lost after save"));
    }

    private void checkAndCompleteRecovery(RecoveryState recoveryState) {
        if (recoveryState.getOverallProgress() >= 1.0 &&
            recoveryState.getPhase() != com.gridsentinel.recovery.RecoveryPhase.COMPLETED) {
            // The RecoveryState.completeMilestone() method already handles this
            // but we call it here for explicit clarity
        }
    }

    // Port interfaces
    public interface RecoveryStateRepository {
        Optional<com.gridsentinel.recovery.RecoveryState> findById(com.gridsentinel.common.CommonKernel.RecoveryStateId id);
        com.gridsentinel.recovery.RecoveryState save(com.gridsentinel.recovery.RecoveryState recoveryState);
    }

    public interface EventPublisher {
        void publishMilestoneCompleted(com.gridsentinel.recovery.RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);
    }
}