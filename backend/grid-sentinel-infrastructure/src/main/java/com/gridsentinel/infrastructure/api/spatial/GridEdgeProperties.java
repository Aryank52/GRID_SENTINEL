package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Properties for GridEdge GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridEdgeProperties {
    private UUID id;
    private String name;
    private String type;
    private UUID fromNodeId;
    private UUID toNodeId;
    private Double voltageLevelKv;
    private Double thermalRatingAmps;
    private Double mvaRating;
    private Double lengthKm;
    private Integer circuitCount;
    private String conductorType;
    private Integer yearInstalled;
    private UUID primaryAssetId;
    private Boolean isTransformer;
}