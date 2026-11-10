package com.gridsentinel.application.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.IncidentState;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.response.ResponseAction.ActionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Policy for determining if an incident is eligible for recovery initialization.
 * <p>
 * Evaluates whether an incident has reached the appropriate stabilization condition
 * to begin the recovery process.
 * </p>
 */
public interface RecoveryEligibilityPolicy {

    /**
     * Evaluate if an incident is eligible for recovery initialization.
     *
     * @param incident the incident to evaluate
     * @return the evaluation result
     */
    EligibilityResult evaluate(Incident incident);

    /**
     * Result of recovery eligibility evaluation.
     */
    record EligibilityResult(
            boolean eligible,
            String rationale,
            List<String> missingPrerequisites
    ) {
        public static EligibilityResult eligible(String rationale) {
            return new EligibilityResult(true, rationale, List.of());
        }

        public static EligibilityResult notEligible(String rationale, List<String> missingPrerequisites) {
            return new EligibilityResult(false, rationale, missingPrerequisites);
        }
    }
}