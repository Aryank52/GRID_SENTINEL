package com.gridsentinel.application.incident;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.risk.RiskScore;

import java.util.Optional;

/**
 * Policy for determining when a risk score should trigger incident creation or escalation.
 * <p>
 * This encapsulates the business rules for incident lifecycle transitions based on
 * risk assessment results. The policy is deterministic, testable, and framework-independent.
 * </p>
 */
public interface IncidentTriggerPolicy {

    /**
     * Evaluate whether a risk score should trigger incident creation.
     *
     * @param riskScore the computed risk score
     * @param threatEvent the originating threat event (may be null if risk-only)
     * @return the evaluation result
     */
    IncidentEvaluation evaluateForCreation(RiskScore riskScore, ThreatContext threatEvent);

    /**
     * Evaluate whether an existing incident should be escalated based on new risk score.
     *
     * @param incident the existing incident
     * @param newRiskScore the new risk score
     * @return the evaluation result
     */
    IncidentEvaluation evaluateForEscalation(com.gridsentinel.incident.Incident incident, RiskScore newRiskScore);

    /**
     * Context about the originating threat for incident evaluation.
     */
    record ThreatContext(
            CommonKernel.ThreatEventId threatEventId,
            com.gridsentinel.common.ThreatSeverity threatSeverity,
            double threatConfidence,
            CommonKernel.AssetId affectedAssetId
    ) {}

    /**
     * Result of incident policy evaluation.
     */
    enum Decision {
        /** No action needed - risk below threshold or incident already handled */
        NO_ACTION,
        /** Create a new incident */
        CREATE_INCIDENT,
        /** Escalate existing incident (change severity/state) */
        ESCALATE_INCIDENT,
        /** Update existing incident with new information */
        UPDATE_INCIDENT
    }

    /**
     * Evaluation result with decision and explanation.
     */
    record IncidentEvaluation(
            Decision decision,
            String rationale,
            com.gridsentinel.common.ThreatSeverity suggestedSeverity,
            IncidentState suggestedState
    ) {
        public static IncidentEvaluation noAction(String rationale) {
            return new IncidentEvaluation(Decision.NO_ACTION, rationale, null, null);
        }

        public static IncidentEvaluation createIncident(com.gridsentinel.common.ThreatSeverity severity, String rationale) {
            return new IncidentEvaluation(Decision.CREATE_INCIDENT, rationale, severity, IncidentState.DETECTED);
        }

        public static IncidentEvaluation escalateIncident(com.gridsentinel.common.ThreatSeverity severity, String rationale) {
            return new IncidentEvaluation(Decision.ESCALATE_INCIDENT, rationale, severity, null);
        }

        public static IncidentEvaluation updateIncident(String rationale) {
            return new IncidentEvaluation(Decision.UPDATE_INCIDENT, rationale, null, null);
        }
    }
}