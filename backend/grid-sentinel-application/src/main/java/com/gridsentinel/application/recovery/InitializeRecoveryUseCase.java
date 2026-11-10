package com.gridsentinel.application.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryState;
import com.gridsentinel.recovery.MilestoneType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for initializing recovery from a contained incident.
 * <p>
 * Creates a RecoveryState entity with appropriate milestones based on
 * the incident type, affected assets, and completed mitigation actions.
 * </p>
 */
@Service
@Transactional
public class InitializeRecoveryUseCase {

    private final IncidentRepository incidentRepository;
    private final RecoveryStateRepository recoveryStateRepository;
    private final RecoveryEligibilityPolicy eligibilityPolicy;
    private final MilestoneFactory milestoneFactory;
    private final EventPublisher eventPublisher;

    public InitializeRecoveryUseCase(
            IncidentRepository incidentRepository,
            RecoveryStateRepository recoveryStateRepository,
            RecoveryEligibilityPolicy eligibilityPolicy,
            MilestoneFactory milestoneFactory,
            EventPublisher eventPublisher
    ) {
        this.incidentRepository = incidentRepository;
        this.recoveryStateRepository = recoveryStateRepository;
        this.eligibilityPolicy = eligibilityPolicy;
        this.milestoneFactory = milestoneFactory;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Initialize recovery for a contained incident.
     *
     * @param incidentId the incident to initialize recovery for
     * @param recoveryManagerId the user assigned as recovery manager
     * @param estimatedCompletionAt optional estimated completion time
     * @return the created recovery state
     */
    public RecoveryState initialize(CommonKernel.IncidentId incidentId,
                                    CommonKernel.UserId recoveryManagerId,
                                    Instant estimatedCompletionAt) {
        // Load incident
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Check for existing active recovery
        Optional<RecoveryState> existing = recoveryStateRepository.findActiveByIncidentId(incidentId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Recovery already exists for incident: " + incidentId);
        }

        // Check eligibility
        RecoveryEligibilityPolicy.EligibilityResult eligibility = eligibilityPolicy.evaluate(
                incidentRepository.findById(incidentId).orElseThrow()
        );
        if (!eligibility.eligible()) {
            throw new IllegalStateException("Incident not eligible for recovery: " + eligibility.rationale());
        }

        // Build milestones based on incident type and affected assets
        List<RecoveryMilestone> milestones = milestoneFactory.createMilestones(incident);

        // Create recovery state
        RecoveryState.Builder builder = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(incidentId)
                .recoveryManagerId(recoveryManagerId)
                .estimatedCompletionAt(estimatedCompletionAt)
                .correlationId(incident.getCorrelationId());

        for (RecoveryMilestone milestone : milestones) {
            builder.addMilestone(milestone);
        }

        RecoveryState recoveryState = builder.build();

        // Save
        RecoveryState saved = recoveryStateRepository.save(recoveryState);

        // Update incident state to RECOVERING
        incident.startRecovery(CommonKernel.UserId.of(java.util.UUID.fromString(recoveryManagerId.toString())), "Recovery initialized");
        incidentRepository.save(incident);

        // Publish event
        eventPublisher.publishRecoveryInitialized(saved, incident);

        return saved;
    }

    // Port interfaces
    public interface IncidentRepository {
        Optional<Incident> findById(CommonKernel.IncidentId id);
        Incident save(Incident incident);
    }

    public interface RecoveryStateRepository {
        RecoveryState save(RecoveryState recoveryState);
        Optional<RecoveryState> findById(CommonKernel.RecoveryStateId id);
        Optional<RecoveryState> findActiveByIncidentId(CommonKernel.IncidentId incidentId);
        List<RecoveryState> findByPhase(com.gridsentinel.recovery.RecoveryPhase phase);
    }

    public interface MilestoneFactory {
        List<RecoveryMilestone> createMilestones(Incident incident);
    }

    public interface EventPublisher {
        void publishRecoveryInitialized(com.gridsentinel.recovery.RecoveryState recoveryState,
                                         com.gridsentinel.incident.Incident incident);
    }
}