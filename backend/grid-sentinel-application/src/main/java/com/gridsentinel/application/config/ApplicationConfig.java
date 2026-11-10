package com.gridsentinel.application.config;

import com.gridsentinel.application.impact.*;
import com.gridsentinel.application.incident.*;
import com.gridsentinel.application.observation.*;
import com.gridsentinel.application.risk.*;
import com.gridsentinel.application.response.*;
import com.gridsentinel.application.simulation.*;
import com.gridsentinel.audit.AuditLog;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.infrastructure.realtime.RealTimeEventService;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.risk.ProtectionAction;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.simulation.ImpactResult;
import com.gridsentinel.simulation.Simulation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application layer configuration for observation, detection, risk assessment, incident management, impact assessment, simulation, and response planning.
 * <p>
 * Wires together the use cases, evidence extractors, detection pipelines, risk scoring, incident creation, impact assessment, simulation, and response planning.
 * Infrastructure adapters are provided separately.
 * </p>
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public IngestObservationUseCase ingestObservationUseCase(
            IngestObservationUseCase.ObservationRepository observationRepository,
            IngestObservationUseCase.EvidenceExtractor evidenceExtractor,
            IngestObservationUseCase.ThreatDetectionPipeline threatDetectionPipeline
    ) {
        return new IngestObservationUseCase(observationRepository, evidenceExtractor, threatDetectionPipeline);
    }

    @Bean
    public IngestObservationUseCase.EvidenceExtractor evidenceExtractor() {
        return new EvidenceExtractorImpl();
    }

    @Bean
    public IngestObservationUseCase.ThreatDetectionPipeline threatDetectionPipeline(
            IngestObservationUseCase.ThreatEventRepository threatEventRepository,
            IngestObservationUseCase.EvidenceStore evidenceStore,
            IngestObservationUseCase.ThreatDetectionStrategyRegistry strategyRegistry,
            IngestObservationUseCase.EventPublisher eventPublisher
    ) {
        return new ThreatDetectionPipelineImpl(
                threatEventRepository,
                evidenceStore,
                strategyRegistry,
                eventPublisher
        );
    }

    @Bean
    public IngestObservationUseCase.EvidenceStore evidenceStore() {
        return new InMemoryEvidenceStore();
    }

    @Bean
    public IngestObservationUseCase.ThreatDetectionStrategyRegistry strategyRegistry() {
        return new DefaultThreatDetectionStrategyRegistry();
    }

    @Bean
    public IngestObservationUseCase.EventPublisher eventPublisher() {
        return new LoggingEventPublisher();
    }

    // Risk Assessment beans
    @Bean
    public AssessThreatRiskUseCase assessThreatRiskUseCase(
            AssessThreatRiskUseCase.AssetRepository assetRepository,
            AssessThreatRiskUseCase.RiskScoreRepository riskScoreRepository,
            RiskScoringStrategy riskScoringStrategy,
            AssessThreatRiskUseCase.EventPublisher eventPublisher
    ) {
        return new AssessThreatRiskUseCase(assetRepository, riskScoreRepository, riskScoringStrategy, eventPublisher);
    }

    @Bean
    public RiskScoringStrategy riskScoringStrategy() {
        return new DefaultRiskScoringStrategy();
    }

    @Bean
    public AssessThreatRiskUseCase.EventPublisher riskEventPublisher() {
        return new LoggingRiskEventPublisher();
    }

    // Incident Management beans
    @Bean
    public CreateIncidentFromRiskUseCase createIncidentFromRiskUseCase(
            CreateIncidentFromRiskUseCase.IncidentRepository incidentRepository,
            CreateIncidentFromRiskUseCase.AssetRepository assetRepository,
            CreateIncidentFromRiskUseCase.ThreatEventRepository threatEventRepository,
            IncidentTriggerPolicy incidentTriggerPolicy,
            CreateIncidentFromRiskUseCase.EventPublisher eventPublisher,
            CreateIncidentFromRiskUseCase.RealTimeEventService realTimeEventService
    ) {
        return new CreateIncidentFromRiskUseCase(
                incidentRepository,
                assetRepository,
                threatEventRepository,
                incidentTriggerPolicy,
                eventPublisher,
                realTimeEventService
        );
    }

    @Bean
    public IncidentTriggerPolicy incidentTriggerPolicy() {
        return new DefaultIncidentTriggerPolicy();
    }

    @Bean
    public CreateIncidentFromRiskUseCase.EventPublisher incidentEventPublisher() {
        return new LoggingIncidentEventPublisher();
    }

    @Bean
    public CreateIncidentFromRiskUseCase.RealTimeEventService realTimeEventServiceAdapter(
            RealTimeEventService realTimeEventService
    ) {
        return new RealTimeEventServiceAdapter(realTimeEventService);
    }

    // Impact Assessment beans
    @Bean
    public AssessIncidentImpactUseCase assessIncidentImpactUseCase(
            AssessIncidentImpactUseCase.IncidentRepository incidentRepository,
            AssessIncidentImpactUseCase.AssetRepository assetRepository,
            AssessIncidentImpactUseCase.GridNodeRepository gridNodeRepository,
            AssessIncidentImpactUseCase.GridEdgeRepository gridEdgeRepository,
            AssessIncidentImpactUseCase.ImpactResultRepository impactResultRepository,
            ImpactAssessmentStrategy impactAssessmentStrategy,
            AssessIncidentImpactUseCase.EventPublisher eventPublisher
    ) {
        return new AssessIncidentImpactUseCase(
                incidentRepository,
                assetRepository,
                gridNodeRepository,
                gridEdgeRepository,
                impactResultRepository,
                impactAssessmentStrategy,
                eventPublisher
        );
    }

    @Bean
    public ImpactAssessmentStrategy impactAssessmentStrategy() {
        return new DefaultImpactAssessmentStrategy();
    }

    @Bean
    public AssessIncidentImpactUseCase.EventPublisher impactEventPublisher() {
        return new LoggingImpactEventPublisher();
    }

    // Simulation beans
    @Bean
    public CreateSimulationFromImpactUseCase createSimulationFromImpactUseCase(
            CreateSimulationFromImpactUseCase.IncidentRepository incidentRepository,
            CreateSimulationFromImpactUseCase.ImpactResultRepository impactResultRepository,
            CreateSimulationFromImpactUseCase.SimulationRepository simulationRepository,
            CreateSimulationFromImpactUseCase.EventPublisher eventPublisher
    ) {
        return new CreateSimulationFromImpactUseCase(
                incidentRepository,
                impactResultRepository,
                simulationRepository,
                eventPublisher
        );
    }

    @Bean
    public ExecuteSimulationUseCase executeSimulationUseCase(
            ExecuteSimulationUseCase.SimulationRepository simulationRepository,
            SimulationStrategy simulationStrategy,
            ExecuteSimulationUseCase.EventPublisher eventPublisher
    ) {
        return new ExecuteSimulationUseCase(
                simulationRepository,
                simulationStrategy,
                eventPublisher
        );
    }

    @Bean
    public SimulationStrategy simulationStrategy() {
        return new DefaultSimulationStrategy();
    }

    @Bean
    public ExecuteSimulationUseCase.EventPublisher simulationEventPublisher() {
        return new LoggingSimulationEventPublisher();
    }

    @Bean
    public CreateSimulationFromImpactUseCase.EventPublisher simulationCreateEventPublisher() {
        return new LoggingSimulationCreateEventPublisher();
    }

    // Response Planning beans
    @Bean
    public GenerateResponseOptionsUseCase generateResponseOptionsUseCase(
            GenerateResponseOptionsUseCase.SimulationRepository simulationRepository,
            GenerateResponseOptionsUseCase.IncidentRepository incidentRepository,
            GenerateResponseOptionsUseCase.ResponsePlanRepository responsePlanRepository,
            GenerateResponseOptionsUseCase.AssetRepository assetRepository,
            ResponsePlanningStrategy responsePlanningStrategy,
            GenerateResponseOptionsUseCase.EventPublisher eventPublisher
    ) {
        return new GenerateResponseOptionsUseCase(
                simulationRepository,
                incidentRepository,
                responsePlanRepository,
                assetRepository,
                responsePlanningStrategy,
                eventPublisher
        );
    }

    @Bean
    public ResponsePlanningStrategy responsePlanningStrategy() {
        return new DefaultResponsePlanningStrategy();
    }

    @Bean
    public GenerateResponseOptionsUseCase.EventPublisher responseEventPublisher() {
        return new LoggingResponseEventPublisher();
    }

    // Response Action Mitigation beans
    @Bean
    public AssignResponseActionUseCase assignResponseActionUseCase(
            AssignResponseActionUseCase.ResponsePlanRepository responsePlanRepository,
            AssignResponseActionUseCase.UserRepository userRepository,
            AssignResponseActionUseCase.EventPublisher eventPublisher
    ) {
        return new AssignResponseActionUseCase(responsePlanRepository, userRepository, eventPublisher);
    }

    @Bean
    public StartResponseActionUseCase startResponseActionUseCase(
            StartResponseActionUseCase.ResponsePlanRepository responsePlanRepository,
            StartResponseActionUseCase.EventPublisher eventPublisher
    ) {
        return new StartResponseActionUseCase(responsePlanRepository, eventPublisher);
    }

    @Bean
    public CompleteResponseActionUseCase completeResponseActionUseCase(
            CompleteResponseActionUseCase.ResponsePlanRepository responsePlanRepository,
            CompleteResponseActionUseCase.EventPublisher eventPublisher
    ) {
        return new CompleteResponseActionUseCase(responsePlanRepository, eventPublisher);
    }

    @Bean
    public FailResponseActionUseCase failResponseActionUseCase(
            FailResponseActionUseCase.ResponsePlanRepository responsePlanRepository,
            FailResponseActionUseCase.EventPublisher eventPublisher
    ) {
        return new FailResponseActionUseCase(responsePlanRepository, eventPublisher);
    }

    @Bean
    public BlockResponseActionUseCase blockResponseActionUseCase(
            BlockResponseActionUseCase.ResponsePlanRepository responsePlanRepository,
            BlockResponseActionUseCase.EventPublisher eventPublisher
    ) {
        return new BlockResponseActionUseCase(responsePlanRepository, eventPublisher);
    }

    @Bean
    public AssignResponseActionUseCase.EventPublisher actionAssignEventPublisher() {
        return new LoggingActionEventPublisher();
    }

    @Bean
    public StartResponseActionUseCase.EventPublisher actionStartEventPublisher() {
        return new LoggingActionEventPublisher();
    }

    @Bean
    public CompleteResponseActionUseCase.EventPublisher actionCompleteEventPublisher() {
        return new LoggingActionEventPublisher();
    }

    @Bean
    public FailResponseActionUseCase.EventPublisher actionFailEventPublisher() {
        return new LoggingActionEventPublisher();
    }

    @Bean
    public BlockResponseActionUseCase.EventPublisher actionBlockEventPublisher() {
        return new LoggingActionEventPublisher();
    }

    // Operator Decision beans
    @Bean
    public DecideResponsePlanUseCase decideResponsePlanUseCase(
            DecideResponsePlanUseCase.ResponsePlanRepository responsePlanRepository,
            DecideResponsePlanUseCase.IncidentRepository incidentRepository,
            DecideResponsePlanUseCase.AuditLogRepository auditLogRepository,
            DecideResponsePlanUseCase.EventPublisher eventPublisher
    ) {
        return new DecideResponsePlanUseCase(
                responsePlanRepository,
                incidentRepository,
                auditLogRepository,
                eventPublisher
        );
    }

    @Bean
    public DecideResponsePlanUseCase.EventPublisher decisionEventPublisher() {
        return new LoggingDecisionEventPublisher();
    }

    /**
     * Simple logging event publisher for development (threats).
     */
    public static class LoggingEventPublisher implements IngestObservationUseCase.EventPublisher {
        @Override
        public void publishThreatDetected(com.gridsentinel.detection.ThreatEvent threat) {
            System.out.println("THREAT DETECTED: " + threat.getType() + " - " + threat.getSeverity() + " - " + threat.getDescription());
        }
    }

    /**
     * Simple logging event publisher for development (risk scores).
     */
    public static class LoggingRiskEventPublisher implements AssessThreatRiskUseCase.EventPublisher {
        @Override
        public void publishRiskScoreUpdated(RiskScore riskScore) {
            System.out.println("RISK SCORE UPDATED: Asset=" + riskScore.getAssetId() + " Score=" + riskScore.getScore() + " Band=" + riskScore.getBand());
        }
    }

    /**
     * Simple logging event publisher for development (incidents).
     */
    public static class LoggingIncidentEventPublisher implements CreateIncidentFromRiskUseCase.EventPublisher {
        @Override
        public void publishIncidentCreated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation) {
            System.out.println("INCIDENT CREATED: " + incident.getIncidentNumber() + " - " + incident.getSeverity() + " - " + evaluation.rationale());
        }

        @Override
        public void publishIncidentEscalated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation) {
            System.out.println("INCIDENT ESCALATED: " + incident.getIncidentNumber() + " - " + evaluation.rationale());
        }

        @Override
        public void publishIncidentUpdated(Incident incident, RiskScore riskScore, IncidentTriggerPolicy.IncidentEvaluation evaluation) {
            System.out.println("INCIDENT UPDATED: " + incident.getIncidentNumber() + " - " + evaluation.rationale());
        }

        @Override
        public void publishPolicyEvaluation(CommonKernel.ThreatEventId threatEventId, CommonKernel.RiskScoreId riskScoreId, IncidentTriggerPolicy.IncidentEvaluation evaluation) {
            System.out.println("POLICY EVALUATION: threat=" + threatEventId + " risk=" + riskScoreId + " decision=" + evaluation.decision() + " rationale=" + evaluation.rationale());
        }
    }

    /**
     * Simple logging event publisher for development (impact assessments).
     */
    public static class LoggingImpactEventPublisher implements AssessIncidentImpactUseCase.EventPublisher {
        @Override
        public void publishImpactAssessed(Incident incident, ImpactResult impactResult) {
            System.out.println("IMPACT ASSESSED: Incident=" + incident.getIncidentNumber() + " Assets=" + impactResult.getAffectedAssetIds().size() + " Nodes=" + impactResult.getAffectedNodeIds().size());
        }
    }

    /**
     * Simple logging event publisher for development (simulation creation).
     */
    public static class LoggingSimulationCreateEventPublisher implements CreateSimulationFromImpactUseCase.EventPublisher {
        @Override
        public void publishSimulationCreated(Simulation simulation, ImpactResult impactResult, Incident incident, java.util.Map<String, String> assumptions) {
            System.out.println("SIMULATION CREATED: " + simulation.getName() + " - " + simulation.getScenarioId() + " for incident " + incident.getIncidentNumber());
        }
    }

    /**
     * Simple logging event publisher for development (simulation execution).
     */
    public static class LoggingSimulationEventPublisher implements ExecuteSimulationUseCase.EventPublisher {
        @Override
        public void publishSimulationCreated(Simulation simulation) {
            System.out.println("SIMULATION CREATED EVENT: " + simulation.getName());
        }

        @Override
        public void publishSimulationRunning(Simulation simulation) {
            System.out.println("SIMULATION RUNNING: " + simulation.getName() + " - " + simulation.getScenarioId());
        }

        @Override
        public void publishSimulationCompleted(Simulation simulation) {
            System.out.println("SIMULATION COMPLETED: " + simulation.getName() + " - Result: " + (simulation.getResult() != null ? "available" : "none"));
        }

        @Override
        public void publishSimulationFailed(Simulation simulation) {
            System.out.println("SIMULATION FAILED: " + simulation.getName() + " - " + simulation.getErrorMessage());
        }

        @Override
        public void publishSimulationCancelled(Simulation simulation) {
            System.out.println("SIMULATION CANCELLED: " + simulation.getName());
        }
    }

    /**
     * Simple logging event publisher for development (response planning).
     */
    public static class LoggingResponseEventPublisher implements GenerateResponseOptionsUseCase.EventPublisher {
        @Override
        public void publishResponsePlanCreated(ResponsePlan plan, Simulation simulation, Incident incident) {
            System.out.println("RESPONSE PLAN CREATED: " + plan.getName() + " for simulation " + simulation.getScenarioId());
        }

        @Override
        public void publishResponsePlanUpdated(ResponsePlan plan, ResponsePlan.PlanStatus previousStatus, String actorId, String action) {
            System.out.println("RESPONSE PLAN UPDATED: " + plan.getName() + " from " + previousStatus + " to " + plan.getStatus() + " by " + actorId + " (" + action + ")");
        }
    }

    /**
     * Simple logging event publisher for development (operator decisions).
     */
    public static class LoggingDecisionEventPublisher implements DecideResponsePlanUseCase.EventPublisher {
        @Override
        public void publishResponsePlanUpdated(ResponsePlan plan, ResponsePlan.PlanStatus previousStatus, String actorId, String action) {
            System.out.println("RESPONSE PLAN DECISION: " + plan.getName() + " from " + previousStatus + " to " + plan.getStatus() + " by " + actorId + " (" + action + ")");
        }
    }

    /**
     * Adapter to bridge infrastructure RealTimeEventService to application RealTimeEventService.
     */
    public static class RealTimeEventServiceAdapter implements CreateIncidentFromRiskUseCase.RealTimeEventService {
        private final RealTimeEventService delegate;

        public RealTimeEventServiceAdapter(RealTimeEventService delegate) {
            this.delegate = delegate;
        }

        @Override
        public void publishIncidentCreated(Incident incident) {
            delegate.publishIncidentCreated(incident);
        }

        @Override
        public void publishIncidentStateChanged(Incident incident, com.gridsentinel.common.IncidentState previousState, String actorId, String reason) {
            delegate.publishIncidentStateChanged(incident, previousState, actorId, reason);
        }
    }

    /**
     * Simple logging event publisher for development (response actions).
     */
    public static class LoggingActionEventPublisher implements
            AssignResponseActionUseCase.EventPublisher,
            StartResponseActionUseCase.EventPublisher,
            CompleteResponseActionUseCase.EventPublisher,
            FailResponseActionUseCase.EventPublisher,
            BlockResponseActionUseCase.EventPublisher {
        @Override
        public void publishResponsePlanUpdated(ResponsePlan plan, ResponsePlan.PlanStatus previousStatus, String actorId, String action) {
            System.out.println("RESPONSE ACTION " + action.toUpperCase() + ": " + plan.getName() + " by " + actorId);
        }
    }
}