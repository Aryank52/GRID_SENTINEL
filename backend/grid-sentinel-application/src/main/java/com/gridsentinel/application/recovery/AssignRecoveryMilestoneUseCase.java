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
 * Use case for assigning recovery milestones to operators.
 */
@Service
@Transactional
public class AssignRecoveryMilestoneUseCase {

    private final RecoveryStateRepository recoveryStateRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public AssignRecoveryMilestoneUseCase(
            RecoveryStateRepository recoveryStateRepository,
            UserRepository userRepository,
            EventPublisher eventPublisher
    ) {
        this.recoveryStateRepository = recoveryStateRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Assign a recovery milestone to an operator.
     *
     * @param recoveryStateId the recovery state containing the milestone
     * @param milestoneId the milestone to assign
     * @param operatorId the operator to assign
     * @param assignedBy the user making the assignment
     * @return the updated milestone
     */
    public com.gridsentinel.recovery.RecoveryMilestone assign(CommonKernel.RecoveryStateId recoveryStateId,
                                                              String milestoneId,
                                                              CommonKernel.UserId operatorId,
                                                              CommonKernel.UserId assignedBy) {
        // Load and validate recovery state
        RecoveryState recoveryState = recoveryStateRepository.findById(recoveryStateId)
                .orElseThrow(() -> new IllegalArgumentException("Recovery state not found: " + recoveryStateId));

        // Verify recovery is active
        if (recoveryState.getPhase() == com.gridsentinel.recovery.RecoveryPhase.NOT_STARTED ||
            recoveryState.getPhase() == com.gridsentinel.recovery.RecoveryPhase.COMPLETED ||
            recoveryState.getPhase() == com.gridsentinel.recovery.RecoveryPhase.CANCELLED) {
            throw new IllegalStateException("Cannot assign milestones in recovery state: " + recoveryState.getPhase());
        }

        // Find the milestone
        RecoveryMilestone milestone = recoveryState.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found: " + milestoneId));

        // Verify milestone is assignable
        if (milestone.getStatus() != RecoveryStatus.NOT_STARTED) {
            throw new IllegalStateException("Cannot assign milestone in status: " + milestone.getStatus());
        }

        // Verify operator exists
        // userRepository.findById(operatorId).orElseThrow(() -> new IllegalArgumentException("Operator not found: " + operatorId));

        // Assign the milestone
        com.gridsentinel.recovery.RecoveryMilestone milestoneRef = recoveryState.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Milestone lost after load"));
        milestoneRef.start(operatorId);

        // Save and publish event
        RecoveryState saved = recoveryStateRepository.save(recoveryState);
        eventPublisher.publishMilestoneAssigned(saved, milestoneId, operatorId);

        // Return updated milestone
        return saved.getMilestones().stream()
                .filter(m -> m.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Milestone lost after save"));
    }

    // Port interfaces
    public interface RecoveryStateRepository {
        Optional<RecoveryState> findById(com.gridsentinel.common.CommonKernel.RecoveryStateId id);
        RecoveryState save(RecoveryState recoveryState);
    }

    public interface UserRepository {
        Optional<com.gridsentinel.auth.User> findById(CommonKernel.UserId id);
    }

    public interface EventPublisher {
        void publishMilestoneAssigned(RecoveryState recoveryState, String milestoneId, CommonKernel.UserId operatorId);
    }
}