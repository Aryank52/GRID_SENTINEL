package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a risk score is computed or updated.
 * Emitted when a RiskScore entity is created or updated.
 */
public final class RiskScoreUpdatedEvent extends GridEvent {

    private final String riskScoreId;
    private final String assetId;
    private final double previousScore;
    private final double newScore;
    private final String previousBand;
    private final String newBand;
    private final String modelName;
    private final String modelVersion;
    private final String strategyName;
    private final String correlationId;

    public RiskScoreUpdatedEvent(CorrelationId correlationId,
                                 String riskScoreId,
                                 String assetId,
                                 double previousScore,
                                 double newScore,
                                 String previousBand,
                                 String newBand,
                                 String modelName,
                                 String modelVersion,
                                 String strategyName,
                                 String source) {
        super("RiskScoreUpdated", correlationId, "RiskScore", riskScoreId, 1,
                determineSeverity(newScore, newBand),
                "risk-engine");
        this.riskScoreId = Objects.requireNonNull(riskScoreId);
        this.assetId = assetId;
        this.previousScore = previousScore;
        this.newScore = newScore;
        this.previousBand = previousBand;
        this.newBand = newBand;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.strategyName = strategyName;
        this.correlationId = correlationId != null ? correlationId.toString() : null;
    }

    private static EventSeverity determineSeverity(double score, String band) {
        return switch (band) {
            case "CRITICAL" -> EventSeverity.CRITICAL;
            case "HIGH" -> EventSeverity.HIGH;
            case "MEDIUM" -> EventSeverity.MEDIUM;
            default -> EventSeverity.LOW;
        };
    }

    @Override
    public String eventType() {
        return "RiskScoreUpdated";
    }

    @Override
    protected Object payload() {
        return new Payload(
                riskScoreId,
                assetId,
                previousScore,
                newScore,
                previousBand,
                newBand,
                modelName,
                modelVersion,
                strategyName
        );
    }

    public record Payload(
            String riskScoreId,
            String assetId,
            double previousScore,
            double newScore,
            String previousBand,
            String newBand,
            String modelName,
            String modelVersion,
            String strategyName
    ) {}
}