package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a grid asset's status changes (health, criticality, status, etc.).
 */
public final class GridAssetStatusChangedEvent extends GridEvent<GridAssetStatusChangedEvent.Payload> {

    private final String assetId;
    private final String assetName;
    private final String assetType;
    private final String previousHealthStatus;
    private final String newHealthStatus;
    private final String previousCriticality;
    private final String newCriticality;
    private final double previousHealthIndex;
    private final double newHealthIndex;
    private final String previousRiskBand;
    private final String newRiskBand;
    private final String changeReason;

    public GridAssetStatusChangedEvent(CorrelationId correlationId,
                                       String assetId,
                                       String assetName,
                                       String assetType,
                                       String previousHealthStatus,
                                       String newHealthStatus,
                                       String previousCriticality,
                                       String newCriticality,
                                       double previousHealthIndex,
                                       double newHealthIndex,
                                       String previousRiskBand,
                                       String newRiskBand,
                                       String changeReason,
                                       String source) {
        super("GridAssetStatusChanged", correlationId, "Asset", assetId, 1,
                determineSeverity(previousHealthStatus, newHealthStatus,
                        previousCriticality, newCriticality),
                "asset-management");
        this.assetId = Objects.requireNonNull(assetId);
        this.assetName = assetName;
        this.assetType = assetType;
        this.previousHealthStatus = previousHealthStatus;
        this.newHealthStatus = newHealthStatus;
        this.previousCriticality = previousCriticality;
        this.newCriticality = newCriticality;
        this.previousHealthIndex = previousHealthIndex;
        this.newHealthIndex = newHealthIndex;
        this.previousRiskBand = previousRiskBand;
        this.newRiskBand = newRiskBand;
        this.changeReason = changeReason;
    }

    private static EventSeverity determineSeverity(String prevHealth, String newHealth,
                                                   String prevCrit, String newCrit) {
        // Critical if health degraded to CRITICAL or criticality increased to MISSION_CRITICAL
        if ("CRITICAL".equals(newHealth) || "MISSION_CRITICAL".equals(newCrit)) {
            return EventSeverity.CRITICAL;
        }
        // High if health degraded to DEGRADED or criticality increased to HIGH
        if ("DEGRADED".equals(newHealth) || "HIGH".equals(newCrit)) {
            return EventSeverity.HIGH;
        }
        // Medium if health improved
        if ("HEALTHY".equals(newHealth) && !"HEALTHY".equals(prevHealth)) {
            return EventSeverity.MEDIUM;
        }
        return EventSeverity.LOW;
    }

    @Override
    public String eventType() {
        return "GridAssetStatusChanged";
    }

    @Override
    public Payload payload() {
        return new Payload(
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
    }

    public record Payload(
            String assetId,
            String assetName,
            String assetType,
            String previousHealthStatus,
            String newHealthStatus,
            String previousCriticality,
            String newCriticality,
            double previousHealthIndex,
            double newHealthIndex,
            String previousRiskBand,
            String newRiskBand,
            String changeReason
    ) {}
}