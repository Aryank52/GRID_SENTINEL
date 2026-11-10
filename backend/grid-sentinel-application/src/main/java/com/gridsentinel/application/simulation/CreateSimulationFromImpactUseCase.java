package com.gridsentinel.application.simulation;

import com.gridsentinel.application.impact.AssessIncidentImpactUseCase;
import com.gridsentinel.application.impact.ImpactAssessmentResult;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.Simulation;
import com.gridsentinel.simulation.SimulationParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Use case for creating a simulation from an impact assessment result.
 * <p>
 * Converts an {@link ImpactAssessmentResult} into a {@link Simulation} scenario,
 * capturing the baseline topology, affected elements, and assumptions.
 * </p>
 */
@Service
@Transactional
public class CreateSimulationFromImpactUseCase {

    private final IncidentRepository incidentRepository;
    private final ImpactResultRepository impactResultRepository;
    private final SimulationRepository simulationRepository;
    private final EventPublisher eventPublisher;

    public CreateSimulationFromImpactUseCase(
            IncidentRepository incidentRepository,
            ImpactResultRepository impactResultRepository,
            SimulationRepository simulationRepository,
            EventPublisher eventPublisher
    ) {
        this.incidentRepository = incidentRepository;
        this.impactResultRepository = impactResultRepository;
        this.simulationRepository = simulationRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a simulation scenario from an impact assessment result.
     *
     * @param incidentId the incident to base the simulation on
     * @param name optional name for the simulation
     * @param requestedBy the user requesting the simulation
     * @param assumptions optional custom assumptions
     * @return the created simulation
     */
    public Simulation createSimulation(
            CommonKernel.IncidentId incidentId,
            String name,
            CommonKernel.UserId requestedBy,
            Map<String, String> assumptions
    ) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));

        // Get the latest impact result for this incident
        Optional<ImpactResult> impactResultOpt = impactResultRepository.findByScenarioId(
                "INCIDENT-" + incident.getIncidentNumber()
        );

        ImpactResult impactResult = impactResultOpt.orElseGet(() -> buildDefaultImpactResult(incident));

        // Build topology snapshot from impact
        TopologySnapshot topologySnapshot = buildTopologySnapshot(impactResult, incident);

        // Build simulation parameters
        SimulationParameters parameters = SimulationParameters.builder()
                .cascadeEnabled(true)
                .loadRedistributionFactor(1.0)
                .maxIterations(100)
                .randomSeed(System.currentTimeMillis())
                .build();

        // Build scenario assumptions
        Map<String, String> scenarioAssumptions = buildAssumptions(impactResult, incident, assumptions);

        // Generate scenario ID
        String scenarioId = "SIM-" + incident.getIncidentNumber() + "-" + UUID.randomUUID().toString().substring(0, 8);

        // Create simulation
        Simulation simulation = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name(name != null ? name : "Simulation for " + incident.getIncidentNumber())
                .scenarioId(scenarioId)
                .topologySnapshot(buildTopologySnapshotForSimulation(impactResult, incident))
                .topologySnapshotId("TOP-" + scenarioId)
                .strategyName("default-impact-simulation-v1")
                .strategyVersion("1.0")
                .randomSeed(System.currentTimeMillis())
                .parameters(parameters)
                .requestedBy(requestedBy)
                .correlationId(incident.getCorrelationId())
                .build();

        // Store assumptions as scenario metadata (in a real implementation, would persist separately)
        // For now, we include them in the simulation's metadata

        Simulation saved = simulationRepository.save(simulation);

        // Publish event
        eventPublisher.publishSimulationCreated(saved, impactResult, incident, scenarioAssumptions);

        return saved;
    }

    private ImpactResult buildDefaultImpactResult(Incident incident) {
        return ImpactResult.builder()
                .simulationId(CommonKernel.SimulationId.generate())
                .scenarioId("INCIDENT-" + incident.getIncidentNumber())
                .computedAt(Instant.now())
                .iterationsRun(0)
                .converged(true)
                .strategyName("incident-impact-assessment-v1")
                .strategyVersion("1.0")
                .build();
    }

    private TopologySnapshot buildTopologySnapshot(ImpactResult impactResult, Incident incident) {
        // In a full implementation, this would query the grid topology
        // For now, return empty snapshot
        return TopologySnapshot.builder()
                .nodes(Collections.emptyList())
                .edges(Collections.emptyList())
                .build();
    }

    private TopologySnapshot buildTopologySnapshotForSimulation(ImpactResult impactResult, Incident incident) {
        return buildTopologySnapshot(impactResult, incident);
    }

    private Map<String, String> buildAssumptions(
            ImpactResult impactResult,
            Incident incident,
            Map<String, String> customAssumptions
    ) {
        Map<String, String> assumptions = new LinkedHashMap<>();

        // Default assumptions based on impact
        assumptions.put("asset_unavailable", "Primary affected asset taken offline");
        assumptions.put("threat_persists", "Threat condition remains active during simulation");
        assumptions.put("cascade_enabled", "Cascade failure modeling enabled");
        assumptions.put("load_redistribution", "Load redistributed to available capacity");
        assumptions.put("restoration_time_estimate", "Based on asset criticality and historical data");

        if (customAssumptions != null) {
            assumptions.putAll(customAssumptions);
        }

        return assumptions;
    }

    // Port interfaces
    public interface IncidentRepository {
        Optional<Incident> findById(CommonKernel.IncidentId id);
    }

    public interface ImpactResultRepository {
        Optional<ImpactResult> findByScenarioId(String scenarioId);
        Optional<ImpactResult> findBySimulationId(CommonKernel.SimulationId simulationId);
    }

    public interface SimulationRepository {
        Simulation save(Simulation simulation);
        Optional<Simulation> findById(CommonKernel.SimulationId id);
    }

    public interface EventPublisher {
        void publishSimulationCreated(Simulation simulation, ImpactResult impactResult, Incident incident, Map<String, String> assumptions);
    }
}