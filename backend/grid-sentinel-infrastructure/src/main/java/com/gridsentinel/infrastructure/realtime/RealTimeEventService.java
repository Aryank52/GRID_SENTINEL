package com.gridsentinel.infrastructure.realtime;

import org.springframework.stereotype.Service;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.CommonKernel.IncidentState;
import com.gridsentinel.common.CommonKernel.RiskBand;
import com.gridsentinel.common.CommonKernel.ThreatSeverity;
import com.gridsentinel.common.CommonKernel.HealthIndex;
import com.gridsentinel.common.CommonKernel.Criticality;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.simulation.Simulation;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.recovery.RecoveryState;
import com.gridsentinel.asset.Asset;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Application service for publishing real-time grid events.
 * <p>
 * This service is called from application use cases (REST controllers, command handlers)
 * to publish events after successful operations.
 * </p>
 */
@Service
public class RealTimeEventService {

    private final StompRealTimeEventPublisher realTimePublisher;

    public RealTimeEventService(StompRealTimeEventPublisher realTimePublisher) {
        this.realTimePublisher = realTimePublisher;
    }

    // =========================================================================
    // Threat Events
    // =========================================================================

    /**
     * Publishes a ThreatDetectedEvent after a threat is created.
     */
    public void publishThreatDetected(ThreatEvent threat) {
        if (threat == null) return;
        
        CorrelationId correlationId = threat.getCorrelationId() != null 
                ? threat.getCorrelationId() 
                : CorrelationId.generate();

        Set<String> evidenceIds = threat.getSupportingEvidence() != null 
                ? threat.getSupportingEvidence().stream()
                    .map(e -> e.getId().toString())
                    .collect(Collectors.toSet())
                : Set.of();

        com.gridsentinel.event.ThreatDetectedEvent event = new com.gridsentinel.event.ThreatDetectedEvent(
                correlationId,
                threat.getId().toString(),
                threat.getType().name(),
                threat.getSeverity().name(),
                threat.getConfidence(),
                threat.getDetectedAt(),
                threat.getAffectedAssetId() != null ? threat.getAffectedAssetId().toString() : null,
                threat.getNearbyGridNodeId() != null ? threat.getNearbyGridNodeId().toString() : null,
                threat.getDescription(),
                evidenceIds,
                threat.getDetectionStrategy(),
                threat.getStrategyVersion(),
                "detection-pipeline"
        );

        realTimePublisher.publish(event, correlationId);
    }

