package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Event fired when a new threat is detected by the detection pipeline.
 * Emitted when a ThreatEvent entity is first created and persisted.
 */
public final class ThreatDetectedEvent extends GridEvent<ThreatDetectedEvent.Payload> {

    private final String threatEventId;
    private final String threatType;
    private final String severity;
    private final double confidence;
    private final Instant detectedAt;
    private final String affectedAssetId;
    private final String nearbyGridNodeId;
    private final String description;
    private final String detectionStrategy;
    private final String strategyVersion;
    private final Set<String> evidenceIds;

    public ThreatDetectedEvent(CorrelationId correlationId,
                               String threatEventId,
                               String threatType,
                               String severity,
                               double confidence,
                               Instant detectedAt,
                               String affectedAssetId,
                               String nearbyGridNodeId,
                               String description,
                               Set<String> evidenceIds,
                               String detectionStrategy,
                               String strategyVersion,
                               String source) {
        super("ThreatDetected", correlationId, "ThreatEvent", threatEventId, 0,
                severity != null && (severity.equals("HIGH") || severity.equals("CRITICAL"))
                        ? EventSeverity.HIGH : EventSeverity.MEDIUM,
                "detection-pipeline");
        this.threatEventId = Objects.requireNonNull(threatEventId);
        this.threatType = Objects.requireNonNull(threatType);
        this.severity = Objects.requireNonNull(severity);
        this.confidence = confidence;
        this.detectedAt = Objects.requireNonNull(detectedAt);
        this.affectedAssetId = affectedAssetId;
        this.nearbyGridNodeId = nearbyGridNodeId;
        this.description = description;
        this.detectionStrategy = detectionStrategy;
        this.strategyVersion = strategyVersion;
        this.evidenceIds = Set.copyOf(evidenceIds);
    }

    @Override
    public String eventType() {
        return "ThreatDetected";
    }

    @Override
    public Payload payload() {
        return new Payload(
                threatEventId,
                threatType,
                severity,
                confidence,
                detectedAt.toString(),
                affectedAssetId,
                nearbyGridNodeId,
                description,
                evidenceIds,
                detectionStrategy,
                strategyVersion
        );
    }

    public record Payload(
            String threatEventId,
            String threatType,
            String severity,
            double confidence,
            String detectedAt,
            String affectedAssetId,
            String nearbyGridNodeId,
            String description,
            Set<String> evidenceIds,
            String detectionStrategy,
            String strategyVersion
    ) {}
}