package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Properties for Asset GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetProperties {
    private UUID id;
    private String name;
    private String type;
    private String typeDisplayName;
    private String criticality;
    private Double healthIndex;
    private String healthStatus;
    private Integer redundancyLevel;
    private Boolean hasRedundancy;
    private UUID gridNodeId;
    private String description;
    private Instant installedAt;
    private Instant lastInspectedAt;
    private String riskBand;
    private Double riskScore;
    private Double baseRiskWeight;
}