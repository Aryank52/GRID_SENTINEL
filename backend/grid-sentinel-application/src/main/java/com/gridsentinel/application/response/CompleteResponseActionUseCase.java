package com.gridsentinel.application.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.response.ResponsePlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for completing a response action.
 */
@Service
@Transactional
public class CompleteResponseActionUseCase {

    private final ResponsePlanRepository responsePlanRepository;
    private final EventPublisher eventPublisher;

    public CompleteResponseActionUseCase(
            ResponsePlanRepository responsePlanRepository,
            EventPublisher eventPublisher
    ) {
        this.responsePlanRepository = responsePlanRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Complete a response action.
     *
     * @param planId the response plan containing the action
     * @param actionId the action to complete
     * @param operatorId the operator completing the action
     * @param resultNotes the completion result/notes
     * @return the updated response action
     */
    public com.gridsentinel.response.ResponseAction complete(CommonKernel.ResponsePlanId planId,
                                                             CommonKernel.ResponseActionId actionId,
                                                             CommonKernel.UserId operatorId,
                                                             String resultNotes) {
        // Load and validate plan
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        // Find the action
        com.gridsentinel.response.ResponseAction action = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found in plan: " + actionId));

        // Complete the action
        ResponsePlan.PlanStatus previousStatus = plan.getStatus();
        com.gridsentinel.response.ResponseAction actionRef = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found: " + actionId));
        actionRef.complete(operatorId, resultNotes);

        // Check if plan can be completed
        checkAndCompletePlan(plan);

        // Save and publish event
        ResponsePlan saved = responsePlanRepository.save(plan);
        eventPublisher.publishResponsePlanUpdated(saved, plan.getStatus(), action.getAssignedTo().toString(), "action_completed");

        // Return the updated action
        return saved.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Action lost after save"));
    }

    private void checkAndCompletePlan(ResponsePlan plan) {
        // Check if all actions are completed
        boolean allCompleted = plan.getActions().stream()
                .allMatch(a -> a.getStatus() == com.gridsentinel.response.ResponseAction.ActionStatus.COMPLETED ||
                        a.getStatus() == com.gridsentinel.response.ResponseAction.ActionStatus.VERIFIED ||
                        a.getStatus() == com.gridsentinel.response.ResponseAction.ActionStatus.SKIPPED);

        if (allCompleted && plan.getStatus() == com.gridsentinel.response.ResponsePlan.PlanStatus.EXECUTING) {
            plan.complete();
        }
    }

    // Port interfaces
    public interface ResponsePlanRepository {
        Optional<com.gridsentinel.response.ResponsePlan> findById(CommonKernel.ResponsePlanId id);
        com.gridsentinel.response.ResponsePlan save(com.gridsentinel.response.ResponsePlan plan);
    }

    public interface EventPublisher {
        void publishResponsePlanUpdated(com.gridsentinel.response.ResponsePlan plan,
                                        com.gridsentinel.response.ResponsePlan.PlanStatus previousStatus,
                                        String actorId, String action);
    }
}