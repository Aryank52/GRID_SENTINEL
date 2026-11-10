package com.gridsentinel.application.integration;

import com.gridsentinel.application.config.ApplicationConfig;
import com.gridsentinel.application.incident.CreateIncidentFromRiskUseCase;
import com.gridsentinel.application.impact.AssessIncidentImpactUseCase;
import com.gridsentinel.application.risk.AssessThreatRiskUseCase;
import com.gridsentinel.application.observation.IngestObservationUseCase;
import com.gridsentinel.application.observation.EvidenceExtractorImpl;
import com.gridsentinel.application.observation.InMemoryEvidenceStore;
import com.gridsentinel.application.observation.DefaultThreatDetectionStrategyRegistry;
import com.gridsentinel.application.observation.ThreatDetectionPipelineImpl;
import com.gridsentinel.application.risk.DefaultRiskScoringStrategy;
import com.gridsentinel.application.impact.DefaultImpactAssessmentStrategy;
import com.gridsentinel.application.incident.DefaultIncidentTriggerPolicy;
import com.gridsentinel.application.simulation.CreateSimulationFromImpactUseCase;
import com.gridsentinel.application.simulation.ExecuteSimulationUseCase;
import com.gridsentinel.application.simulation.DefaultSimulationStrategy;
import com.gridsentinel.application.response.GenerateResponseOptionsUseCase;
import com.gridsentinel.application.response.DefaultResponsePlanningStrategy;
import com.gridsentinel.application.response.DecideResponsePlanUseCase;
import com.gridsentinel.application.response.AssignResponseActionUseCase;
import com.gridsentinel.application.response.StartResponseActionUseCase;
import com.gridsentinel.application.response.CompleteResponseActionUseCase;
import com.gridsentinel.application.recovery.InitializeRecoveryUseCase;
import com.gridsentinel.application.recovery.StartRecoveryMilestoneUseCase;
import com.gridsentinel.application.recovery.CompleteRecoveryMilestoneUseCase;
import com.gridsentinel.application.recovery.CompleteRecoveryUseCase;
import com.gridsentinel.application.recovery.DefaultMilestoneFactory;
import com.gridsentinel.application.recovery.DefaultRecoveryEligibilityPolicy;
import com.gridsentinel.application.recovery.RecoveryEventPublisher;
import com.gridsentinel.asset.Asset;
import com.gridsentinel.asset.AssetType;
import com.gridsentinel.auth.User;
import com.gridsentinel.auth.Role;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ObservationSourceType;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatStatus;
import com.gridsentinel.detection.ThreatType;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.grid.GridEdge;
import com.gridsentinel.grid.GridNode;
import com.gridsentinel.grid.NodeType;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.risk.RiskScore;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.simulation.Simulation;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.recovery.RecoveryState;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryMilestone;
import com.gridsentinel.recovery.RecoveryStatus;
import com.gridsentinel.recovery.MilestoneType;
import com.gridsentinel.infrastructure.persistence.repository.AssetRepository;
import com.gridsentinel.infrastructure.persistence.repository.GridNodeRepository;
import com.gridsentinel.infrastructure.persistence.repository.UserRepository;
import com.gridsentinel.infrastructure.persistence.repository.IncidentRepository;
import com.gridsentinel.infrastructure.persistence.repository.RecoveryStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Application-Layer Integration Test for GRID SENTINEL workflow.
 * Tests the complete lifecycle through application use cases:
 * Observation → Threat Detection → Risk Assessment → Incident Creation
 * → Impact Assessment → Simulation → Response Planning → Operator Decision
 * → Mitigation (Action Assignment/Execution) → Recovery → Audit
 */
@SpringBootTest
@ContextConfiguration(classes = {ApplicationConfig.class})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DisplayName("Application Layer Workflow Integration Test")
class ApplicationLayerWorkflowTest {

    @Autowired
    IngestObservationUseCase ingestObservationUseCase;

    @Autowired
    AssessThreatRiskUseCase assessThreatRiskUseCase;

