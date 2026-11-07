package com.gridsentinel.audit;

/**
 * Types of audit events.
 */
public enum AuditEventType {
    CREATE,
    UPDATE,
    DELETE,
    TRANSITION,
    APPROVE,
    REJECT,
    ASSIGN,
    UNASSIGN,
    EXECUTE,
    COMPLETE,
    FAIL,
    CANCEL,
    ARCHIVE,
    RESTORE,
    LOGIN,
    LOGOUT,
    ACCESS_DENIED,
    CONFIGURATION_CHANGE,
    DATA_EXPORT,
    DATA_IMPORT,
    AI_ASSISTED_ACTION,
    SECURITY_EVENT
}