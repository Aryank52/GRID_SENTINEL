package com.gridsentinel.response;

/**
 * Status of a response plan.
 */
public enum PlanStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    ACTIVE,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLED,
    ARCHIVED
}