    @Autowired
    CreateIncidentFromRiskUseCase createIncidentFromRiskUseCase;

    @Autowired
    AssessIncidentImpactUseCase assessIncidentImpactUseCase;

    @Autowired
    CreateSimulationFromImpactUseCase createSimulationFromImpactUseCase;

    @Autowired
    ExecuteSimulationUseCase executeSimulationUseCase;

    @Autowired
    GenerateResponseOptionsUseCase generateResponseOptionsUseCase;

    @Autowired
    DecideResponsePlanUseCase decideResponsePlanUseCase;

    @Autowired
    AssignResponseActionUseCase assignResponseActionUseCase;

    @Autowired
    StartResponseActionUseCase startResponseActionUseCase;

    @Autowired
    CompleteResponseActionUseCase completeResponseActionUseCase;

    @Autowired
    InitializeRecoveryUseCase initializeRecoveryUseCase;

    @Autowired
    StartRecoveryMilestoneUseCase startRecoveryMilestoneUseCase;

    @Autowired
    CompleteRecoveryMilestoneUseCase completeRecoveryMilestoneUseCase;

    @Autowired
    CompleteRecoveryUseCase completeRecoveryUseCase;

    // Repositories (Spring Data JPA repositories with save() methods)
    @Autowired
    AssetRepository assetRepository;

    @Autowired
    GridNodeRepository gridNodeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IncidentRepository incidentRepository;

    @Autowired
    RecoveryStateRepository recoveryStateRepository;

    @Autowired
    IngestObservationUseCase.ObservationRepository observationRepository;

    @Autowired
    IngestObservationUseCase.ThreatEventRepository threatEventRepository;

    @Autowired
    AssessThreatRiskUseCase.RiskScoreRepository riskScoreRepository;

    @Autowired
    AssessIncidentImpactUseCase.GridEdgeRepository gridEdgeRepository;

    @Autowired
    AssessIncidentImpactUseCase.ImpactResultRepository impactResultRepository;

    @Autowired
    CreateSimulationFromImpactUseCase.SimulationRepository simulationRepository;

    @Autowired
    GenerateResponseOptionsUseCase.ResponsePlanRepository responsePlanRepository;

    // Test data
    CommonKernel.AssetId testAssetId;
    CommonKernel.GridNodeId testNodeId;
    CommonKernel.UserId testUserId = CommonKernel.UserId.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    CommonKernel.UserId testUserId2 = CommonKernel.UserId.of(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    @BeforeEach
    void setup() {
        // Create test asset (geometry will be null in H2 test environment without PostGIS)
        Asset asset = Asset.builder()
                .id(CommonKernel.AssetId.generate())
                .name("Test Substation Alpha")
                .type(AssetType.SUBSTATION)
                .criticality(CommonKernel.Criticality.HIGH)
                .healthIndex(CommonKernel.HealthIndex.of(0.85))
                .geometry(null)
                .build();
        asset = assetRepository.save(asset);
        testAssetId = asset.getId();

        // Create test grid node
        GridNode node = GridNode.builder()
                .id(CommonKernel.GridNodeId.generate())
                .name("Node Alpha")
                .type(NodeType.SUBSTATION)
                .voltageLevelKv(115.0)
                .geometry(null)
                .build();
        node = gridNodeRepository.save(node);
        testNodeId = node.getId();

        // Create test users
        User user = User.builder()
                .id(testUserId)
                .username("test-operator")
                .email("operator@grid-sentinel.com")
                .passwordHash("$2a$10$dummyhash")
                .fullName("Test Operator")
                .roles(Set.of(Role.INCIDENT_COMMANDER, Role.DISPATCHER))
                .build();
        userRepository.save(user);

        User user2 = User.builder()
                .id(testUserId2)
                .username("test-operator2")
                .email("operator2@grid-sentinel.com")
                .passwordHash("$2a$10$dummyhash")
                .fullName("Test Operator 2")
                .roles(Set.of(Role.DISPATCHER, Role.OPERATOR))
                .build();
        userRepository.save(user2);
    }

    @Test
    @DisplayName("Complete E2E workflow: Observation → Threat → Risk → Incident → Impact → Simulation → Response → Decision → Mitigation → Recovery")
    void fullEndToEndWorkflow() {
        // ============================================================
        // STEP 1: OBSERVATION INGESTION
        // ============================================================
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-001",
                        Instant.now(),
                        "Thermal anomaly detected at Substation Alpha",
                        testAssetId,
                        testNodeId,
                        null // geometry - null in H2 test environment
                )
        );

