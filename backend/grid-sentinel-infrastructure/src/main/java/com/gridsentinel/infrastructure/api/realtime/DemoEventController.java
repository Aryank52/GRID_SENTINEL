package com.gridsentinel.infrastructure.api.realtime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.infrastructure.realtime.RealTimeEventService;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DEMO/DEVELOPMENT ONLY - Test event trigger endpoints.
 * <p>
 * These endpoints are for development and testing purposes only.
 * They allow triggering simulated real-time events to verify the WebSocket/STOMP
 * delivery and frontend integration.
 * </p>
 * <p>
 * <b>WARNING:</b> These are NOT production operational controls.
 * They simulate events for testing the real-time event pipeline.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/dev/realtime")
public class DemoEventController {

    private final RealTimeEventService realTimeEventService;

    public DemoEventController(RealTimeEventService realTimeEventService) {
        this.realTimeEventService = realTimeEventService;
    }

    /**
     * Trigger a simulated ThreatDetectedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/threat-detected")
    public ResponseEntity<Map<String, Object>> triggerThreatDetected(@RequestBody(required = false) Map<String, Object> payload) {
        String threatEventId = "THR-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String threatType = (String) payload.getOrDefault("threatType", "FIRE");
        String severity = (String) payload.getOrDefault("severity", "HIGH");
        double confidence = ((Number) payload.getOrDefault("confidence", 0.9)).doubleValue();
        String description = (String) payload.getOrDefault("description", "DEMO: Simulated wildfire threat near transmission corridor");
        String affectedAssetId = (String) payload.getOrDefault("affectedAssetId", "ASSET-DEMO-001");
        String nearbyGridNodeId = (String) payload.getOrDefault("nearbyGridNodeId", "NODE-DEMO-001");
        String detectionStrategy = (String) payload.getOrDefault("detectionStrategy", "thermal-detection-v1");
        String strategyVersion = (String) payload.getOrDefault("strategyVersion", "1.0.0");

        realTimeEventService.publishThreatDetected(
            threatEventId,
            threatType,
            severity,
            confidence,
            description,
            affectedAssetId,
            nearbyGridNodeId,
            detectionStrategy,
            strategyVersion
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "ThreatDetected",
            "eventId", threatEventId,
            "message", "Simulated threat detected event published to /topic/threats"
        ));
    }

    /**
     * Trigger a simulated IncidentCreatedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/incident-created")
    public ResponseEntity<Map<String, Object>> triggerIncidentCreated(@RequestBody(required = false) Map<String, Object> payload) {
        String incidentId = "INC-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String incidentNumber = "INC-2026-DEMO-" + UUID.randomUUID().toString().substring(0, 6);
        String state = (String) payload.getOrDefault("state", "DETECTED");
        String severity = (String) payload.getOrDefault("severity", "HIGH");
        String description = (String) payload.getOrDefault("description", "DEMO: Simulated incident from threat escalation");
        String originatingThreatId = (String) payload.getOrDefault("originatingThreatId", "THR-DEMO-001");
        String primaryAssetId = (String) payload.getOrDefault("primaryAssetId", "ASSET-DEMO-001");
        String primaryGridNodeId = (String) payload.getOrDefault("primaryGridNodeId", "NODE-DEMO-001");
        String assignedOperatorId = (String) payload.getOrDefault("assignedOperatorId", "OPERATOR-DEMO-001");
        String incidentCommanderId = (String) payload.getOrDefault("incidentCommanderId", "COMMANDER-DEMO-001");

        realTimeEventService.publishIncidentCreated(
            incidentId,
            incidentNumber,
            state,
            severity,
            originatingThreatId,
            primaryAssetId,
            primaryGridNodeId,
            description,
            assignedOperatorId,
            incidentCommanderId
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "IncidentCreated",
            "eventId", incidentId,
            "message", "Simulated incident created event published to /topic/incidents"
        ));
    }

    /**
     * Trigger a simulated IncidentStateChangedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/incident-state-changed")
    public ResponseEntity<Map<String, Object>> triggerIncidentStateChanged(@RequestBody(required = false) Map<String, Object> payload) {
        String incidentId = (String) payload.getOrDefault("incidentId", "INC-DEMO-001");
        String incidentNumber = (String) payload.getOrDefault("incidentNumber", "INC-2026-DEMO-001");
        String previousState = (String) payload.getOrDefault("previousState", "DETECTED");
        String newState = (String) payload.getOrDefault("newState", "CONFIRMED");
        String actorId = (String) payload.getOrDefault("actorId", "OPERATOR-DEMO-001");
        String reason = (String) payload.getOrDefault("reason", "DEMO: State transition for testing");

        realTimeEventService.publishIncidentStateChanged(
            incidentId,
            incidentNumber,
            previousState,
            newState,
            actorId,
            reason
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "IncidentStateChanged",
            "incidentId", incidentId,
            "previousState", previousState,
            "newState", newState,
            "message", "Simulated incident state change published to /topic/incidents"
        ));
    }

    /**
     * Trigger a simulated RiskScoreUpdatedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/risk-score-updated")
    public ResponseEntity<Map<String, Object>> triggerRiskScoreUpdated(@RequestBody(required = false) Map<String, Object> payload) {
        String assetId = (String) payload.getOrDefault("assetId", "ASSET-DEMO-001");
        double previousScore = ((Number) payload.getOrDefault("previousScore", 0.35)).doubleValue();
        double newScore = ((Number) payload.getOrDefault("newScore", 0.82)).doubleValue();
        String previousBand = (String) payload.getOrDefault("previousBand", "LOW");
        String newBand = (String) payload.getOrDefault("newBand", "HIGH");
        String modelName = (String) payload.getOrDefault("modelName", "wildfire-risk-model");
        String modelVersion = (String) payload.getOrDefault("modelVersion", "2.1.0");
        String strategyName = (String) payload.getOrDefault("strategyName", "bayesian-inference");

        realTimeEventService.publishRiskScoreUpdated(
            assetId,
            previousScore,
            newScore,
            previousBand,
            newBand,
            modelName,
            modelVersion,
            strategyName
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "RiskScoreUpdated",
            "assetId", assetId,
            "previousScore", previousScore,
            "newScore", newScore,
            "message", "Simulated risk score update published to /topic/risk"
        ));
    }

    /**
     * Trigger a simulated GridAssetStatusChangedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/asset-status-changed")
    public ResponseEntity<Map<String, Object>> triggerAssetStatusChanged(@RequestBody(required = false) Map<String, Object> payload) {
        String assetId = (String) payload.getOrDefault("assetId", "ASSET-DEMO-001");
        String assetName = (String) payload.getOrDefault("assetName", "Transmission Line 500kV #1 (DEMO)");
        String assetType = (String) payload.getOrDefault("assetType", "TRANSMISSION_LINE");
        String previousHealthStatus = (String) payload.getOrDefault("previousHealthStatus", "HEALTHY");
        String newHealthStatus = (String) payload.getOrDefault("newHealthStatus", "DEGRADED");
        String previousCriticality = (String) payload.getOrDefault("previousCriticality", "MEDIUM");
        String newCriticality = (String) payload.getOrDefault("newCriticality", "HIGH");
        double previousHealthIndex = ((Number) payload.getOrDefault("previousHealthIndex", 0.9)).doubleValue();
        double newHealthIndex = ((Number) payload.getOrDefault("newHealthIndex", 0.45)).doubleValue();
        String previousRiskBand = (String) payload.getOrDefault("previousRiskBand", "LOW");
        String newRiskBand = (String) payload.getOrDefault("newRiskBand", "HIGH");
        String changeReason = (String) payload.getOrDefault("changeReason", "DEMO: Thermal anomaly detected by sensor");

        realTimeEventService.publishAssetStatusChanged(
            assetId,
            assetName,
            assetType,
            previousHealthStatus,
            newHealthStatus,
            previousCriticality,
            newCriticality,
            previousHealthIndex,
            newHealthIndex,
            previousRiskBand,
            newRiskBand,
            changeReason
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "GridAssetStatusChanged",
            "assetId", assetId,
            "previousHealthStatus", previousHealthStatus,
            "newHealthStatus", newHealthStatus,
            "message", "Simulated asset status change published to /topic/grid"
        ));
    }

    /**
     * Trigger a simulated ThreatEscalatedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/threat-escalated")
    public ResponseEntity<Map<String, Object>> triggerThreatEscalated(@RequestBody(required = false) Map<String, Object> payload) {
        String threatEventId = "THR-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String previousSeverity = (String) payload.getOrDefault("previousSeverity", "MEDIUM");
        String newSeverity = (String) payload.getOrDefault("newSeverity", "HIGH");
        String escalationReason = (String) payload.getOrDefault("escalationReason", "DEMO: Threat escalation due to worsening conditions");

        realTimeEventService.publishThreatEscalated(
            threatEventId,
            previousSeverity,
            newSeverity,
            escalationReason
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "ThreatEscalated",
            "threatEventId", threatEventId,
            "previousSeverity", previousSeverity,
            "newSeverity", newSeverity,
            "message", "Simulated threat escalation event published to /topic/threats"
        ));
    }

    /**
     * Trigger a simulated SimulationCompletedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/simulation-completed")
    public ResponseEntity<Map<String, Object>> triggerSimulationCompleted(@RequestBody(required = false) Map<String, Object> payload) {
        String simulationId = "SIM-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String name = (String) payload.getOrDefault("name", "Cascading Failure Simulation");
        String scenarioId = (String) payload.getOrDefault("scenarioId", "SCENARIO-DEMO-001");
        String status = (String) payload.getOrDefault("status", "COMPLETED");
        String strategyName = (String) payload.getOrDefault("strategyName", "cascading-failure-v1");
        String strategyVersion = (String) payload.getOrDefault("strategyVersion", "1.0.0");
        Long randomSeed = ((Number) payload.getOrDefault("randomSeed", 42L)).longValue();
        String errorMessage = (String) payload.getOrDefault("errorMessage", null);

        realTimeEventService.publishSimulationCompleted(
            simulationId,
            name,
            scenarioId,
            status,
            strategyName,
            strategyVersion,
            randomSeed,
            errorMessage
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "SimulationCompleted",
            "simulationId", simulationId,
            "name", name,
            "simulationStatus", status,
            "message", "Simulated simulation completion event published to /topic/simulation"
        ));
    }

    /**
     * Trigger a simulated ResponsePlanUpdatedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/response-plan-updated")
    public ResponseEntity<Map<String, Object>> triggerResponsePlanUpdated(@RequestBody(required = false) Map<String, Object> payload) {
        String responsePlanId = "RSP-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String incidentId = (String) payload.getOrDefault("incidentId", "INC-DEMO-001");
        String name = (String) payload.getOrDefault("name", "Containment Plan Alpha");
        String previousStatus = (String) payload.getOrDefault("previousStatus", "DRAFT");
        String newStatus = (String) payload.getOrDefault("newStatus", "APPROVED");
        String actorId = (String) payload.getOrDefault("actorId", "INCIDENT_COMMANDER-001");
        String action = (String) payload.getOrDefault("action", "APPROVED");

        realTimeEventService.publishResponsePlanUpdated(
            responsePlanId,
            incidentId,
            name,
            previousStatus,
            newStatus,
            actorId,
            action
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "ResponsePlanUpdated",
            "responsePlanId", responsePlanId,
            "incidentId", incidentId,
            "previousStatus", previousStatus,
            "newStatus", newStatus,
            "action", action,
            "message", "Simulated response plan update published to /topic/incidents"
        ));
    }

    /**
     * Trigger a simulated RecoveryProgressUpdatedEvent.
     * 
     * @param payload optional custom payload fields
     * @return confirmation
     */
    @PostMapping("/recovery-progress-updated")
    public ResponseEntity<Map<String, Object>> triggerRecoveryProgressUpdated(@RequestBody(required = false) Map<String, Object> payload) {
        String recoveryStateId = "REC-DEMO-" + UUID.randomUUID().toString().substring(0, 8);
        String incidentId = (String) payload.getOrDefault("incidentId", "INC-DEMO-001");
        String previousPhase = (String) payload.getOrDefault("previousPhase", "STABILIZATION");
        String newPhase = (String) payload.getOrDefault("newPhase", "REPAIR");
        double previousProgress = ((Number) payload.getOrDefault("previousProgress", 0.3)).doubleValue();
        double newProgress = ((Number) payload.getOrDefault("newProgress", 0.6)).doubleValue();
        String milestoneId = (String) payload.getOrDefault("milestoneId", null);
        String milestoneName = (String) payload.getOrDefault("milestoneName", null);
        Boolean milestoneCompleted = (Boolean) payload.getOrDefault("milestoneCompleted", false);

        realTimeEventService.publishRecoveryProgressUpdated(
            recoveryStateId,
            incidentId,
            previousPhase,
            newPhase,
            previousProgress,
            newProgress,
            milestoneId,
            milestoneName,
            milestoneCompleted
        );

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_EVENT_TRIGGERED",
            "eventType", "RecoveryProgressUpdated",
            "recoveryStateId", recoveryStateId,
            "incidentId", incidentId,
            "previousPhase", previousPhase,
            "newPhase", newPhase,
            "previousProgress", previousProgress,
            "newProgress", newProgress,
            "message", "Simulated recovery progress update published to /topic/recovery"
        ));
    }

    /**
     * Trigger a full demo sequence: Threat -> Incident -> State Change -> Risk Update -> Asset Change.
     * 
     * @return confirmation with all triggered events
     */
    @PostMapping("/full-demo-sequence")
    public ResponseEntity<Map<String, Object>> triggerFullDemoSequence() {
        // 1. Threat Detected
        triggerThreatDetected(Map.of());
        
        // 2. Incident Created
        triggerIncidentCreated(Map.of());
        
        // 3. Incident State Changed (after short delay)
        triggerIncidentStateChanged(Map.of(
            "incidentId", "INC-DEMO-001",
            "incidentNumber", "INC-2026-DEMO-001",
            "previousState", "DETECTED",
            "newState", "CONFIRMED"
        ));
        
        // 4. Risk Score Updated
        triggerRiskScoreUpdated(Map.of());
        
        // 5. Asset Status Changed
        triggerAssetStatusChanged(Map.of());
        
        // 6. Threat Escalated
        triggerThreatEscalated(Map.of());
        
        // 7. Simulation Completed
        triggerSimulationCompleted(Map.of());
        
        // 8. Response Plan Updated
        triggerResponsePlanUpdated(Map.of());
        
        // 9. Recovery Progress Updated
        triggerRecoveryProgressUpdated(Map.of());

        return ResponseEntity.ok(Map.of(
            "status", "DEMO_SEQUENCE_TRIGGERED",
            "events", new String[]{
                "ThreatDetected",
                "IncidentCreated",
                "IncidentStateChanged",
                "RiskScoreUpdated",
                "GridAssetStatusChanged",
                "ThreatEscalated",
                "SimulationCompleted",
                "ResponsePlanUpdated",
                "RecoveryProgressUpdated"
            },
            "message", "Full demo sequence triggered - check Command Center and Live Grid Map for real-time updates",
            "topics", new String[]{
                "/topic/threats",
                "/topic/incidents",
                "/topic/risk",
                "/topic/grid",
                "/topic/simulation",
                "/topic/recovery"
            }
        ));
    }
}