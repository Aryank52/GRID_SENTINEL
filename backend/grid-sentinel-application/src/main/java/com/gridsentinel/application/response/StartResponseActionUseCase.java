package com.gridsentinel.application.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.response.ResponsePlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for starting a response action.
 */
@Service
@Transactional
public class StartResponseActionUseCase {

    private final ResponsePlanRepository responsePlanRepository;
    private final EventPublisher eventPublisher;

    public StartResponseActionUseCase(
            ResponsePlanRepository responsePlanRepository,
            EventPublisher eventPublisher
    ) {
        this.responsePlanRepository = responsePlanRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Start a response action.
     *
     * @param planId the response plan containing the action
     * @param actionId the action to start
     * @param operatorId the operator starting the action
     * @return the updated response action
     */
    public com.gridsentinel.response.ResponseAction start(CommonKernel.ResponsePlanId planId,
                                                          CommonKernel.ResponseActionId actionId,
                                                          CommonKernel.UserId operatorId) {
        // Load and validate plan
        ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        // Verify plan is in executing state
        if (plan.getStatus() != com.gridsentinel.response.ResponsePlan.PlanStatus.APPROVED &&
            plan.getStatus() != com.gridsentinel.response.ResponsePlan.PlanStatus.EXECUTING) {
            throw new IllegalStateException("Cannot start actions in plan with status: " + plan.getStatus());
        }

        // Find the action
        com.gridsentinel.response.ResponseAction action = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found in plan: " + actionId));

        // Check dependencies
        if (!dependenciesSatisfied(action, plan)) {
            throw new IllegalStateException("Dependencies not satisfied for action: " + actionId);
        }

        // Verify action can be started
        if (action.getStatus() != com.gridsentinel.response.ResponseAction.ActionStatus.PENDING &&
            action.getStatus() != com.gridsentinel.response.ResponseAction.ActionStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot start action in status: " + action.getStatus());
        }

        // Start the action
        ResponsePlan.PlanStatus previousStatus = plan.getStatus();
        if (plan.getStatus() == com.gridsentinel.response.ResponsePlan.PlanStatus.APPROVED) {
            plan.startExecution();
        }
        action.start(operatorId);

        // Save and publish event
        ResponsePlan saved = responsePlanRepository.save(plan);
        eventPublisher.publishResponsePlanUpdated(saved, previousStatus, operatorId.toString(), "action_started");

        // Return the updated action
        return saved.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Action lost after save"));
    }

    private boolean dependenciesSatisfied(com.gridsentinel.response.ResponseAction action, ResponsePlan plan) {
        if (action.getDependsOn() == null || action.getDependsOn().isEmpty()) {
            return true;
        }
        return action.getDependsOn().stream()
                .allMatch(depId -> plan.getActions().stream()
                        .filter(a -> a.getId().equals(depId))
                        .findFirst()
                        .map(a -> a.getStatus() == com.gridsentinel.response.ResponseAction.ActionStatus.COMPLETED ||
                                a.getStatus() == com.gridsentinel.response.ResponseAction.ActionStatus.VERIFIED)
                        .orElse(false));
    }

    // Port interfaces
    public interface ResponsePlanRepository {
        Optional<com.gridsentinel.response.ResponsePlan> findById(CommonKernel.ResponsePlanId id);
        ResponsePlan save(ResponsePlan plan);
    }

    public interface EventPublisher {
        void publishResponsePlanUpdated(com.gridsentinel.response.ResponsePlan plan,
                                        com.gridsentinel.response.ResponsePlan.PlanStatus previousStatus,
                                        String actorId, String action);
    }
}