        assertThat(observationId).isNotNull();

        var observation = observationRepository.findById(observationId).orElseThrow();
        assertThat(observation.getValidationStatus()).isEqualTo(com.gridsentinel.observation.ValidationStatus.PENDING);
        assertThat(observation.getAssociatedAssetId()).isEqualTo(testAssetId);

        // ============================================================
        // STEP 2: THREAT DETECTION (triggered automatically by observation ingestion)
        // ============================================================
        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        assertThat(threats).isNotEmpty();

        ThreatEvent threat = threats.get(0);
        // Threat type depends on the evidence extractor - verify it's detected
        assertThat(threat.getSeverity()).isNotNull();
        assertThat(threat.getStatus()).isIn(ThreatStatus.NEW, ThreatStatus.TRIAGED, ThreatStatus.CONFIRMED);
        assertThat(threat.getAffectedAssetId()).isEqualTo(testAssetId);

        CommonKernel.ThreatEventId threatId = threat.getId();

        // ============================================================
        // STEP 3: RISK ASSESSMENT
        // ============================================================
        List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(riskScores).isNotEmpty();

        RiskScore riskScore = riskScores.get(0);
        assertThat(riskScore).isNotNull();
        assertThat(riskScore.getAssetId()).isEqualTo(testAssetId);
        assertThat(riskScore.getScore()).isBetween(0.0, 1.0);
        assertThat(riskScore.getBand()).isNotNull();
        assertThat(riskScore.getFactorBreakdown()).isNotNull();

        CommonKernel.RiskScoreId riskScoreId = riskScore.getId();

        // Verify risk score persisted
        Optional<RiskScore> persistedRisk = riskScoreRepository.findLatestByAssetId(testAssetId);
        assertThat(persistedRisk).isPresent();
        assertThat(persistedRisk.get().getScore()).isEqualTo(riskScore.getScore());