    /**
     * Publishes a ThreatEscalatedEvent after a threat's severity is escalated.
     */
    public void publishThreatEscalated(ThreatEvent threat, 
                                        ThreatSeverity previousSeverity,
                                        String reason) {
        if (threat == null || previousSeverity == null) return;
        
        CorrelationId correlationId = threat.getCorrelationId() != null 
                ? threat.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.ThreatEscalatedEvent event = new com.gridsentinel.event.ThreatEscalatedEvent(
                correlationId,
                threat.getId().toString(),
                previousSeverity.name(),
                threat.getSeverity().name(),
                reason,
                "threat-pipeline"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Incident Events
    // =========================================================================

    /**
     * Publishes an IncidentCreatedEvent after an incident is created.
     */
    public void publishIncidentCreated(Incident incident) {
        if (incident == null) return;
        
        CorrelationId correlationId = incident.getCorrelationId() != null 
                ? incident.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.IncidentCreatedEvent event = new com.gridsentinel.event.IncidentCreatedEvent(
                correlationId,
                incident.getId().toString(),
                incident.getIncidentNumber(),
                incident.getState().name(),
                incident.getSeverity() != null ? incident.getSeverity().name() : null,
                incident.getOriginatingThreatId() != null ? incident.getOriginatingThreatId().toString() : null,
                incident.getPrimaryAssetId() != null ? incident.getPrimaryAssetId().toString() : null,
                incident.getPrimaryGridNodeId() != null ? incident.getPrimaryGridNodeId().toString() : null,
                incident.getDescription(),
                incident.getAssignedOperatorId() != null ? incident.getAssignedOperatorId().toString() : null,
                incident.getIncidentCommanderId() != null ? incident.getIncidentCommanderId().toString() : null,
                "incident-management"
        );

        realTimePublisher.publish(event, correlationId);
    }

    /**
     * Publishes an IncidentStateChangedEvent after an incident's state changes.
     */
    public void publishIncidentStateChanged(Incident incident,
                                             IncidentState previousState,
                                             String actorId,
                                             String reason) {
        if (incident == null || previousState == null) return;
        
        CorrelationId correlationId = incident.getCorrelationId() != null 
                ? incident.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.IncidentStateChangedEvent event = new com.gridsentinel.event.IncidentStateChangedEvent(
                correlationId,
                incident.getId().toString(),
                incident.getIncidentNumber(),
                previousState.name(),
                incident.getState().name(),
                actorId,
                reason,
                "incident-management"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Risk Score Events
    // =========================================================================

    /**
     * Publishes a RiskScoreUpdatedEvent after a risk score is computed or updated.
     */
    public void publishRiskScoreUpdated(RiskScore riskScore,
                                         double previousScore,
                                         RiskBand previousBand) {
        if (riskScore == null) return;
        
        CorrelationId correlationId = riskScore.getCorrelationId() != null 
                ? riskScore.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.RiskScoreUpdatedEvent event = new com.gridsentinel.event.RiskScoreUpdatedEvent(
                correlationId,
                riskScore.getId().toString(),
                riskScore.getAssetId().toString(),
                previousScore,
                riskScore.getScore(),
                previousBand != null ? previousBand.name() : null,
                riskScore.getBand() != null ? riskScore.getBand().name() : null,
                riskScore.getModelName(),
                riskScore.getModelVersion(),
                riskScore.getStrategyName(),
                "risk-engine"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Simulation Events
    // =========================================================================

    /**
     * Publishes a SimulationCompletedEvent after a simulation completes.
     */
    public void publishSimulationCompleted(Simulation simulation) {
        if (simulation == null) return;
        
        CorrelationId correlationId = simulation.getCorrelationId() != null 
                ? simulation.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.SimulationCompletedEvent event = new com.gridsentinel.event.SimulationCompletedEvent(
                correlationId,
                simulation.getId().toString(),
                simulation.getName(),
                simulation.getScenarioId() != null ? simulation.getScenarioId().toString() : null,
                simulation.getStatus().name(),
                simulation.getStrategyName(),
                simulation.getStrategyVersion(),
                simulation.getRandomSeed(),
                simulation.getErrorMessage(),
                "simulation-engine"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Response Plan Events
    // =========================================================================

    /**
     * Publishes a ResponsePlanUpdatedEvent after a response plan is updated.
     */
    public void publishResponsePlanUpdated(ResponsePlan plan,
                                            ResponsePlan.PlanStatus previousStatus,
                                            String actorId,
                                            String action) {
        if (plan == null || action == null) return;
        
        CorrelationId correlationId = plan.getCorrelationId() != null 
                ? plan.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.ResponsePlanUpdatedEvent event = new com.gridsentinel.event.ResponsePlanUpdatedEvent(
                correlationId,
                plan.getId().toString(),
                plan.getIncidentId() != null ? plan.getIncidentId().toString() : null,
                plan.getName(),
                previousStatus != null ? previousStatus.name() : null,
                plan.getStatus().name(),
                actorId,
                action,
                "response-planning"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Recovery Events
    // =========================================================================

    /**
     * Publishes a RecoveryProgressUpdatedEvent after recovery progress is updated.
     */
    public void publishRecoveryProgressUpdated(RecoveryState recovery,
                                                RecoveryState.RecoveryPhase previousPhase,
                                                double previousProgress,
                                                String milestoneId,
                                                String milestoneName,
                                                boolean milestoneCompleted) {
        if (recovery == null) return;
        
        CorrelationId correlationId = recovery.getCorrelationId() != null 
                ? recovery.getCorrelationId() 
                : CorrelationId.generate();

        com.gridsentinel.event.RecoveryProgressUpdatedEvent event = new com.gridsentinel.event.RecoveryProgressUpdatedEvent(
                correlationId,
                recovery.getId().toString(),
                recovery.getIncidentId() != null ? recovery.getIncidentId().toString() : null,
                previousPhase != null ? previousPhase.name() : null,
                recovery.getPhase() != null ? recovery.getPhase().name() : null,
                previousProgress,
                recovery.getProgress(),
                milestoneId,
                milestoneName,
                milestoneCompleted,
                "recovery-management"
        );

        realTimePublisher.publish(event, correlationId);
    }

    // =========================================================================
    // Asset Events
    // =========================================================================

    /**
     * Publishes a GridAssetStatusChangedEvent after an asset's status changes.
     * Note: Asset entity doesn't have correlationId field, so we generate one.
     */
    public void publishAssetStatusChanged(Asset asset,
                                           HealthIndex previousHealthIndex,
                                           Criticality previousCriticality,
                                           String changeReason) {
        if (asset == null) return;
        
        CorrelationId correlationId = CorrelationId.generate();

        // Determine health status string from HealthIndex
        String previousHealthStatus = previousHealthIndex != null ? healthIndexToStatus(previousHealthIndex) : null;
        String newHealthStatus = asset.getHealthIndex() != null ? healthIndexToStatus(asset.getHealthIndex()) : null;
        
        // Determine risk band from health index
        String previousRiskBand = previousHealthIndex != null ? healthIndexToRiskBand(previousHealthIndex) : null;
        String newRiskBand = asset.getHealthIndex() != null ? healthIndexToRiskBand(asset.getHealthIndex()) : null;

        com.gridsentinel.event.GridAssetStatusChangedEvent event = new com.gridsentinel.event.GridAssetStatusChangedEvent(
                correlationId,
                asset.getId().toString(),
                asset.getName(),
                asset.getType().name(),
                previousHealthStatus,
                newHealthStatus,
                previousCriticality != null ? previousCriticality.name() : null,
                asset.getCriticality() != null ? asset.getCriticality().name() : null,
                previousHealthIndex != null ? previousHealthIndex.value() : 1.0,
                asset.getHealthIndex() != null ? asset.getHealthIndex().value() : 1.0,
                previousRiskBand,
                newRiskBand,
                changeReason,
                "asset-management"
        );

        realTimePublisher.publish(event, correlationId);
    }

    private String healthIndexToStatus(HealthIndex healthIndex) {
        if (healthIndex == null) return "UNKNOWN";
        double value = healthIndex.value();
        if (value >= 0.8) return "HEALTHY";
        if (value >= 0.4) return "DEGRADED";
        return "CRITICAL";
    }

    private String healthIndexToRiskBand(HealthIndex healthIndex) {
        if (healthIndex == null) return "UNKNOWN";
        double value = healthIndex.value();
        if (value >= 0.8) return "NEGLIGIBLE";
        if (value >= 0.6) return "LOW";
        if (value >= 0.4) return "MEDIUM";
        if (value >= 0.2) return "HIGH";
        return "CRITICAL";
    }
}