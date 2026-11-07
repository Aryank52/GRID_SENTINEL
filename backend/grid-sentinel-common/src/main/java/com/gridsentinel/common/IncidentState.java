package com.gridsentinel.common;

/**
 * Incident lifecycle states.
 */
public enum IncidentState {
    DETECTED,
    TRIAGED,
    CONFIRMED,
    ACTIVE_RESPONSE,
    CONTAINED,
    RECOVERING,
    RESOLVED,
    CLOSED
}