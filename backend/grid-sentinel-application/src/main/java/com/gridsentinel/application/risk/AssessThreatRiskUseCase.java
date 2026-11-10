package com.gridsentinel.application.risk;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.risk.RiskScore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Use case for assessing risk from a detected threat.
 * <p>
 * When a threat is detected, this use case evaluates the risk to affected assets
 * by combining asset criticality, health, redundancy, and threat severity.
 * </p>
 */
@Service
@Transactional
public class AssessThreatRiskUseCase {

    private final AssetRepository assetRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final RiskScoringStrategy riskScoringStrategy;
    private final EventPublisher eventPublisher;

    public AssessThreatRiskUseCase(
            AssetRepository assetRepository,
            RiskScoreRepository riskScoreRepository,
            RiskScoringStrategy riskScoringStrategy,
            EventPublisher eventPublisher
    ) {
        this.assetRepository = assetRepository;
        this.riskScoreRepository = riskScoreRepository;
        this.riskScoringStrategy = riskScoringStrategy;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Assess risk for all assets affected by a threat event.
     *
     * @param threatEvent the detected threat event
     * @return list of computed risk scores
     */
    public List<RiskScore> assessThreatRisk(ThreatEvent threatEvent) {
        List<RiskScore> riskScores = new ArrayList<>();

        // Get affected asset from the threat
        CommonKernel.AssetId affectedAssetId = threatEvent.getAffectedAssetId();
        if (affectedAssetId == null) {
            // Fallback: find assets near the threat location
            return riskScores;
        }

        // Build risk scoring context
        RiskScoringContext context = buildContext(threatEvent);

        // Score the affected asset
        Asset asset = assetRepository.findById(affectedAssetId).orElse(null);
        if (asset != null) {
            RiskScore riskScore = riskScoringStrategy.score(asset, context);
            riskScore = riskScoreRepository.save(riskScore);
            riskScores.add(riskScore);

            // Publish real-time event
            eventPublisher.publishRiskScoreUpdated(riskScore);
        }

        return riskScores;
    }

    /**
     * Re-assess risk for a specific asset (e.g., after threat escalation or mitigation).
     */
    public RiskScore reassessAssetRisk(CommonKernel.AssetId assetId, CommonKernel.CorrelationId correlationId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetId));

        // Build context with current threat environment
        RiskScoringContext context = buildContextForAsset(asset, correlationId);

        RiskScore riskScore = riskScoringStrategy.score(asset, context);
        riskScore = riskScoreRepository.save(riskScore);

        eventPublisher.publishRiskScoreUpdated(riskScore);
        return riskScore;
    }

    private RiskScoringContext buildContext(ThreatEvent threat) {
        // Build topology snapshot from current grid state
        TopologySnapshot topology = buildTopologySnapshot();

        // Build environmental context
        EnvironmentalContext environment = new EnvironmentalContext(
                20.0, // temperature
                10.0, // wind speed
                60.0, // humidity
                "NONE" // weather alert
        );

        // Build historical context
        HistoricalContext history = new HistoricalContext(
                0, // incident count
                0.0, // avg resolution time
                List.of() // previous failure modes
        );

        return new RiskScoringContext(
                topology,
                environment,
                history,
                threat.getCorrelationId() != null ? threat.getCorrelationId() : CommonKernel.CorrelationId.generate()
        );
    }

    private RiskScoringContext buildContextForAsset(Asset asset, CommonKernel.CorrelationId correlationId) {
        return new RiskScoringContext(
                buildTopologySnapshot(),
                new EnvironmentalContext(20.0, 10.0, 60.0, "NONE"),
                new HistoricalContext(0, 0.0, List.of()),
                correlationId
        );
    }

    private TopologySnapshot buildTopologySnapshot() {
        // In a full implementation, this would query the grid topology
        // For now, return empty snapshot
        return TopologySnapshot.builder()
                .nodes(List.of())
                .edges(List.of())
                .build();
    }

    // Port interfaces
    public interface AssetRepository {
        Optional<Asset> findById(CommonKernel.AssetId id);
        List<Asset> findByGridNodeId(CommonKernel.GridNodeId nodeId);
    }

    public interface RiskScoreRepository {
        RiskScore save(RiskScore riskScore);
        Optional<RiskScore> findLatestByAssetId(CommonKernel.AssetId assetId);
        List<RiskScore> findByAssetId(CommonKernel.AssetId assetId);
    }

    public interface EventPublisher {
        void publishRiskScoreUpdated(RiskScore riskScore);
    }
}