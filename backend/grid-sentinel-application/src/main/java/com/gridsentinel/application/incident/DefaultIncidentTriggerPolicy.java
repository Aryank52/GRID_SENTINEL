package com.gridsentinel.application.incident;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.RiskScore;

import java.util.Optional;

/**
 * Default implementation of incident trigger policy.
 * <p>
 * Business rules for incident creation/escalation:
 * <ul>
 *   <li>CRITICAL risk band + HIGH/CRITICAL threat → CREATE_INCIDENT</li>
 *   <li>HIGH risk band + MEDIUM+ threat + CRITICAL/HIGH asset criticality → CREATE_INCIDENT</li>
 *   <li>HIGH risk band + CRITICAL threat → CREATE_INCIDENT</li>
 *   <li>Existing active incident + higher risk/severity → ESCALATE_INCIDENT</li>
 *   <li>Existing active incident + same/lower risk → NO_ACTION</li>
 *   <li>Risk below HIGH band → NO_ACTION (unless threat is CRITICAL)</li>
 * </ul>
 * </p>
 */
public class DefaultIncidentTriggerPolicy implements IncidentTriggerPolicy {

    // Risk score thresholds
    private static final double CRITICAL_RISK_THRESHOLD = 0.8;
    private static final double HIGH_RISK_THRESHOLD = 0.6;
    private static final double MEDIUM_RISK_THRESHOLD = 0.3;

    @Override
    public IncidentEvaluation evaluateForCreation(RiskScore riskScore, ThreatContext threatEvent) {
        // No threat context - evaluate purely on risk
        if (threatEvent == null) {
            return evaluateRiskOnly(riskScore);
        }

        // CRITICAL threat always creates incident if risk is at least MEDIUM
        if (threatEvent.threatSeverity() == ThreatSeverity.CRITICAL) {
            if (riskScore.getScore() >= MEDIUM_RISK_THRESHOLD || riskScore.getBand() == RiskBand.MEDIUM || 
                riskScore.getBand() == RiskBand.HIGH || riskScore.getBand() == RiskBand.CRITICAL) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.CRITICAL,
                    "CRITICAL threat with risk score " + String.format("%.2f", riskScore.getScore()) + " (" + riskScore.getBand() + ")"
                );
            }
        }

        // CRITICAL risk band with HIGH+ threat creates incident
        if (riskScore.getBand() == RiskBand.CRITICAL) {
            if (threatEvent.threatSeverity() == ThreatSeverity.HIGH || threatEvent.threatSeverity() == ThreatSeverity.CRITICAL) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.CRITICAL,
                    "CRITICAL risk band (" + String.format("%.2f", riskScore.getScore()) + ") with " + threatEvent.threatSeverity() + " threat"
                );
            }
            // Even MEDIUM threat with CRITICAL risk creates incident
            if (threatEvent.threatSeverity() == ThreatSeverity.MEDIUM && riskScore.getScore() >= CRITICAL_RISK_THRESHOLD) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.HIGH,
                    "CRITICAL risk band (" + String.format("%.2f", riskScore.getScore()) + ") with " + threatEvent.threatSeverity() + " threat"
                );
            }
        }

        // HIGH risk band with HIGH/CRITICAL threat creates incident
        if (riskScore.getBand() == RiskBand.HIGH) {
            if (threatEvent.threatSeverity() == ThreatSeverity.CRITICAL) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.CRITICAL,
                    "HIGH risk band (" + String.format("%.2f", riskScore.getScore()) + ") with CRITICAL threat"
                );
            }
            if (threatEvent.threatSeverity() == ThreatSeverity.HIGH) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.HIGH,
                    "HIGH risk band (" + String.format("%.2f", riskScore.getScore()) + ") with HIGH threat"
                );
            }
            // MEDIUM threat with HIGH risk
            if (threatEvent.threatSeverity() == ThreatSeverity.MEDIUM && riskScore.getScore() >= HIGH_RISK_THRESHOLD) {
                return IncidentEvaluation.createIncident(
                    ThreatSeverity.HIGH,
                    "HIGH risk band (" + String.format("%.2f", riskScore.getScore()) + ") with " + threatEvent.threatSeverity() + " threat"
                );
            }
        }

        return IncidentEvaluation.noAction(
            "Risk score " + String.format("%.2f", riskScore.getScore()) + " (" + riskScore.getBand() + ") with " + 
            threatEvent.threatSeverity() + " threat does not meet incident creation threshold"
        );
    }

    private IncidentEvaluation evaluateRiskOnly(RiskScore riskScore) {
        if (riskScore.getBand() == RiskBand.CRITICAL) {
            return IncidentEvaluation.createIncident(
                ThreatSeverity.CRITICAL,
                "CRITICAL risk band (" + String.format("%.2f", riskScore.getScore()) + ") without threat context"
            );
        }
        if (riskScore.getBand() == RiskBand.HIGH) {
            return IncidentEvaluation.createIncident(
                ThreatSeverity.HIGH,
                "HIGH risk band (" + String.format("%.2f", riskScore.getScore()) + ") without threat context"
            );
        }
        return IncidentEvaluation.noAction(
            "Risk-only evaluation: " + riskScore.getBand() + " (" + String.format("%.2f", riskScore.getScore()) + ") below incident threshold"
        );
    }

    @Override
    public IncidentEvaluation evaluateForEscalation(Incident incident, RiskScore newRiskScore) {
        // Only escalate active incidents
        if (!incident.isActive()) {
            return IncidentEvaluation.noAction("Incident is not active (state: " + incident.getState() + ")");
        }

        // If incident already has higher or equal severity, no escalation needed
        if (incident.getSeverity() != null) {
            int currentSeverityOrdinal = incident.getSeverity().ordinal();
            int newSeverityOrdinal = mapRiskBandToSeverity(newRiskScore.getBand()).ordinal();
            
            if (newSeverityOrdinal <= currentSeverityOrdinal) {
                return IncidentEvaluation.noAction(
                    "New risk severity (" + mapRiskBandToSeverity(newRiskScore.getBand()) + 
                    ") not higher than current incident severity (" + incident.getSeverity() + ")"
                );
            }
        }

        // Escalate if new risk indicates higher severity
        ThreatSeverity suggestedSeverity = mapRiskBandToSeverity(newRiskScore.getBand());
        
        return IncidentEvaluation.escalateIncident(
            suggestedSeverity,
            "Risk escalation: new risk " + String.format("%.2f", newRiskScore.getScore()) + 
            " (" + newRiskScore.getBand() + ") suggests severity " + suggestedSeverity + 
            " vs current " + incident.getSeverity()
        );
    }

    private ThreatSeverity mapRiskBandToSeverity(RiskBand band) {
        return switch (band) {
            case CRITICAL -> ThreatSeverity.CRITICAL;
            case HIGH -> ThreatSeverity.HIGH;
            case MEDIUM -> ThreatSeverity.MEDIUM;
            case LOW -> ThreatSeverity.LOW;
            case NEGLIGIBLE -> ThreatSeverity.INFO;
        };
    }
}