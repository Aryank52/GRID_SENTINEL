package com.gridsentinel.infrastructure.api.spatial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Properties for Incident GeoJSON features.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentProperties {
    private UUID id;
    private String incidentNumber;
    private String state;
    private String severity;
    private UUID originatingThreatId;
    private UUID primaryAssetId;
    private UUID primaryGridNodeId;
    private String description;
    private String operatorNotes;
    private UUID assignedOperatorId;
    private UUID incidentCommanderId;
    private Instant detectedAt;
    private Instant triagedAt;
    private Instant confirmedAt;
    private Instant responseStartedAt;
    private Instant containedAt;
    private Instant recoveringAt;
    private Instant resolvedAt;
    private Instant closedAt;
    private Boolean isActive;
    private Boolean isTerminal;
    private List<Map<String, Object>> stateHistory;
}