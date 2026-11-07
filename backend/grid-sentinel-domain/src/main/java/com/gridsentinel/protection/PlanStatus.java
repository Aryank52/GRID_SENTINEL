package com.gridsentinel.protection;

/**
 * Status of a protection plan.
 */
public enum PlanStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    ACTIVE,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLED,
    ARCHIVED
}