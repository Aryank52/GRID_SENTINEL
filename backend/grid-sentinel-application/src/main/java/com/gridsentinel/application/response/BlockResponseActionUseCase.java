package com.gridsentinel.application.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.response.ResponsePlan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Use case for blocking a response action.
 */
@Service
@Transactional
public class BlockResponseActionUseCase {

    private final ResponsePlanRepository responsePlanRepository;
    private final EventPublisher eventPublisher;

    public BlockResponseActionUseCase(
            ResponsePlanRepository responsePlanRepository,
            EventPublisher eventPublisher
    ) {
        this.responsePlanRepository = responsePlanRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Block a response action (mark as blocked/on hold).
     *
     * @param planId the response plan containing the action
     * @param actionId the action to block
     * @param operatorId the operator blocking the action
     * @param reason the blocking reason
     * @return the updated response action
     */
    public com.gridsentinel.response.ResponseAction block(CommonKernel.ResponsePlanId planId,
                                                          CommonKernel.ResponseActionId actionId,
                                                          CommonKernel.UserId operatorId,
                                                          String reason) {
        // Load and validate plan
        com.gridsentinel.response.ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        // Find the action
        com.gridsentinel.response.ResponseAction action = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found in plan: " + actionId));

        // Block the action
        ResponsePlan.PlanStatus previousStatus = plan.getStatus();
        com.gridsentinel.response.ResponseAction actionRef = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found: " + actionId));
        actionRef.hold();

        // Save and publish event
        com.gridsentinel.response.ResponsePlan saved = responsePlanRepository.save(plan);
        eventPublisher.publishResponsePlanUpdated(saved, previousStatus, operatorId.toString(), "action_blocked");

        // Return the updated action
        return saved.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Action lost after save"));
    }

    /**
     * Resume a blocked action.
     */
    public com.gridsentinel.response.ResponseAction resume(CommonKernel.ResponsePlanId planId,
                                                           CommonKernel.ResponseActionId actionId,
                                                           CommonKernel.UserId operatorId) {
        // Load and validate plan
        com.gridsentinel.response.ResponsePlan plan = responsePlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Response plan not found: " + planId));

        // Find the action
        com.gridsentinel.response.ResponseAction action = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found in plan: " + actionId));

        // Resume the action
        ResponsePlan.PlanStatus previousStatus = plan.getStatus();
        com.gridsentinel.response.ResponseAction actionRef = plan.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Action not found: " + actionId));
        actionRef.resume();

        // Save and publish event
        com.gridsentinel.response.ResponsePlan saved = responsePlanRepository.save(plan);
        eventPublisher.publishResponsePlanUpdated(saved, previousStatus, operatorId.toString(), "action_resumed");

        // Return the updated action
        return saved.getActions().stream()
                .filter(a -> a.getId().equals(actionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Action lost after save"));
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