package com.gridsentinel.application.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Use case for completing recovery and transitioning incident to resolved/closed.
 */
@Service
@Transactional
public class CompleteRecoveryUseCase {

    private final RecoveryStateRepository recoveryStateRepository;
    private final IncidentRepository incidentRepository;
    private final EventPublisher eventPublisher;

    public CompleteRecoveryUseCase(
            RecoveryStateRepository recoveryStateRepository,
            IncidentRepository incidentRepository,
            EventPublisher eventPublisher
    ) {
        this.recoveryStateRepository = recoveryStateRepository;
        this.incidentRepository = incidentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Complete the recovery process and transition incident to resolved.
     *
     * @param recoveryStateId the recovery state to complete
     * @param operatorId the operator completing the recovery
     * @param notes completion notes
     * @return the completed recovery state
     */
    public com.gridsentinel.recovery.RecoveryState complete(CommonKernel.RecoveryStateId recoveryStateId,
                                                             CommonKernel.UserId operatorId,
                                                             String notes) {
        // Load and validate recovery state
        RecoveryState recoveryState = recoveryStateRepository.findById(recoveryStateId)
                .orElseThrow(() -> new IllegalArgumentException("Recovery state not found: " + recoveryStateId));

        // Verify recovery is complete (all milestones completed)
        if (recoveryState.getOverallProgress() < 1.0) {
            throw new IllegalStateException("Cannot complete recovery: not all milestones are completed");
        }

        if (recoveryState.getPhase() != com.gridsentinel.recovery.RecoveryPhase.COMPLETED) {
            throw new IllegalStateException("Recovery not in COMPLETED phase: " + recoveryState.getPhase());
        }

        // Complete the recovery
        RecoveryState recoveryStateRef = recoveryStateRepository.findById(recoveryStateId).orElseThrow();
        
        // Set actual completion time
        recoveryStateRef.setAfterActionReport(notes);
        recoveryStateRef.setLessonsLearned("Recovery completed by " + operatorId + " at " + Instant.now());
        
        // Mark as completed (already in COMPLETED phase, but update timestamps)
        RecoveryState saved = recoveryStateRepository.save(recoveryStateRef);

        // Update incident state to RESOLVED
        Incident incident = incidentRepository.findById(recoveryState.getIncidentId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + recoveryState.getIncidentId()));
        
        IncidentState previousIncidentState = incident.getState();
        incident.resolve(CommonKernel.UserId.of(java.util.UUID.fromString(operatorId.toString())), "Recovery completed");
        Incident savedIncident = incidentRepository.save(incident);

        // Publish events
        eventPublisher.publishRecoveryCompleted(saved, recoveryState.getIncidentId());
        if (previousIncidentState != savedIncident.getState()) {
            eventPublisher.publishIncidentStateChanged(savedIncident, previousIncidentState, operatorId.toString(), "Recovery completed");
        }

        return saved;
    }

    // Port interfaces
    public interface RecoveryStateRepository {
        Optional<RecoveryState> findById(CommonKernel.RecoveryStateId id);
        RecoveryState save(RecoveryState recoveryState);
    }

    public interface IncidentRepository {
        Optional<Incident> findById(CommonKernel.IncidentId id);
        Incident save(Incident incident);
    }

    public interface EventPublisher {
        void publishRecoveryCompleted(RecoveryState recoveryState, CommonKernel.IncidentId incidentId);
        void publishIncidentStateChanged(Incident incident, IncidentState previousState, String actorId, String reason);
    }
}