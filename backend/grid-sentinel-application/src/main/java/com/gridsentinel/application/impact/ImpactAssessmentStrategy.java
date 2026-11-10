package com.gridsentinel.application.impact;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.incident.Incident;

import java.util.Set;

/**
 * Strategy for assessing the impact of an incident on grid infrastructure.
 * <p>
 * Implementations provide deterministic, explainable impact assessments
 * based on topology, asset criticality, and incident characteristics.
 * </p>
 */
public interface ImpactAssessmentStrategy {

    /**
     * Assess the impact of an incident.
     *
     * @param context the assessment context containing incident and topology data
     * @return the impact assessment result
     */
    ImpactAssessmentResult assess(ImpactAssessmentContext context);

    /**
     * Get strategy metadata for explainability.
     */
    StrategyMetadata metadata();
}