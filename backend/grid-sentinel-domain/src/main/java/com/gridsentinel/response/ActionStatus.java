package com.gridsentinel.response;

/**
 * Status of a response action.
 */
public enum ActionStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    FAILED,
    SKIPPED,
    CANCELLED,
    VERIFIED
}