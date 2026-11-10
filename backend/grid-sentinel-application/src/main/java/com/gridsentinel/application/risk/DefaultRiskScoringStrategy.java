package com.gridsentinel.application.risk;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.risk.FactorBreakdown;
import com.gridsentinel.risk.RiskFactor;
import com.gridsentinel.risk.RiskScore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Default deterministic risk scoring strategy.
 * <p>
 * Computes risk score as weighted combination of:
 * - Asset criticality (weight: 0.30)
 * - Asset health (weight: 0.25)
 * - Threat exposure (weight: 0.25) - mapped to incident frequency/environmental risk
 * - Redundancy (weight: 0.20)
 * </p>
 */
@Component
public class DefaultRiskScoringStrategy implements RiskScoringStrategy {

    private static final String STRATEGY_NAME = "default-weighted-risk-v1";
    private static final String STRATEGY_VERSION = "1.0";

    // Factor weights (must sum to 1.0)
    private static final double WEIGHT_CRITICALITY = 0.30;
    private static final double WEIGHT_HEALTH = 0.25;
    private static final double WEIGHT_THREAT = 0.25;
    private static final double WEIGHT_REDUNDANCY = 0.20;

    @Override
    public RiskScore score(Asset asset, RiskScoringContext context) {
        // Calculate individual factor scores
        double criticalityScore = criticalityToScore(asset.getCriticality());
        double healthScore = 1.0 - asset.getHealthIndex().value(); // Invert health (lower health = higher risk)
        double threatScore = calculateThreatScore(context);
        double redundancyScore = 1.0 - asset.redundancyFactor(); // Invert redundancy (less redundancy = higher risk)

        // Compute weighted risk score
        double riskScore = (WEIGHT_CRITICALITY * criticalityScore) +
                (WEIGHT_HEALTH * healthScore) +
                (WEIGHT_THREAT * threatScore) +
                (WEIGHT_REDUNDANCY * redundancyScore);

        // Clamp to [0.0, 1.0]
        riskScore = Math.max(0.0, Math.min(1.0, riskScore));

        // Build factor breakdown using correct RiskFactor enum values
        FactorBreakdown breakdown = FactorBreakdown.builder()
                .addFactor(RiskFactor.ASSET_CRITICALITY, WEIGHT_CRITICALITY, criticalityScore, criticalityScore)
                .addFactor(RiskFactor.ASSET_HEALTH, WEIGHT_HEALTH, asset.getHealthIndex().value(), healthScore)
                .addFactor(RiskFactor.INCIDENT_FREQUENCY, WEIGHT_THREAT, threatScore, threatScore)
                .addFactor(RiskFactor.REDUNDANCY_LEVEL, WEIGHT_REDUNDANCY, asset.redundancyFactor(), redundancyScore)
                .build();

        return RiskScore.builder()
                .id(CommonKernel.RiskScoreId.generate())
                .assetId(asset.getId())
                .score(riskScore)
                .modelName("default-weighted-risk")
                .modelVersion(STRATEGY_VERSION)
                .strategyName(STRATEGY_NAME)
                .factorBreakdown(breakdown)
                .computedAt(Instant.now())
                .correlationId(context.correlationId())
                .build();
    }

    @Override
    public List<RiskScore> scoreBatch(List<Asset> assets, RiskScoringContext context) {
        return assets.stream()
                .map(asset -> score(asset, context))
                .toList();
    }

    @Override
    public StrategyMetadata metadata() {
        return new StrategyMetadata(
                STRATEGY_NAME,
                STRATEGY_VERSION,
                "Default weighted risk scoring using criticality, health, threat exposure, and redundancy",
                List.of("asset_criticality", "asset_health", "incident_frequency", "redundancy_level")
        );
    }

    private double criticalityToScore(CommonKernel.Criticality criticality) {
        return switch (criticality) {
            case MISSION_CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
        };
    }

    private double calculateThreatScore(RiskScoringContext context) {
        // In a full implementation, this would query active threats for the asset
        // For now, use environmental and historical factors as proxy
        double envRisk = 0.0;

        // Temperature factor (extreme temps increase risk)
        if (context.environment().temperatureCelsius() > 35 || context.environment().temperatureCelsius() < -10) {
            envRisk += 0.2;
        }

        // Wind factor (high winds increase risk)
        if (context.environment().windSpeedKmh() > 50) {
            envRisk += 0.3;
        } else if (context.environment().windSpeedKmh() > 30) {
            envRisk += 0.15;
        }

        // Weather alert factor
        if ("SEVERE".equalsIgnoreCase(context.environment().weatherAlertLevel())) {
            envRisk += 0.4;
        } else if ("WATCH".equalsIgnoreCase(context.environment().weatherAlertLevel())) {
            envRisk += 0.2;
        }

        // Historical factor
        if (context.history().incidentCountLastYear() > 5) {
            envRisk += 0.2;
        } else if (context.history().incidentCountLastYear() > 2) {
            envRisk += 0.1;
        }

        return Math.min(1.0, envRisk);
    }
}