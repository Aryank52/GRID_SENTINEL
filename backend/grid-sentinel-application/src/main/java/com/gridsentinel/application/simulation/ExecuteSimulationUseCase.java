package com.gridsentinel.application.simulation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.Simulation;
import com.gridsentinel.simulation.SimulationParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Use case for executing a simulation.
 * <p>
 * Runs the simulation using the configured strategy and persists the result.
 * </p>
 */
@Service
@Transactional
public class ExecuteSimulationUseCase {

    private final SimulationRepository simulationRepository;
    private final SimulationStrategy simulationStrategy;
    private final EventPublisher eventPublisher;

    public ExecuteSimulationUseCase(
            SimulationRepository simulationRepository,
            SimulationStrategy simulationStrategy,
            EventPublisher eventPublisher
    ) {
        this.simulationRepository = simulationRepository;
        this.simulationStrategy = simulationStrategy;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Execute a simulation.
     *
     * @param simulationId the simulation to execute
     * @return the completed simulation with result
     */
    public Simulation execute(CommonKernel.SimulationId simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        // Validate state
        if (simulation.getStatus() != SimulationStatus.PENDING) {
            throw new IllegalStateException("Simulation not in PENDING state: " + simulation.getStatus());
        }

        // Transition to RUNNING
        simulation.start();
        Simulation running = simulationRepository.save(simulation);

        // Publish running event
        eventPublisher.publishSimulationRunning(running);

        try {
            // Execute simulation strategy
            ImpactResult result = simulationStrategy.simulate(
                    running.getScenarioId(),
                    running.getTopologySnapshot(),
                    running.getParameters()
            );

            // Complete simulation
            running.complete(result);
            Simulation completed = simulationRepository.save(running);

            // Publish completed event
            eventPublisher.publishSimulationCompleted(completed);

            return completed;

        } catch (Exception e) {
            // Fail simulation
            running.fail("Simulation execution failed: " + e.getMessage());
            Simulation failed = simulationRepository.save(running);

            // Publish failed event
            eventPublisher.publishSimulationFailed(failed);

            throw new RuntimeException("Simulation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Cancel a pending or running simulation.
     */
    public Simulation cancel(CommonKernel.SimulationId simulationId) {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new IllegalArgumentException("Simulation not found: " + simulationId));

        if (simulation.getStatus() == SimulationStatus.COMPLETED ||
            simulation.getStatus() == SimulationStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel terminal simulation: " + simulation.getStatus());
        }

        simulation.cancel();
        Simulation cancelled = simulationRepository.save(simulation);
        eventPublisher.publishSimulationCancelled(cancelled);

        return cancelled;
    }

    // Port interfaces
    public interface SimulationRepository {
        Simulation save(Simulation simulation);
        Optional<Simulation> findById(CommonKernel.SimulationId id);
    }

    public interface EventPublisher {
        void publishSimulationCreated(Simulation simulation);
        void publishSimulationRunning(Simulation simulation);
        void publishSimulationCompleted(Simulation simulation);
        void publishSimulationFailed(Simulation simulation);
        void publishSimulationCancelled(Simulation simulation);
    }
}