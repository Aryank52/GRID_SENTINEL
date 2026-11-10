package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ResponsePriority;
import com.gridsentinel.incident.Incident;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ResponsePlanTest {

    @Test
    void responsePlanCreation() {
        CommonKernel.ResponsePlanId id = CommonKernel.ResponsePlanId.generate();
        CommonKernel.IncidentId incidentId = CommonKernel.IncidentId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();

        ResponsePlan plan = ResponsePlan.builder()
                .id(id)
                .incidentId(incidentId)
                .correlationId(correlationId)
                .name("Test Response Plan")
                .description("Test description")
                .build();

        assertThat(plan.getId()).isEqualTo(id);
        assertThat(plan.getIncidentId()).isEqualTo(incidentId);
        assertThat(plan.getCorrelationId()).isEqualTo(correlationId);
        assertThat(plan.getName()).isEqualTo("Test Response Plan");
        assertThat(plan.getDescription()).isEqualTo("Test description");
        assertThat(plan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.DRAFT);
        assertThat(plan.getActions()).isEmpty();
        assertThat(plan.getCreatedAt()).isNotNull();
        assertThat(plan.getVersion()).isEqualTo(0);
    }

    @Test
    void responsePlanIncidentIdCannotBeNull() {
        assertThatThrownBy(() -> ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void responsePlanAddAction() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        int oldVersion = plan.getVersion();
        plan.addAction(action);

        assertThat(plan.getActions()).hasSize(1);
        assertThat(plan.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void responsePlanRemoveAction() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        int versionAfterAdd = plan.getVersion();

        plan.removeAction(action.getId());

        assertThat(plan.getActions()).isEmpty();
        assertThat(plan.getVersion()).isEqualTo(versionAfterAdd + 1);
    }

    @Test
    void responsePlanCannotAddActionWhenNotDraft() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        // Add action first
        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        // Approve the plan
        plan.approve(CommonKernel.UserId.generate());

        ResponseAction newAction = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team 2")
                .description("Deploy fire suppression team 2")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        assertThatThrownBy(() -> plan.addAction(newAction))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void responsePlanApprove() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        int oldVersion = plan.getVersion();

        plan.approve(CommonKernel.UserId.generate());

        assertThat(plan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.APPROVED);
        assertThat(plan.getApprovedBy()).isNotNull();
        assertThat(plan.getApprovedAt()).isNotNull();
        assertThat(plan.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void responsePlanCannotApproveEmpty() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        assertThatThrownBy(() -> plan.approve(CommonKernel.UserId.generate()))
                .isInstanceOf(CommonKernel.ValidationException.class);
    }

    @Test
    void responsePlanStart() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        plan.approve(CommonKernel.UserId.generate());
        int oldVersion = plan.getVersion();

        plan.start();

        assertThat(plan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.EXECUTING);
        assertThat(plan.getStartedAt()).isNotNull();
        assertThat(plan.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void responsePlanComplete() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        plan.approve(CommonKernel.UserId.generate());
        plan.start();
        int oldVersion = plan.getVersion();

        // Start the action first, then complete it
        action.start(CommonKernel.UserId.generate());
        action.complete(CommonKernel.UserId.generate(), "Done");

        plan.complete();

        assertThat(plan.getStatus()).isEqualTo(ResponsePlan.PlanStatus.COMPLETED);
        assertThat(plan.getCompletedAt()).isNotNull();
        assertThat(plan.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void responsePlanCannotCompleteWithIncompleteActions() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Deploy team")
                .description("Deploy fire suppression team")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        plan.addAction(action);
        plan.approve(CommonKernel.UserId.generate());
        plan.start();

        // Action not completed
        assertThatThrownBy(() -> plan.complete())
                .isInstanceOf(CommonKernel.ValidationException.class);
    }

    @Test
    void responsePlanGetActionsByPriority() {
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .name("Test")
                .build();

        ResponseAction action1 = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Low priority")
                .description("Low")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.LOW)
                .build();

        ResponseAction action2 = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("High priority")
                .description("High")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.HIGH)
                .build();

        ResponseAction action3 = ResponseAction.builder()
                .id(CommonKernel.ResponseActionId.generate())
                .name("Critical priority")
                .description("Critical")
                .type(ResponseAction.ActionType.DEPLOY_CREW)
                .priority(ResponsePriority.IMMEDIATE)
                .build();

        plan.addAction(action1);
        plan.addAction(action2);
        plan.addAction(action3);

        List<ResponseAction> actionsByPriority = plan.getActionsByPriority();
        assertThat(actionsByPriority).hasSize(3);
        assertThat(actionsByPriority.get(0).getPriority()).isEqualTo(ResponsePriority.IMMEDIATE);
        assertThat(actionsByPriority.get(1).getPriority()).isEqualTo(ResponsePriority.HIGH);
        assertThat(actionsByPriority.get(2).getPriority()).isEqualTo(ResponsePriority.LOW);
    }

    @Test
    void responsePlanAllStatuses() {
        for (ResponsePlan.PlanStatus status : ResponsePlan.PlanStatus.values()) {
            // Just verify the enum values exist
            assertThat(status).isNotNull();
        }
    }

    @Test
    void responsePlanEquality() {
        CommonKernel.ResponsePlanId id = CommonKernel.ResponsePlanId.generate();
        CommonKernel.IncidentId incidentId = CommonKernel.IncidentId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();
        Instant createdAt = Instant.now();

        ResponsePlan plan1 = ResponsePlan.builder()
                .id(id)
                .incidentId(incidentId)
                .name("Test")
                .correlationId(correlationId)
                .build();

        ResponsePlan plan2 = ResponsePlan.builder()
                .id(id)
                .incidentId(incidentId)
                .name("Test")
                .correlationId(correlationId)
                .build();

        assertThat(plan1).isEqualTo(plan2);
        assertThat(plan1.hashCode()).isEqualTo(plan2.hashCode());
    }
}