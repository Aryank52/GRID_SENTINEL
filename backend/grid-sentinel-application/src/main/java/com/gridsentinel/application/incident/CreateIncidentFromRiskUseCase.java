package com.gridsentinel.application.incident;

import com.gridsentinel.application.risk.AssessThreatRiskUseCase;
import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.strategy.StrategyInterfaces;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Use case for creating or escalating incidents from risk assessments.
 * <p>
 * This use case connects the risk assessment workflow to incident management.
 * It evaluates risk scores against the incident trigger policy and creates
 * or escalates incidents as appropriate.
 * </p>
 */
@Service
@Transactional
public class CreateIncidentFromRiskUseCase {

    private final IncidentRepository incidentRepository;
    private final AssetRepository assetRepository;
    private final ThreatEventRepository threatEventRepository;
    private final IncidentTriggerPolicy incidentTriggerPolicy;
    private final EventPublisher eventPublisher;
    private final RealTimeEventService realTimeEventService;

    public CreateIncidentFromRiskUseCase(
            IncidentRepository incidentRepository,
            AssetRepository assetRepository,
            ThreatEventRepository threatEventRepository,
            IncidentTriggerPolicy incidentTriggerPolicy,
            EventPublisher eventPublisher,
            RealTimeEventService realTimeEventService
    ) {
        this.incidentRepository = incidentRepository;
        this.assetRepository = assetRepository;
        this.threatEventRepository = threatEventRepository;
        this.incidentTriggerPolicy = incidentTriggerPolicy;
        this.eventPublisher = eventPublisher;
        this.realTimeEventService = realTimeEventService;
    }

    /**
     * Evaluate a threat event for incident creation.
     * This is the main entry point from the threat detection pipeline.
     *
     * @param threatEvent the detected threat event
     * @return the created/updated incident, or empty if no incident was created
     */
    public Optional<Incident> evaluateThreatForIncident(ThreatEvent threatEvent) {
        // Get the risk score for the affected asset
        CommonKernel.AssetId assetId = threatEvent.getAffectedAssetId();
        if (assetId == null) {
            return Optional.empty();
        }

        // Get latest risk score for this asset
        Optional<RiskScore> latestRiskScore = threatEventRepository.findLatestRiskScoreByAssetId(assetId);
        if (latestRiskScore.isEmpty()) {
            return Optional.empty();
        }

        RiskScore riskScore = latestRiskScore.get();

        // Build threat context
        IncidentTriggerPolicy.ThreatContext threatContext = new IncidentTriggerPolicy.ThreatContext(
                threatEvent.getId(),
                threatEvent.getSeverity(),
                threatEvent.getConfidence(),
                assetId
        );

        // Evaluate incident trigger policy
        IncidentTriggerPolicy.IncidentEvaluation evaluation = 
                incidentTriggerPolicy.evaluateForCreation(riskScore, threatContext);

        // Execute the decision
        return switch (evaluation.decision()) {
            case CREATE_INCIDENT -> createIncident(threatEvent, riskScore, evaluation);
            case ESCALATE_INCIDENT -> escalateExistingIncident(threatEvent, riskScore, evaluation);
            case UPDATE_INCIDENT -> updateExistingIncident(threatEvent, riskScore, evaluation);
            case NO_ACTION -> {
                eventPublisher.publishPolicyEvaluation(threatEvent.getId(), riskScore.getId(), evaluation);
                yield Optional.empty();
            }
        };
    }

    /**
     * Evaluate a risk score for incident creation (without threat context).
     *
     * @param riskScore the risk score to evaluate
     * @return the created/updated incident, or empty if no incident was created
     */
    public Optional<Incident> evaluateRiskForIncident(RiskScore riskScore) {
        // Check if there's an existing active incident for this asset
        Optional<Incident> existingIncident = incidentRepository.findActiveByAssetId(riskScore.getAssetId());

        if (existingIncident.isPresent()) {
            // Evaluate for escalation
            IncidentTriggerPolicy.IncidentEvaluation evaluation = 
                    incidentTriggerPolicy.evaluateForEscalation(existingIncident.get(), riskScore);

            return switch (evaluation.decision()) {
                case ESCALATE_INCIDENT -> escalateExistingIncident(existingIncident.get(), riskScore, evaluation);
                case UPDATE_INCIDENT -> updateExistingIncident(existingIncident.get(), riskScore, evaluation);
                case NO_ACTION -> {
                    eventPublisher.publishPolicyEvaluation(null, riskScore.getId(), evaluation);
                    yield Optional.of(existingIncident.get());
                }
                default -> Optional.of(existingIncident.get());
            };
        }

        // No existing incident - evaluate for creation
        IncidentTriggerPolicy.IncidentEvaluation evaluation = 
                incidentTriggerPolicy.evaluateForCreation(riskScore, null);

        if (evaluation.decision() == IncidentTriggerPolicy.Decision.CREATE_INCIDENT) {
            return createIncidentFromRisk(riskScore, evaluation);
        }

        eventPublisher.publishPolicyEvaluation(null, riskScore.getId(), evaluation);
        return Optional.empty();
    }

