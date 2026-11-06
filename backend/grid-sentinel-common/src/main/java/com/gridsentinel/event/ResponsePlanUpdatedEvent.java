package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;

import java.util.Objects;

/**
 * Event fired when a response plan is created, updated, approved, started, or completed.
 */
public final class ResponsePlanUpdatedEvent extends GridEvent {

    private final String responsePlanId;
    private final String incidentId;
    private final String name;
    private final String previousStatus;
    private final String newStatus;
    private final String actorId;
    private final String action; // CREATED, UPDATED, APPROVED, STARTED, COMPLETED, CANCELLED

    public ResponsePlanUpdatedEvent(CorrelationId correlationId,
                                    String responsePlanId,
                                    String incidentId,
                                    String name,
                                    String previousStatus,
                                    String newStatus,
                                    String actorId,
                                    String action,
                                    String source) {
        super("ResponsePlanUpdated", correlationId, "ResponsePlan", responsePlanId, 1,
                determineSeverity(action),
                "response-planning");
        this.responsePlanId = Objects.requireNonNull(responsePlanId);
        this.incidentId = incidentId;
        this.name = name;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.actorId = actorId;
        this.action = action;
    }

    private static EventSeverity determineSeverity(String action) {
        return switch (action) {
            case "STARTED", "COMPLETED" -> EventSeverity.HIGH;
            case "APPROVED" -> EventSeverity.MEDIUM;
            case "CANCELLED" -> EventSeverity.LOW;
            default -> EventSeverity.MEDIUM;
        };
    }

    @Override
    public String eventType() {
        return "ResponsePlanUpdated";
    }

    @Override
    protected Object payload() {
        return new Payload(
                responsePlanId,
                incidentId,
                name,
                previousStatus,
                newStatus,
                actorId,
                action
        );
    }

    public record Payload(
            String responsePlanId,
            String incidentId,
            String name,
            String previousStatus,
            String newStatus,
            String actorId,
            String action
    ) {}
}