package com.gridsentinel.application.response;

import com.gridsentinel.application.impact.AssessIncidentImpactUseCase;
import com.gridsentinel.application.simulation.CreateSimulationFromImpactUseCase;
import com.gridsentinel.application.simulation.ExecuteSimulationUseCase;
import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ResponsePriority;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.ProtectionAction;
import com.gridsentinel.risk.ActionCategory;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.Simulation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use case for generating response options from a completed simulation.
 * <p>
 * Converts simulation results into actionable response plans with prioritized
 * actions, cost estimates, and risk reduction projections.
 * </p>
 */
@Service
@Transactional
public class GenerateResponseOptionsUseCase {

    private final SimulationRepository simulationRepository;
    private final IncidentRepository incidentRepository;
    private final ResponsePlanRepository responsePlanRepository;
    private final AssetRepository assetRepository;
    private final ResponsePlanningStrategy responsePlanningStrategy;
    private final EventPublisher eventPublisher;

    public GenerateResponseOptionsUseCase(
            SimulationRepository simulationRepository,
            IncidentRepository incidentRepository,
            ResponsePlanRepository responsePlanRepository,
            AssetRepository assetRepository,
            ResponsePlanningStrategy responsePlanningStrategy,
            EventPublisher eventPublisher
    ) {
        this.simulationRepository = simulationRepository;
        this.incidentRepository = incidentRepository;
        this.responsePlanRepository = responsePlanRepository;
        this.assetRepository = assetRepository;
        this.responsePlanningStrategy = responsePlanningStrategy;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Generate response options for a completed simulation.
     *
     * @param simulationId the completed simulation to base response on
     * @param operatorId the operator requesting the response options
     * @return the generated response plan
     */
    public ResponsePlan generateResponseOptions(
            CommonKernel.SimulationId simulationId,
            CommonKernel.UserId operatorId
    ) {
        // Load and validate simulation
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot generate response options for simulation in state: " + simulation.getStatus());
        }

        // Load incident via correlation ID
        Incident incident = incidentRepository.findByCorrelationId(simulation.getCorrelationId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found for simulation: " + simulationId));

        // Load impact result from simulation
        ImpactResult impactResult = simulation.getResult();
        if (impactResult == null) {
            throw new IllegalStateException("Simulation has no result: " + simulationId);
        }

        // Build planning context
        ResponsePlanningStrategy.PlanningContext context = buildPlanningContext(incident, simulation, impactResult);

        // Generate response plan using strategy
        ResponsePlan plan = responsePlanningStrategy.plan(incident, buildTopologySnapshot(impactResult), context);

        // Set metadata - create a new plan with metadata
        ResponsePlan planWithMetadata = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(incident.getId())
                .name("Response Plan for " + incident.getIncidentNumber())
                .description("Generated from simulation " + simulation.getScenarioId())
                .createdBy(operatorId)
                .correlationId(simulation.getCorrelationId())
                .build();

        // Add actions from the strategy-generated plan
        for (ResponseAction action : plan.getActions()) {
            planWithMetadata.addAction(action);
        }

        // Save plan
        ResponsePlan saved = responsePlanRepository.save(planWithMetadata);

        // Publish event
        eventPublisher.publishResponsePlanCreated(saved, simulation, incident);

        return saved;
    }

    /**
     * Generate multiple response options (alternative plans) for comparison.
     */
    public List<ResponsePlan> generateMultipleOptions(
            CommonKernel.SimulationId simulationId,
            CommonKernel.UserId operatorId,
            int maxOptions
    ) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        if (simulation.getStatus() != SimulationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot generate response options for simulation in state: " + simulation.getStatus());
        }

        Incident incident = incidentRepository.findByCorrelationId(simulation.getCorrelationId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found for simulation: " + simulationId));

        ImpactResult impactResult = simulation.getResult();
        if (impactResult == null) {
            throw new IllegalStateException("Simulation has no result: " + simulationId);
        }

        // Get primary plan
        ResponsePlanningStrategy.PlanningContext primaryContext = buildPlanningContext(incident, simulation, simulation.getResult());
        ResponsePlan primaryPlan = responsePlanningStrategy.plan(
                incident, buildTopologySnapshot(simulation.getResult()), primaryContext
        );

        // Rebuild primary plan with metadata
        ResponsePlan primaryPlanWithMetadata = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(incident.getId())
                .name("Primary Response for " + simulation.getScenarioId())
                .description("Primary response option")
                .createdBy(operatorId)
                .correlationId(simulation.getCorrelationId())
                .build();

        // Add actions from the strategy-generated plan
        for (ResponseAction action : primaryPlan.getActions()) {
            primaryPlanWithMetadata.addAction(action);
        }

        ResponsePlan savedPrimary = responsePlanRepository.save(primaryPlanWithMetadata);

        // For now, return just the primary plan
        // In a full implementation, would generate alternative strategies
        List<ResponsePlan> options = new ArrayList<>();
        options.add(savedPrimary);

        eventPublisher.publishResponsePlanCreated(savedPrimary, 
                simulationRepository.findById(simulationId).orElseThrow(), 
                incidentRepository.findById(incident.getId()).orElseThrow());

        return options;
    }

    /**
     * Update an existing response plan (e.g., after operator revision).
     */
    public ResponsePlan updateResponsePlan(
            CommonKernel.ResponsePlanId planId,
            CommonKernel.UserId operatorId,
            Map<String, Object> updates
    ) {
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        ResponsePlan.PlanStatus previousStatus = plan.getStatus();

        // Apply updates
        if (updates.containsKey("name")) {
            plan = ResponsePlan.builder()
                    .id(plan.getId())
                    .incidentId(plan.getIncidentId())
                    .name((String) updates.get("name"))
                    .description(plan.getDescription())
                    .build();
        }
        if (updates.containsKey("description")) {
            plan = ResponsePlan.builder()
                    .id(plan.getId())
                    .incidentId(plan.getIncidentId())
                    .name(plan.getName())
                    .description((String) updates.get("description"))
                    .build();
        }

        ResponsePlan saved = responsePlanRepository.save(plan);

        eventPublisher.publishResponsePlanUpdated(saved, previousStatus, operatorId.toString(), "plan_updated");

        return saved;
    }

    /**
     * Submit a response plan for approval.
     */
    public ResponsePlan submitForApproval(CommonKernel.ResponsePlanId planId, CommonKernel.UserId operatorId) {
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        plan.submitForApproval();
        ResponsePlan saved = responsePlanRepository.save(plan);

        eventPublisher.publishResponsePlanUpdated(saved, plan.getStatus(), operatorId.toString(), "submitted_for_approval");

        return saved;
    }

    /**
     * Approve a response plan.
     */
    public ResponsePlan approvePlan(CommonKernel.ResponsePlanId planId, CommonKernel.UserId approvedBy) {
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        plan.approve(approvedBy);
        ResponsePlan saved = responsePlanRepository.save(plan);

        eventPublisher.publishResponsePlanUpdated(saved, plan.getStatus(), approvedBy.toString(), "approved");

        return saved;
    }

    /**
     * Start execution of an approved response plan.
     */
    public ResponsePlan startExecution(CommonKernel.ResponsePlanId planId, CommonKernel.UserId operatorId) {
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        plan.startExecution();
        ResponsePlan saved = responsePlanRepository.save(plan);

        eventPublisher.publishResponsePlanUpdated(saved, plan.getStatus(), operatorId.toString(), "execution_started");

        return saved;
    }

    private ResponsePlanningStrategy.PlanningContext buildPlanningContext(Incident incident, Simulation simulation, ImpactResult impactResult) {
        // Get available protection actions based on affected assets
        List<ProtectionAction> availableActions = getAvailableProtectionActions(impactResult);

        // Build resource constraints from simulation/incident
        ResponsePlanningStrategy.ResourceConstraints constraints = new ResponsePlanningStrategy.ResourceConstraints(
                5,
                1_000_000.0,
                CommonKernel.TimeRange.of(Instant.now(), Instant.now().plus(Duration.ofHours(24)))
        );

        return new ResponsePlanningStrategy.PlanningContext(availableActions, constraints, simulation.getCorrelationId());
    }

    private List<ProtectionAction> getAvailableProtectionActions(ImpactResult impactResult) {
        List<ProtectionAction> actions = new ArrayList<>();

        // Generate protection actions based on affected asset types
        for (String assetId : impactResult.getAffectedAssetIds()) {
            if (assetId.isEmpty()) continue;
            
            // Add generic protection actions based on impact type
            actions.add(ProtectionAction.builder()
                    .actionId("ACT-" + UUID.randomUUID().toString().substring(0, 8))
                    .name("Assess Damage - Asset " + assetId)
                    .description("On-site damage assessment for affected asset")
                    .category(ActionCategory.INSPECTION_PROGRAM)
                    .estimatedCost(5000.0)
                    .expectedRiskReduction(0.1)
                    .confidence(0.9)
                    .priority(1)
                    .build());

            actions.add(ProtectionAction.builder()
                    .actionId("ACT-" + UUID.randomUUID().toString().substring(0, 8))
                    .name("Deploy Repair Crew - Asset " + assetId)
                    .description("Dispatch field repair crew to affected asset")
                    .category(ActionCategory.MAINTENANCE_PROGRAM)
                    .estimatedCost(25000.0)
                    .expectedRiskReduction(0.3)
                    .confidence(0.8)
                    .priority(2)
                    .build());
        }

        return actions;
    }

    private com.gridsentinel.grid.TopologySnapshot buildTopologySnapshot(ImpactResult impactResult) {
        // In a full implementation, this would load the actual topology
        return com.gridsentinel.grid.TopologySnapshot.builder()
                .nodes(List.of())
                .edges(List.of())
                .build();
    }

    // Port interfaces
    public interface SimulationRepository {
        Optional<Simulation> findById(CommonKernel.SimulationId id);
    }

    public interface IncidentRepository {
        Optional<Incident> findById(CommonKernel.IncidentId id);
        Optional<Incident> findByCorrelationId(CommonKernel.CorrelationId correlationId);
    }

    public interface ResponsePlanRepository {
        ResponsePlan save(ResponsePlan plan);
        Optional<ResponsePlan> findById(CommonKernel.ResponsePlanId id);
        List<ResponsePlan> findByIncidentId(CommonKernel.IncidentId incidentId);
        List<ResponsePlan> findByStatus(ResponsePlan.PlanStatus status);
    }

    public interface AssetRepository {
        Optional<Asset> findById(CommonKernel.AssetId id);
    }

    public interface EventPublisher {
        void publishResponsePlanCreated(ResponsePlan plan, Simulation simulation, Incident incident);
        void publishResponsePlanUpdated(ResponsePlan plan, ResponsePlan.PlanStatus previousStatus, String actorId, String action);
    }
}