    private Optional<Incident> createIncident(ThreatEvent threatEvent, RiskScore riskScore, 
                                              IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        // Check for duplicate incident for this threat
        Optional<Incident> existing = incidentRepository.findByOriginatingThreatId(threatEvent.getId());
        if (existing.isPresent()) {
            // Update existing incident if needed
            return updateExistingIncident(existing.get(), riskScore, evaluation);
        }

        Asset asset = assetRepository.findById(riskScore.getAssetId()).orElse(null);
        
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .severity(evaluation.suggestedSeverity())
                .originatingThreatId(threatEvent.getId())
                .primaryAssetId(riskScore.getAssetId())
                .primaryGridNodeId(threatEvent.getNearbyGridNodeId())
                .location(threatEvent.getLocation())
                .description(buildIncidentDescription(threatEvent, riskScore, evaluation))
                .detectedAt(threatEvent.getDetectedAt())
                .correlationId(threatEvent.getCorrelationId())
                .build();

        Incident saved = incidentRepository.save(incident);

        // Publish real-time events
        realTimeEventService.publishIncidentCreated(saved);
        eventPublisher.publishIncidentCreated(saved, riskScore, evaluation);

        return Optional.of(saved);
    }

    private Optional<Incident> createIncidentFromRisk(RiskScore riskScore, 
                                                      IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        Asset asset = assetRepository.findById(riskScore.getAssetId()).orElse(null);
        
        Incident incident = Incident.builder()
                .id(CommonKernel.IncidentId.generate())
                .severity(evaluation.suggestedSeverity())
                .primaryAssetId(riskScore.getAssetId())
                .primaryGridNodeId(asset != null ? asset.getGridNodeId() : null)
                .location(asset != null ? asset.getGeometry() : null)
                .description(buildIncidentDescriptionFromRisk(riskScore, evaluation))
                .detectedAt(Instant.now())
                .correlationId(riskScore.getCorrelationId())
                .build();

        Incident saved = incidentRepository.save(incident);

        // Publish real-time events
        realTimeEventService.publishIncidentCreated(saved);
        eventPublisher.publishIncidentCreated(saved, riskScore, evaluation);

        return Optional.of(saved);
    }

    private Optional<Incident> escalateExistingIncident(ThreatEvent threatEvent, RiskScore riskScore,
                                                        IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        Optional<Incident> existing = incidentRepository.findByOriginatingThreatId(threatEvent.getId());
        if (existing.isEmpty()) {
            existing = incidentRepository.findActiveByAssetId(riskScore.getAssetId());
        }
        if (existing.isPresent()) {
            return escalateExistingIncident(existing.get(), riskScore, evaluation);
        }
        return Optional.empty();
    }

    private Optional<Incident> escalateExistingIncident(Incident incident, RiskScore riskScore,
                                                        IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        IncidentState previousState = incident.getState();
        ThreatSeverity previousSeverity = incident.getSeverity();

        // Update severity if suggested
        if (evaluation.suggestedSeverity() != null && 
            (incident.getSeverity() == null || 
             evaluation.suggestedSeverity().ordinal() > incident.getSeverity().ordinal())) {
            // Create new incident with updated severity (immutable pattern)
            // For now, we'll update through repository
            // In reality, this would be a domain method on Incident
            incident = updateIncidentSeverity(incident, evaluation.suggestedSeverity());
        }

        // Update state if suggested
        if (evaluation.suggestedState() != null && 
            evaluation.suggestedState().ordinal() > incident.getState().ordinal()) {
            incident = updateIncidentState(incident, evaluation.suggestedState());
        }

        Incident saved = incidentRepository.save(incident);

        // Publish state change event
        if (previousState != saved.getState()) {
            realTimeEventService.publishIncidentStateChanged(saved, previousState, "system", evaluation.rationale());
        }

        eventPublisher.publishIncidentEscalated(saved, riskScore, evaluation);
        return Optional.of(saved);
    }

