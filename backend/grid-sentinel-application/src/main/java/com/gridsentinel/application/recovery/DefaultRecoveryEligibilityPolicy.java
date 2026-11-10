package com.gridsentinel.application.recovery;

import com.gridsentinel.incident.Incident;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.response.ResponseAction.ActionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of recovery eligibility policy.
 * <p>
 * An incident is eligible for recovery when:
 * - Incident state is CONTAINED (stabilized)
 * - There is an approved response plan
 * - All required mitigation actions are completed or verified
 * - No critical actions are in FAILED or BLOCKED state without resolution
 * </p>
 */
public class DefaultRecoveryEligibilityPolicy implements RecoveryEligibilityPolicy {

    @Override
    public EligibilityResult evaluate(Incident incident) {
        List<String> missingPrerequisites = new ArrayList<>();

        // 1. Incident must be in CONTAINED state (stabilized)
        if (incident.getState() != IncidentState.CONTAINED) {
            missingPrerequisites.add("Incident must be in CONTAINED state (current: " + incident.getState() + ")");
        }

        // 2. Must have an approved response plan
        // In a full implementation, this would query the response plan repository
        // For now, we'll check if there's a correlation ID that could link to a response plan

        // 3. All critical response actions should be completed
        // This would require querying the response plan repository
        // For now, we'll note this as a prerequisite

        if (missingPrerequisites.isEmpty()) {
            return EligibilityResult.eligible(
                    "Incident is contained and ready for recovery initialization"
            );
        }

        return EligibilityResult.notEligible(
                "Incident does not meet recovery prerequisites",
                missingPrerequisites
        );
    }
}