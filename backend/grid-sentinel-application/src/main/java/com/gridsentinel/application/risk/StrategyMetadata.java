package com.gridsentinel.application.risk;

import java.util.List;

/**
 * Strategy metadata for explainability.
 */
public record StrategyMetadata(
        String strategyName,
        String version,
        String description,
        List<String> requiredInputs
) {}