    private Optional<Incident> updateExistingIncident(ThreatEvent threatEvent, RiskScore riskScore,
                                                      IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        Optional<Incident> existing = incidentRepository.findByOriginatingThreatId(threatEvent.getId());
        if (existing.isEmpty()) {
            existing = incidentRepository.findActiveByAssetId(riskScore.getAssetId());
        }
        if (existing.isPresent()) {
            return updateExistingIncident(existing.get(), riskScore, evaluation);
        }
        return Optional.empty();
    }

    private Optional<Incident> updateExistingIncident(Incident incident, RiskScore riskScore,
                                                      IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        // Just update description/notes with new risk info
        // In a full implementation, might update other fields
        IncidentState previousState = incident.getState();
        
        // If state should progress, progress it
        if (evaluation.suggestedState() != null && 
            evaluation.suggestedState().ordinal() > incident.getState().ordinal()) {
            incident = updateIncidentState(incident, evaluation.suggestedState());
        }

        Incident saved = incidentRepository.save(incident);

        if (previousState != saved.getState()) {
            realTimeEventService.publishIncidentStateChanged(saved, previousState, "system", evaluation.rationale());
        }

        eventPublisher.publishIncidentUpdated(saved, riskScore, evaluation);
        return Optional.of(saved);
    }

    private Incident updateIncidentSeverity(Incident incident, ThreatSeverity newSeverity) {
        // Since Incident is immutable, we need to create a new one or use a mutable approach
        // For simplicity, we'll assume the repository handles updates
        // In reality, this would be a domain method on Incident
        return incident; // Placeholder - actual implementation would update severity
    }

    private Incident updateIncidentState(Incident incident, IncidentState newState) {
        // Placeholder for state update
        return incident;
    }

    private Incident updateIncident(Incident incident, RiskScore riskScore, 
                                    IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        return incidentRepository.save(incident);
    }

    private String buildIncidentDescription(ThreatEvent threatEvent, RiskScore riskScore,
                                            IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        return String.format(
            "Incident created from threat %s (type: %s, severity: %s). " +
            "Risk assessment: score=%.2f, band=%s. Policy rationale: %s",
            threatEvent.getId(),
            threatEvent.getType(),
            threatEvent.getSeverity(),
            riskScore.getScore(),
            riskScore.getBand(),
            evaluation.rationale()
        );
    }

    private String buildIncidentDescriptionFromRisk(RiskScore riskScore,
                                                    IncidentTriggerPolicy.IncidentEvaluation evaluation) {
        return String.format(
            "Incident created from risk assessment. " +
            "Risk score: %.2f, band=%s. Policy rationale: %s",
            riskScore.getScore(),
            riskScore.getBand(),
            evaluation.rationale()
        );
    }

    // Port interfaces
    public interface IncidentRepository {
        Incident save(Incident incident);
        Optional<Incident> findById(CommonKernel.IncidentId id);
        Optional<Incident> findByOriginatingThreatId(CommonKernel.ThreatEventId threatId);
        Optional<Incident> findActiveByAssetId(CommonKernel.AssetId assetId);
        Optional<Incident> findByIncidentNumber(String incidentNumber);
    }

    public interface AssetRepository {
        Optional<Asset> findById(CommonKernel.AssetId id);
    }

    public interface ThreatEventRepository {
        Optional<ThreatEvent> findById(CommonKernel.ThreatEventId id);
        Optional<RiskScore> findLatestRiskScoreByAssetId(CommonKernel.AssetId assetId);
    }

    public interface EventPublisher {
        void publishIncidentCreated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation);
        void publishIncidentEscalated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation);
        void publishIncidentUpdated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation);
        void publishPolicyEvaluation(CommonKernel.ThreatEventId threatEventId, CommonKernel.RiskScoreId riskScoreId, IncidentTriggerPolicy.IncidentEvaluation evaluation);
    }

    // Real-time event service from infrastructure
    public interface RealTimeEventService {
        void publishIncidentCreated(Incident incident);
        void publishIncidentStateChanged(Incident incident, com.gridsentinel.common.IncidentState previousState, String actorId, String reason);
    }
}