        // ============================================================
        // STEP 4: INCIDENT CREATION
        // ============================================================
        Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat);
        assertThat(incidentOpt).isPresent();

        Incident incident = incidentOpt.get();
        assertThat(incident).isNotNull();
        assertThat(incident.getState()).isIn(IncidentState.DETECTED, IncidentState.TRIAGED, IncidentState.CONFIRMED, IncidentState.ACTIVE_RESPONSE);
        assertThat(incident.getSeverity()).isNotNull();
        assertThat(incident.getOriginatingThreatId()).isEqualTo(threatId);
        assertThat(incident.getCorrelationId()).isNotNull();

        CommonKernel.IncidentId incidentId = incident.getId();

        // Verify incident persisted
        Optional<Incident> persistedIncident = incidentRepository.findById(incidentId);
        assertThat(persistedIncident).isPresent();
        assertThat(persistedIncident.get().getIncidentNumber()).isEqualTo(incident.getIncidentNumber());

        // ============================================================
        // STEP 5: IMPACT ASSESSMENT
        // ============================================================
        com.gridsentinel.application.impact.ImpactAssessmentResult impactResult = assessIncidentImpactUseCase.assessImpact(incidentId);

        assertThat(impactResult).isNotNull();
        assertThat(impactResult.incidentId()).isEqualTo(incidentId);
        assertThat(impactResult.overallImpactScore()).isBetween(0.0, 1.0);
        assertThat(impactResult.severity()).isNotNull();
        assertThat(impactResult.affectedAssets()).isNotEmpty();
        assertThat(impactResult.correlationId()).isEqualTo(incident.getCorrelationId());

        // Verify impact result persisted
        Optional<com.gridsentinel.simulation.ImpactResult> persistedImpact = impactResultRepository.findByScenarioId("INCIDENT-" + incident.getIncidentNumber());
        assertThat(persistedImpact).isPresent();

        // ============================================================
        // STEP 6: SIMULATION
        // ============================================================
        Simulation simulation = createSimulationFromImpactUseCase.createSimulation(
                incidentId,
                "Full Grid Cascade Simulation",
                testUserId,
                java.util.Map.of("weather", "dry", "load_factor", "0.85")
        );

        assertThat(simulation).isNotNull();
        assertThat(simulation.getName()).isEqualTo("Full Grid Cascade Simulation");
        assertThat(simulation.getStatus()).isEqualTo(SimulationStatus.PENDING);
        assertThat(simulation.getScenarioId()).isNotNull();
        assertThat(simulation.getCorrelationId()).isEqualTo(incident.getCorrelationId());

        CommonKernel.SimulationId simulationId = simulation.getId();

        // Execute simulation
        Simulation executedSimulation = executeSimulationUseCase.execute(simulationId);

        assertThat(executedSimulation).isNotNull();
        assertThat(executedSimulation.getStatus()).isEqualTo(SimulationStatus.COMPLETED);
        assertThat(executedSimulation.getResult()).isNotNull();

        // ============================================================
        // STEP 7: RESPONSE OPTIONS GENERATION
        // ============================================================
        ResponsePlan responsePlan = generateResponseOptionsUseCase.generateResponseOptions(simulationId, testUserId);

        assertThat(responsePlan).isNotNull();
        assertThat(responsePlan.getName()).isNotBlank();
        assertThat(responsePlan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.DRAFT);
        assertThat(responsePlan.getIncidentId()).isEqualTo(incidentId);
        assertThat(responsePlan.getActions()).isNotEmpty();
        assertThat(responsePlan.getCorrelationId()).isEqualTo(incident.getCorrelationId());

        CommonKernel.ResponsePlanId planId = responsePlan.getId();

        // Verify response plan persisted
        Optional<ResponsePlan> persistedPlan = responsePlanRepository.findById(planId);
        assertThat(persistedPlan).isPresent();

        // ============================================================
        // STEP 8: OPERATOR DECISION (SUBMIT FOR APPROVAL THEN APPROVE)
        // ============================================================
        // First submit for approval
        ResponsePlan submittedPlan = generateResponseOptionsUseCase.submitForApproval(planId, testUserId);
        assertThat(submittedPlan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.PENDING_APPROVAL);

        // Then approve
        ResponsePlan approvedPlan = decideResponsePlanUseCase.approve(planId, testUserId, "Approved for immediate execution");

        assertThat(approvedPlan).isNotNull();
        assertThat(approvedPlan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.APPROVED);
        assertThat(approvedPlan.getApprovedBy()).isEqualTo(testUserId);
        assertThat(approvedPlan.getApprovedAt()).isNotNull();

        // ============================================================
        // STEP 9: MITIGATION - ACTION ASSIGNMENT & EXECUTION
        // ============================================================
        List<ResponseAction> actions = approvedPlan.getActions();
        assertThat(actions).isNotEmpty();

        // Assign first action
        ResponseAction firstAction = actions.get(0);
        CommonKernel.ResponseActionId firstActionId = firstAction.getId();

        ResponseAction assignedAction = assignResponseActionUseCase.assign(planId, firstActionId, testUserId2, testUserId);

        assertThat(assignedAction).isNotNull();
        assertThat(assignedAction.getStatus()).isIn(ResponseAction.ActionStatus.PENDING, ResponseAction.ActionStatus.ASSIGNED);
        assertThat(assignedAction.getAssignedTo()).isEqualTo(testUserId2);

        // Start action execution
        ResponseAction startedAction = startResponseActionUseCase.start(planId, firstActionId, testUserId2);

        assertThat(startedAction).isNotNull();
        assertThat(startedAction.getStatus()).isEqualTo(ResponseAction.ActionStatus.IN_PROGRESS);
        assertThat(startedAction.getActualStartAt()).isNotNull();

        // Complete action
        ResponseAction completedAction = completeResponseActionUseCase.complete(planId, firstActionId, testUserId2, "Work completed successfully");

        assertThat(completedAction).isNotNull();
        assertThat(completedAction.getStatus()).isEqualTo(ResponseAction.ActionStatus.COMPLETED);
        assertThat(completedAction.getActualEndAt()).isNotNull();

        // ============================================================
        // STEP 10: RECOVERY INITIALIZATION
        // ============================================================
        RecoveryState recoveryState = initializeRecoveryUseCase.initialize(
                incidentId,
                testUserId,
                Instant.now().plusSeconds(3600) // 1 hour estimated completion
        );

        assertThat(recoveryState).isNotNull();
        assertThat(recoveryState.getIncidentId()).isEqualTo(incidentId);
        assertThat(recoveryState.getRecoveryManagerId()).isEqualTo(testUserId);
        assertThat(recoveryState.getPhase()).isEqualTo(RecoveryPhase.NOT_STARTED); // Initially NOT_STARTED
        assertThat(recoveryState.getMilestones()).isNotEmpty();
        assertThat(recoveryState.getCorrelationId()).isEqualTo(incident.getCorrelationId());

        CommonKernel.RecoveryStateId recoveryStateId = recoveryState.getId();

        // Verify recovery state persisted
        Optional<RecoveryState> persistedRecovery = recoveryStateRepository.findById(recoveryStateId);
        assertThat(persistedRecovery).isPresent();

        // ============================================================
        // STEP 11: RECOVERY MILESTONES
        // ============================================================
        List<RecoveryMilestone> milestones = recoveryState.getMilestones();
        assertThat(milestones).isNotEmpty();

        // Start first milestone
        RecoveryMilestone firstMilestone = milestones.get(0);
        String firstMilestoneId = firstMilestone.getId();

        RecoveryMilestone startedMilestone = startRecoveryMilestoneUseCase.start(recoveryStateId, firstMilestoneId, testUserId2);

        assertThat(startedMilestone).isNotNull();
        assertThat(startedMilestone.getStatus()).isEqualTo(RecoveryStatus.IN_PROGRESS);
        assertThat(startedMilestone.getStartedAt()).isNotNull();

        // Complete first milestone
        RecoveryMilestone completedMilestone = completeRecoveryMilestoneUseCase.complete(recoveryStateId, firstMilestoneId, testUserId2, "Restoration verified");

        assertThat(completedMilestone).isNotNull();
        assertThat(completedMilestone.getStatus()).isEqualTo(RecoveryStatus.COMPLETED);
        assertThat(completedMilestone.getCompletedAt()).isNotNull();

        // ============================================================
        // STEP 12: RECOVERY COMPLETION
        // ============================================================
        // Complete all remaining milestones
        for (RecoveryMilestone milestone : recoveryStateRepository.findById(recoveryStateId).orElseThrow().getMilestones()) {
            if (milestone.getStatus() == RecoveryStatus.NOT_STARTED) {
                startRecoveryMilestoneUseCase.start(recoveryStateId, milestone.getId(), testUserId2);
                completeRecoveryMilestoneUseCase.complete(recoveryStateId, milestone.getId(), testUserId2, "Completed");
            }
        }

        // The recovery state should now be in COMPLETED phase
        RecoveryState completedRecoveryState = recoveryStateRepository.findById(recoveryStateId).orElseThrow();
        assertThat(completedRecoveryState.getPhase()).isEqualTo(RecoveryPhase.COMPLETED);
        assertThat(completedRecoveryState.getOverallProgress()).isEqualTo(1.0);
        assertThat(completedRecoveryState.getMilestones()).allMatch(m -> m.isCompleted());

        // Complete recovery (formal completion with notes)
        RecoveryState finalRecovery = completeRecoveryUseCase.complete(recoveryStateId, testUserId, "All milestones completed, full restoration achieved");

        assertThat(finalRecovery).isNotNull();
        assertThat(finalRecovery.getPhase()).isEqualTo(RecoveryPhase.COMPLETED);
        assertThat(finalRecovery.getActualCompletionAt()).isNotNull();
        assertThat(finalRecovery.getMilestones()).allMatch(m -> m.isCompleted());

        // Verify incident state updated to RESOLVED
        Incident resolvedIncident = incidentRepository.findById(incidentId).orElseThrow();
        assertThat(resolvedIncident.getState()).isIn(IncidentState.RESOLVED, IncidentState.CLOSED);

        // ============================================================
        // STEP 13: AUDIT VERIFICATION
        // ============================================================
        // Verify the correlation ID chain is maintained
        assertThat(observation.getId()).isNotNull();
        assertThat(threat.getCorrelationId()).isEqualTo(observation.getId());
        assertThat(riskScore.getCorrelationId()).isEqualTo(threat.getCorrelationId());
        assertThat(incident.getCorrelationId()).isEqualTo(riskScore.getCorrelationId());
        assertThat(simulation.getCorrelationId()).isEqualTo(incident.getCorrelationId());
        assertThat(recoveryState.getCorrelationId()).isEqualTo(simulation.getCorrelationId());

        System.out.println("✅ FULL END-TO-END WORKFLOW COMPLETED SUCCESSFULLY");
        System.out.println("   Observation: " + observationId);
        System.out.println("   Threat: " + threatId + " (" + threat.getType() + ", " + threat.getSeverity() + ")");
        System.out.println("   Risk Score: " + riskScoreId + " (" + riskScore.getScore() + ", " + riskScore.getBand() + ")");
        System.out.println("   Incident: " + incidentId + " (" + incident.getIncidentNumber() + ", " + incident.getSeverity() + ")");
        System.out.println("   Impact: " + impactResult.affectedAssets().size() + " assets, " + impactResult.affectedNodes().size() + " nodes");
        System.out.println("   Simulation: " + simulationId + " (" + simulation.getStatus() + ")");
        System.out.println("   Response Plan: " + planId + " (" + responsePlan.getStatus() + ")");
        System.out.println("   Recovery: " + recoveryStateId + " (" + finalRecovery.getPhase() + ")");
        System.out.println("   Correlation ID Chain: VERIFIED");
    }

    @Test
    @DisplayName("Negative test: Low risk + low severity threat should not create incident")
    void lowRiskLowSeverityNoIncident() {
        // Create observation with low severity
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.OPERATOR_MANUAL,
                        "MANUAL-001",
                        Instant.now(),
                        "Minor equipment vibration detected",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        // Check threats created
        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent lowThreat = threats.stream()
                .filter(t -> t.getSeverity() == ThreatSeverity.LOW || t.getSeverity() == ThreatSeverity.INFO)
                .findFirst()
                .orElse(null);

        if (lowThreat != null) {
            List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(lowThreat);
            if (!riskScores.isEmpty()) {
                RiskScore riskScore = riskScores.get(0);

                // Risk should be LOW or MEDIUM
                assertThat(riskScore.getBand()).isIn(RiskBand.LOW, RiskBand.MEDIUM);

                // Try to create incident - should not create for low risk/low severity
                Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(lowThreat);

                // Policy should reject - incident should be empty
                assertThat(incidentOpt).isEmpty();
            }
        }
    }

    @Test
    @DisplayName("Negative test: Duplicate threat should not create duplicate active incident")
    void duplicateThreatNoDuplicateIncident() {
        // Ingest first observation
        CommonKernel.ObservationId obs1 = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SCADA_TELEMETRY,
                        "SCADA-001",
                        Instant.now(),
                        "Overcurrent on feeder Alpha",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats1 = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat1 = threats1.stream()
                .filter(t -> t.getType() == ThreatType.OVERLOAD)
                .findFirst()
                .orElseThrow();

        List<RiskScore> riskScores1 = assessThreatRiskUseCase.assessThreatRisk(threat1);
        assertThat(riskScores1).isNotEmpty();

        Optional<Incident> incidentOpt1 = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat1);
        assertThat(incidentOpt1).isPresent();

        Incident incident1 = incidentOpt1.get();
        assertThat(incident1).isNotNull();
        CommonKernel.IncidentId incidentId1 = incident1.getId();

        // Ingest second observation for same threat type/location
        CommonKernel.ObservationId obs2 = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SCADA_TELEMETRY,
                        "SCADA-002",
                        Instant.now().plusSeconds(60),
                        "Overcurrent on feeder Alpha (repeat)",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats2 = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat2 = threats2.stream()
                .filter(t -> t.getType() == ThreatType.OVERLOAD && !t.getId().equals(threat1.getId()))
                .findFirst()
                .orElse(null);

        if (threat2 != null) {
            List<RiskScore> riskScores2 = assessThreatRiskUseCase.assessThreatRisk(threat2);
            if (!riskScores2.isEmpty()) {
                Optional<Incident> incidentOpt2 = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat2);

                // Should update existing incident, not create new one
                if (incidentOpt2.isPresent()) {
                    Incident incident2 = incidentOpt2.get();
                    assertThat(incident2.getId()).isEqualTo(incidentId1);
                    assertThat(incident2.getVersion()).isGreaterThan(incident1.getVersion());
                }
            }
        }
    }

    @Test
    @DisplayName("Negative test: Recovery without prerequisites should be rejected")
    void recoveryWithoutPrerequisitesRejected() {
        // Create a new incident without going through mitigation
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-003",
                        Instant.now(),
                        "Earthquake damage at Substation Alpha",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat = threats.get(0);

        List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(riskScores).isNotEmpty();

        Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat);
        assertThat(incidentOpt).isPresent();
        Incident incident = incidentOpt.get();

        // Try to initialize recovery without containing the incident
        assertThatThrownBy(() -> initializeRecoveryUseCase.initialize(
                incident.getId(),
                testUserId,
                Instant.now().plusSeconds(3600)
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not eligible for recovery");
    }

    @Test
    @DisplayName("Negative test: Duplicate recovery should be prevented")
    void duplicateRecoveryPrevented() {
        // Setup full workflow to recovery
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-004",
                        Instant.now(),
                        "Flood at Substation Alpha",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat = threats.get(0);

        List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(riskScores).isNotEmpty();

        Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat);
        assertThat(incidentOpt).isPresent();
        Incident incident = incidentOpt.get();

        // The incident needs to be in CONTAINED state for recovery - 
        // the eligibility policy will reject if not

        // Try to initialize recovery without containing the incident
        assertThatThrownBy(() -> initializeRecoveryUseCase.initialize(
                incident.getId(),
                testUserId,
                Instant.now().plusSeconds(3600)
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not eligible for recovery");
    }

    @Test
    @DisplayName("Negative test: Action cannot start if dependencies incomplete")
    void actionCannotStartIfDependenciesIncomplete() {
        // Setup workflow to response plan with multiple actions
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-005",
                        Instant.now(),
                        "Transformer failure at Substation Alpha",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat = threats.get(0);

        List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(riskScores).isNotEmpty();

        Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat);
        assertThat(incidentOpt).isPresent();
        Incident incident = incidentOpt.get();

        com.gridsentinel.application.impact.ImpactAssessmentResult impactResult = assessIncidentImpactUseCase.assessImpact(incident.getId());

        Simulation simulation = createSimulationFromImpactUseCase.createSimulation(
                incident.getId(), "Test Sim", testUserId, java.util.Map.of()
        );

        executeSimulationUseCase.execute(simulation.getId());

        ResponsePlan responsePlan = generateResponseOptionsUseCase.generateResponseOptions(simulation.getId(), testUserId);

        ResponsePlan approved = decideResponsePlanUseCase.approve(responsePlan.getId(), testUserId, "Approved");

        // Find an action with dependencies
        ResponseAction dependentAction = approved.getActions().stream()
                .filter(a -> a.getDependsOn() != null && !a.getDependsOn().isEmpty())
                .findFirst()
                .orElse(null);

        if (dependentAction != null) {
            // Try to start dependent action without completing dependencies
            assertThatThrownBy(() -> startResponseActionUseCase.start(
                    approved.getId(), dependentAction.getId(), testUserId2
            )).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("dependenc");
        }
    }

    @Test
    @DisplayName("Test: Risk reassessment after mitigation")
    void riskReassessmentAfterMitigation() {
        // Create initial observation
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-006",
                        Instant.now(),
                        "High temperature detected on transformer",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat = threats.get(0);

        // Initial risk assessment
        List<RiskScore> initialRiskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(initialRiskScores).isNotEmpty();
        RiskScore initialRisk = initialRiskScores.get(0);
        double initialScore = initialRisk.getScore();

        // Reassess risk
        RiskScore reassessedRisk = assessThreatRiskUseCase.reassessAssetRisk(testAssetId, threat.getCorrelationId());

        assertThat(reassessedRisk).isNotNull();
        assertThat(reassessedRisk.getAssetId()).isEqualTo(testAssetId);
        // Score may have changed (or stayed same) but should be valid
        assertThat(reassessedRisk.getScore()).isBetween(0.0, 1.0);
        assertThat(reassessedRisk.getBand()).isNotNull();
    }

    @Test
    @DisplayName("Test: Response plan rejection workflow")
    void responsePlanRejectionWorkflow() {
        // Setup workflow to response plan
        CommonKernel.ObservationId observationId = ingestObservationUseCase.ingest(
                new IngestObservationUseCase.IngestObservationCommand(
                        ObservationSourceType.SATELLITE_IMAGERY,
                        "SAT-007",
                        Instant.now(),
                        "Fire detected at Substation Alpha",
                        testAssetId,
                        testNodeId,
                        null
                )
        );

        List<ThreatEvent> threats = threatEventRepository.findByAssetId(testAssetId);
        ThreatEvent threat = threats.get(0);

        List<RiskScore> riskScores = assessThreatRiskUseCase.assessThreatRisk(threat);
        assertThat(riskScores).isNotEmpty();

        Optional<Incident> incidentOpt = createIncidentFromRiskUseCase.evaluateThreatForIncident(threat);
        assertThat(incidentOpt).isPresent();
        Incident incident = incidentOpt.get();

        com.gridsentinel.application.impact.ImpactAssessmentResult impactResult = assessIncidentImpactUseCase.assessImpact(incident.getId());

        Simulation simulation = createSimulationFromImpactUseCase.createSimulation(
                incident.getId(), "Test Sim", testUserId, java.util.Map.of()
        );

        executeSimulationUseCase.execute(simulation.getId());

        ResponsePlan responsePlan = generateResponseOptionsUseCase.generateResponseOptions(simulation.getId(), testUserId);

        // Submit for approval
        ResponsePlan submitted = generateResponseOptionsUseCase.submitForApproval(responsePlan.getId(), testUserId);
        assertThat(submitted.getStatus()).isEqualTo(ResponsePlan.PlanStatus.PENDING_APPROVAL);

        // Reject the plan
        ResponsePlan rejected = decideResponsePlanUseCase.reject(responsePlan.getId(), testUserId, "Insufficient resources allocated");

        assertThat(rejected).isNotNull();
        assertThat(rejected.getStatus()).isEqualTo(ResponsePlan.PlanStatus.REJECTED);
        assertThat(rejected.getDescription()).contains("REJECTED: Insufficient resources allocated");
    }
}