package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Properties for GridNode GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GridNodeProperties {
    private UUID id;
    private String name;
    private String type;
    private Double voltageLevelKv;
    private UUID primaryAssetId;
    private String operator;
    private Integer degree;
    private Boolean isSubstation;
    private Boolean isGeneration;
    private Boolean isLoad;
}