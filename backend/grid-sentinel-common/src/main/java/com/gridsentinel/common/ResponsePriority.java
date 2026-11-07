package com.gridsentinel.common;

/**
 * Response action priority.
 */
public enum ResponsePriority {
    IMMEDIATE,    // < 15 min
    URGENT,       // < 1 hour
    HIGH,         // < 4 hours
    NORMAL,       // < 24 hours
    LOW,          // < 7 days
    DEFERRED      // Scheduled maintenance
}