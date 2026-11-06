package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a simulation completes (successfully or with failure).
 * Emitted when a Simulation entity transitions to COMPLETED, FAILED, or CANCELLED.
 */
public final class SimulationCompletedEvent extends GridEvent<SimulationCompletedEvent.Payload> {

    private final String simulationId;
    private final String name;
    private final String scenarioId;
    private final String status;
    private final String strategyName;
    private final String strategyVersion;
    private final Long randomSeed;
    private final String errorMessage;

    public SimulationCompletedEvent(CorrelationId correlationId,
                                    String simulationId,
                                    String name,
                                    String scenarioId,
                                    String status,
                                    String strategyName,
                                    String strategyVersion,
                                    Long randomSeed,
                                    String errorMessage,
                                    String source) {
        super("SimulationCompleted", correlationId, "Simulation", simulationId, 1,
                determineSeverity(status),
                "simulation-engine");
        this.simulationId = Objects.requireNonNull(simulationId);
        this.name = name;
        this.scenarioId = scenarioId;
        this.status = status;
        this.strategyName = strategyName;
        this.strategyVersion = strategyVersion;
        this.randomSeed = randomSeed;
        this.errorMessage = errorMessage;
    }

    private static EventSeverity determineSeverity(String status) {
        return switch (status) {
            case "FAILED" -> EventSeverity.HIGH;
            case "CANCELLED" -> EventSeverity.LOW;
            default -> EventSeverity.MEDIUM;
        };
    }

    @Override
    public String eventType() {
        return "SimulationCompleted";
    }

    @Override
    public Payload payload() {
        return new Payload(
                simulationId,
                name,
                scenarioId,
                status,
                strategyName,
                strategyVersion,
                randomSeed,
                errorMessage
        );
    }

    public record Payload(
            String simulationId,
            String name,
            String scenarioId,
            String status,
            String strategyName,
            String strategyVersion,
            Long randomSeed,
            String errorMessage
    ) {}
}