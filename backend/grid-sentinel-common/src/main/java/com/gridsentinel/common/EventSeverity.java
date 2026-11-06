package com.gridsentinel.common;

/**
 * Severity levels for grid events, used for routing, alerting, and UI prioritization.
 */
public enum EventSeverity {
    /**
     * Informational event, no immediate action required.
     */
    INFO,
    /**
     * Low priority, informational with potential future impact.
     */
    LOW,
    /**
     * Medium priority, requires attention but not immediately critical.
     */
    MEDIUM,
    /**
     * High priority, requires attention soon.
     */
    HIGH,
    /**
     * Critical priority, requires immediate attention and action.
     */
    CRITICAL,
    /**
     * Emergency/Catastrophic - system-wide impact, all hands.
     */
    EMERGENCY
}