package com.gridsentinel.strategy;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.risk.RiskFactor;
import com.gridsentinel.risk.ProtectionAction;
import com.gridsentinel.observation.Observation;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.simulation.Simulation;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.grid.TopologySnapshot;
import com.gridsentinel.protection.ProtectionPlan;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ObservationSourceType;

import java.util.List;
import java.util.Set;

/**
 * Strategy interfaces for behavioral variability in the domain.
 * Each interface defines a contract that can have multiple implementations
 * (deterministic, ML-based, rule-based, etc.) without coupling callers to concrete classes.
 */
public final class StrategyInterfaces {

    private StrategyInterfaces() {}

    // =========================================================================
    // Risk Scoring
    // =========================================================================

    /**
     * Strategy for computing risk scores for assets.
     * Implementations may vary: deterministic weighted factors, contextual fusion, ML models.
     */
    public interface RiskScoringStrategy {
        /**
         * Compute risk score for a single asset given current context.
         *
         * @param asset the asset to score
         * @param context additional context (topology, environment, history)
         * @return explainable risk score with factor breakdown
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

    /**
     * Context provided to risk scoring strategies.
     */
    record RiskScoringContext(
            TopologySnapshot topology,
            EnvironmentalContext environment,
            HistoricalContext history,
            CommonKernel.CorrelationId correlationId
    ) {}

    record EnvironmentalContext(
            double temperatureCelsius,
            double windSpeedKmh,
            double humidityPercent,
            String weatherAlertLevel
    ) {}

    record HistoricalContext(
            int incidentCountLastYear,
            double avgTimeToResolveHours,
            List<String> previousFailureModes
    ) {}

    record StrategyMetadata(
            String strategyName,
            String version,
            String description,
            List<String> requiredInputs
    ) {}

    // =========================================================================
    // Threat Detection
    // =========================================================================

    /**
     * Strategy for detecting threats from observations.
     * Pipeline: Observation -> Validation -> Normalization -> Feature Extraction -> Anomaly Score -> Fusion -> Classification.
     */
    public interface ThreatDetectionStrategy {
        /**
         * Process a batch of observations and emit threat events.
         */
        List<ThreatEvent> detect(List<Observation> observations, DetectionContext context);

        /**
         * Process a single observation (for streaming).
         */
        ThreatEvent detect(Observation observation, DetectionContext context);

        StrategyMetadata metadata();
    }

    record DetectionContext(
            TopologySnapshot topology,
            Set<Asset> nearbyAssets,
            CommonKernel.CorrelationId correlationId
    ) {}

    // =========================================================================
    // Evidence Fusion
    // =========================================================================

    /**
     * Policy for fusing multiple evidence sources into a unified threat assessment.
     * Implementations: Weighted average, Dempster-Shafer, Bayesian, ML-based.
     */
    public interface EvidenceFusionPolicy {
        /**
         * Fuse multiple threat events (potentially from different sources) into a unified assessment.
         */
        ThreatEvent fuse(List<ThreatEvent> events, FusionContext context);

        StrategyMetadata metadata();
    }

    record FusionContext(
            TopologySnapshot topology,
            CommonKernel.CorrelationId correlationId
    ) {}

    // =========================================================================
    // Impact Simulation
    // =========================================================================

    /**
     * Strategy for simulating failure impact on the grid.
     * Must be deterministic: same input + same version = same output.
     */
    public interface ImpactSimulationStrategy {
        /**
         * Simulate the impact of a failure scenario.
         *
         * @param scenario the failure scenario to simulate
         * @param topology the grid topology at simulation time
         * @return deterministic impact result
         */
        ImpactResult simulate(SimulationScenario scenario, TopologySnapshot topology);

        StrategyMetadata metadata();
    }

    /**
     * Parameter object for simulation scenarios.
     */
    record SimulationScenario(
            String scenarioId,
            String name,
            Set<CommonKernel.GridNodeId> initialFailedNodes,
            Set<CommonKernel.GridEdgeId> initialFailedEdges,
            double timeHorizonHours,
            SimulationParameters parameters
    ) {}

    record SimulationParameters(
            boolean cascadeEnabled,
            double loadRedistributionFactor,
            int maxIterations,
            long randomSeed // for deterministic stochastic elements
    ) {}

    // =========================================================================
    // Response Planning
    // =========================================================================

    /**
     * Strategy for generating prioritized response plans from incidents.
     */
    public interface ResponsePlanningStrategy {
        /**
         * Generate a response plan for the given incident.
         */
        ResponsePlan plan(Incident incident, TopologySnapshot topology, PlanningContext context);

        StrategyMetadata metadata();
    }

    record PlanningContext(
            List<ProtectionAction> availableActions,
            ResourceConstraints constraints,
            CommonKernel.CorrelationId correlationId
    ) {}

    record ResourceConstraints(
            int maxConcurrentTeams,
            double budgetLimit,
            CommonKernel.TimeRange timeWindow
    ) {}

    // =========================================================================
    // Protection Optimization
    // =========================================================================

    /**
     * Strategy for optimizing protection measure selection given budget/risk constraints.
     */
    public interface ProtectionOptimizationStrategy {
        /**
         * Optimize protection plan for a set of assets.
         */
        ProtectionPlan optimize(Set<Asset> assets, OptimizationContext context);

        StrategyMetadata metadata();
    }

    record OptimizationContext(
            double budgetLimit,
            double targetRiskReduction,
            List<ProtectionAction> candidateActions,
            CommonKernel.CorrelationId correlationId
    ) {}

    // =========================================================================
    // Observation Source Adapter
    // =========================================================================

    /**
     * Adapter for ingesting observations from different source types.
     * Normalizes heterogeneous inputs into domain Observation objects.
     */
    public interface ObservationSourceAdapter<T> {
        /**
         * Convert raw source payload to domain Observation.
         */
        Observation adapt(T rawPayload, AdapterContext context);

        /**
         * Validate raw payload before adaptation.
         */
        boolean validate(T rawPayload);

        /**
         * Get the source type this adapter handles.
         */
        ObservationSourceType sourceType();

        StrategyMetadata metadata();
    }

    record AdapterContext(
            CommonKernel.CorrelationId correlationId,
            String sourceIdentifier
    ) {}

    // =========================================================================
    // Model Inference Port
    // =========================================================================

    /**
     * Port for ML model inference (ONNX, Tribuo, Smile, custom).
     * Keeps domain independent of specific ML framework.
     */
    public interface ModelInferencePort<Input, Output> {
        /**
         * Run inference on input.
         */
        Output infer(Input input);

        /**
         * Get model metadata.
         */
        ModelMetadata metadata();
    }

    record ModelMetadata(
            String modelName,
            String version,
            String framework, // ONNX, Tribuo, Smile, etc.
            String inputSchema,
            String outputSchema,
            long trainedAtEpochMillis
    ) {}

    // =========================================================================
    // Explanation Service
    // =========================================================================

    /**
     * Service for generating human-readable explanations of domain decisions.
     * Implementations: Rule-trace, SHAP, LIME, grounded templates.
     */
    public interface ExplanationService {
        /**
         * Explain a risk score.
         */
        String explainRiskScore(RiskScore score);

        /**
         * Explain a threat classification.
         */
        String explainThreatEvent(ThreatEvent event);

        /**
         * Explain a simulation result.
         */
        String explainSimulationResult(ImpactResult result);

        /**
         * Explain a response plan.
         */
        String explainResponsePlan(ResponsePlan plan);
    }
}