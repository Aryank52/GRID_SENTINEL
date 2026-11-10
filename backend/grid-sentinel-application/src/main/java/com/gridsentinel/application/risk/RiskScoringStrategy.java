package com.gridsentinel.application.risk;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.risk.RiskScore;

import java.util.List;

/**
 * Application-layer risk scoring strategy interface.
 * Uses public application-layer context records.
 */
public interface RiskScoringStrategy {

    /**
     * Compute risk score for a single asset given context.
     */
    RiskScore score(Asset asset, RiskScoringContext context);

    /**
     * Batch score multiple assets efficiently.
     */
    List<RiskScore> scoreBatch(List<Asset> assets, RiskScoringContext context);

    /**
     * Get strategy metadata for explainability.
     */
    StrategyMetadata metadata();
}