package com.gridsentinel.detection;

/**
 * Lifecycle status of a threat event.
 */
public enum ThreatStatus {
    NEW,
    TRIAGED,
    CONFIRMED,
    ESCALATED_TO_INCIDENT,
    FALSE_POSITIVE,
    RESOLVED,
    SUPPRESSED
}