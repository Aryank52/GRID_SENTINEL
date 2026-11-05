/**
 * Incident bounded context.
 * <p>
 * Operational lifecycle object created from a confirmed or operator-accepted threat.
 * State machine: DETECTED -> TRIAGED -> CONFIRMED -> ACTIVE_RESPONSE -> CONTAINED -> RECOVERING -> RESOLVED -> CLOSED
 * Immutable audit trail of all state transitions.
 * </p>
 *
 * @since 0.1.0
 */
package com.gridsentinel.incident;