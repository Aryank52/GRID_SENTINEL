/**
 * Grid Sentinel Event Package.
 * <p>
 * Contains all grid event types and the base event infrastructure.
 * Events are immutable, serializable, and designed for transport over WebSocket/STOMP.
 * </p>
 * <p>
 * Event hierarchy:
 * <pre>
 * GridEvent (sealed abstract base)
 * ├─ ThreatDetectedEvent
 * ├─ ThreatEscalatedEvent
 * ├─ IncidentCreatedEvent
 * ├─ IncidentStateChangedEvent
 * ├─ RiskScoreUpdatedEvent
 * ├─ SimulationCompletedEvent
 * ├─ ResponsePlanUpdatedEvent
 * ├─ RecoveryProgressUpdatedEvent
 * └─ GridAssetStatusChangedEvent
 * </pre>
 * </p>
 *
 * @see GridEvent
 * @see GridEventEnvelope
 */
package com.gridsentinel.event;