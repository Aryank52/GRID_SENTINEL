package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Properties for RiskScore GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreProperties {
    private UUID id;
    private UUID assetId;
    private Double score;
    private String band;
    private String modelName;
    private String modelVersion;
    private String strategyName;
    private Map<String, Object> factorBreakdown;
    private Instant computedAt;
    private UUID correlationId;
}