package com.gridsentinel.application.response;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import com.gridsentinel.risk.ProtectionAction;
import com.gridsentinel.risk.ActionCategory;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.common.ResponsePriority;
import com.gridsentinel.grid.TopologySnapshot;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default deterministic response planning strategy.
 * <p>
 * Generates response plans by:
 * 1. Filtering actions by budget and resource constraints
 * 2. Ranking actions by cost-effectiveness and risk reduction
 * 3. Building action sequences respecting dependencies
 * 4. Setting priorities based on risk reduction and urgency
 * </p>
 */
@Component
public class DefaultResponsePlanningStrategy implements ResponsePlanningStrategy {

    private static final String STRATEGY_NAME = "default-response-planning-v1";
    private static final String STRATEGY_VERSION = "1.0";

    @Override
    public ResponsePlan plan(Incident incident, TopologySnapshot topology, PlanningContext context) {
        // Filter actions by budget constraint
        List<ProtectionAction> filteredActions = context.availableActions().stream()
                .filter(a -> a.getEstimatedCost() <= context.constraints().budgetLimit())
                .toList();

        // Rank actions by cost-effectiveness (risk reduction per dollar)
        List<ProtectionAction> rankedActions = filteredActions.stream()
                .sorted(Comparator.comparingDouble(ProtectionAction::adjustedCostEffectiveness).reversed())
                .toList();

        // Select top actions within budget
        List<ProtectionAction> selectedActions = selectActionsWithinBudget(rankedActions, context.constraints().budgetLimit());

        // Build response actions from protection actions
        List<ResponseAction> responseActions = buildResponseActions(selectedActions, incident);

        // Build response plan
        ResponsePlan plan = ResponsePlan.builder()
                .id(CommonKernel.ResponsePlanId.generate())
                .incidentId(incident.getId())
                .name("Response Plan for " + incident.getIncidentNumber())
                .description("Auto-generated from simulation impact assessment")
                .build();

        // Add actions to plan
        for (ResponseAction action : responseActions) {
            plan.addAction(action);
        }

        return plan;
    }

    private List<ProtectionAction> selectActionsWithinBudget(
            List<ProtectionAction> rankedActions,
            double budgetLimit
    ) {
        List<ProtectionAction> selected = new ArrayList<>();
        double totalCost = 0.0;

        for (ProtectionAction action : rankedActions) {
            if (totalCost + action.getEstimatedCost() <= budgetLimit) {
                selected.add(action);
                totalCost += action.getEstimatedCost();
            }
        }

        return selected;
    }

    private List<ResponseAction> buildResponseActions(
            List<ProtectionAction> protectionActions,
            Incident incident
    ) {
        List<ResponseAction> actions = new ArrayList<>();

        for (int i = 0; i < protectionActions.size(); i++) {
            ProtectionAction pa = protectionActions.get(i);

            // Map protection action category to response action type
            ResponseAction.ActionType actionType = mapCategoryToActionType(pa.getCategory());

            // Determine priority based on risk reduction and urgency
            ResponsePriority priority = determinePriority(pa, incident);

            ResponseAction action = ResponseAction.builder()
                    .id(CommonKernel.ResponseActionId.generate())
                    .name(pa.getName())
                    .description(pa.getDescription())
                    .type(actionType)
                    .priority(priority)
                    .addDependency(actions.isEmpty() ? null : actions.get(i - 1).getId()) // Sequential dependency
                    .targetAssetId(null) // Would be set from affected assets
                    .plannedStartAt(Instant.now().plus(java.time.Duration.ofHours(i * 2))) // Staggered starts
                    .plannedEndAt(Instant.now().plus(java.time.Duration.ofHours((i + 1) * 2)))
                    .correlationId(CommonKernel.CorrelationId.generate())
                    .build();

            actions.add(action);
        }

        return actions;
    }

    private ResponseAction.ActionType mapCategoryToActionType(ActionCategory category) {
        return switch (category) {
            case INSPECTION_PROGRAM -> ResponseAction.ActionType.ASSESS_DAMAGE;
            case MAINTENANCE_PROGRAM -> ResponseAction.ActionType.REPAIR_EQUIPMENT;
            case VEGETATION_MANAGEMENT -> ResponseAction.ActionType.ISOLATE_SECTION;
            case HARDENING -> ResponseAction.ActionType.REPAIR_EQUIPMENT;
            case REDUNDANCY -> ResponseAction.ActionType.RESTORE_POWER;
            case MONITORING -> ResponseAction.ActionType.ASSESS_DAMAGE;
            case AUTOMATION -> ResponseAction.ActionType.RESTORE_POWER;
            case PHYSICAL_SECURITY -> ResponseAction.ActionType.SECURE_PERIMETER;
            case CYBER_SECURITY -> ResponseAction.ActionType.COORDINATE_AGENCIES;
            case EMERGENCY_PREPAREDNESS -> ResponseAction.ActionType.DEPLOY_CREW;
            case COMMUNICATION_UPGRADE -> ResponseAction.ActionType.COMMUNICATE_PUBLIC;
            case PROTECTION_UPGRADE -> ResponseAction.ActionType.REPAIR_EQUIPMENT;
            case RELAY_SETTING_OPTIMIZATION -> ResponseAction.ActionType.RESTORE_POWER;
            case FAULT_LOCATION_IMPROVEMENT -> ResponseAction.ActionType.ASSESS_DAMAGE;
            case VOLTAGE_REGULATION -> ResponseAction.ActionType.RESTORE_POWER;
            case LOAD_BALANCING -> ResponseAction.ActionType.RESTORE_POWER;
            case GENERATION_DISPATCH -> ResponseAction.ActionType.RESTORE_POWER;
            case DEMAND_RESPONSE -> ResponseAction.ActionType.COMMUNICATE_PUBLIC;
            case ENERGY_STORAGE -> ResponseAction.ActionType.RESTORE_POWER;
            case MICROGRID_FORMATION -> ResponseAction.ActionType.ISOLATE_SECTION;
            case ISLANDING_CAPABILITY -> ResponseAction.ActionType.ISOLATE_SECTION;
            case BLACK_START_CAPABILITY -> ResponseAction.ActionType.RESTORE_POWER;
            case TRAINING -> ResponseAction.ActionType.DEPLOY_CREW;
            default -> ResponseAction.ActionType.ASSESS_DAMAGE;
        };
    }

    private ResponsePriority determinePriority(ProtectionAction action, Incident incident) {
        // Higher risk reduction and higher incident severity = higher priority
        double riskFactor = action.getExpectedRiskReduction();
        double severityFactor = switch (incident.getSeverity()) {
            case CRITICAL -> 1.0;
            case HIGH -> 0.75;
            case MEDIUM -> 0.5;
            case LOW -> 0.25;
            default -> 0.1;
        };

        double priorityScore = riskFactor * 0.7 + severityFactor * 0.3;

        if (priorityScore >= 0.8) return ResponsePriority.IMMEDIATE;
        if (priorityScore >= 0.6) return ResponsePriority.URGENT;
        if (priorityScore >= 0.4) return ResponsePriority.HIGH;
        if (priorityScore >= 0.2) return ResponsePriority.NORMAL;
        return ResponsePriority.LOW;
    }

    @Override
    public StrategyMetadata metadata() {
        return new StrategyMetadata(
                STRATEGY_NAME,
                STRATEGY_VERSION,
                "Default response planning using cost-effectiveness ranking and budget constraints",
                List.of("available_actions", "budget_limit", "resource_constraints", "incident_severity")
        );
    }
}