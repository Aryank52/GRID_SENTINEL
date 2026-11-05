package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Properties for ThreatEvent GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatEventProperties {
    private UUID id;
    private String type;
    private String severity;
    private Double confidence;
    private Instant detectedAt;
    private Instant firstObservedAt;
    private UUID affectedAssetId;
    private UUID nearbyGridNodeId;
    private String description;
    private String rationale;
    private String status;
    private String detectionStrategy;
    private String strategyVersion;
    private Boolean isActionable;
    private Integer evidenceCount;
}