/**
 * Real-time Grid Event Types for Grid Sentinel
 * 
 * These types mirror the backend GridEventEnvelope and event payloads.
 * Events are delivered via WebSocket/STOMP and used for UI updates.
 */

// ============================================================================
// Common Types
// ============================================================================

/**
 * Correlation ID for event tracing (matches backend CommonKernel.CorrelationId)
 */
export type CorrelationId = string;

/**
 * Severity levels for grid events, matching backend EventSeverity enum
 */
export type EventSeverity = 
  | 'INFO'
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL'
  | 'EMERGENCY';

/**
 * Base envelope for all real-time grid events
 */
export interface GridEventEnvelope<T = unknown> {
  eventId: string;
  eventType: string;
  occurredAt: string; // ISO 8601 timestamp
  correlationId: CorrelationId;
  aggregateType: string;
  aggregateId: string;
  version: number;
  severity: EventSeverity;
  payload: T;
  source: string;
}

// ============================================================================
// Event Payload Types (Discriminated Union Members)
// ============================================================================

export interface ThreatDetectedPayload {
  threatEventId: string;
  threatType: string;
  severity: string;
  confidence: number;
  detectedAt: string;
  affectedAssetId: string | null;
  nearbyGridNodeId: string | null;
  description: string;
  evidenceIds: string[];
  detectionStrategy: string;
  strategyVersion: string;
}

export interface ThreatEscalatedPayload {
  threatEventId: string;
  previousSeverity: string;
  newSeverity: string;
  escalationReason: string;
}

export interface IncidentCreatedPayload {
  incidentId: string;
  incidentNumber: string;
  state: string;
  severity: string | null;
  originatingThreatId: string | null;
  primaryAssetId: string | null;
  primaryGridNodeId: string | null;
  description: string;
  assignedOperatorId: string | null;
  incidentCommanderId: string | null;
}

export interface IncidentStateChangedPayload {
  incidentId: string;
  incidentNumber: string;
  previousState: string;
  newState: string;
  actorId: string | null;
  reason: string | null;
  correlationId: string | null;
}

export interface RiskScoreUpdatedPayload {
  riskScoreId: string;
  assetId: string;
  previousScore: number;
  newScore: number;
  previousBand: string | null;
  newBand: string | null;
  modelName: string;
  modelVersion: string;
  strategyName: string | null;
}

export interface SimulationCompletedPayload {
  simulationId: string;
  name: string | null;
  scenarioId: string | null;
  status: string;
  strategyName: string | null;
  strategyVersion: string | null;
  randomSeed: number | null;
  errorMessage: string | null;
}

export interface ResponsePlanUpdatedPayload {
  responsePlanId: string;
  incidentId: string | null;
  name: string | null;
  previousStatus: string | null;
  newStatus: string;
  actorId: string | null;
  action: string;
}

export interface RecoveryProgressUpdatedPayload {
  recoveryStateId: string;
  incidentId: string | null;
  previousPhase: string | null;
  newPhase: string;
  previousProgress: number;
  newProgress: number;
  milestoneId: string | null;
  milestoneName: string | null;
  milestoneCompleted: boolean;
}

export interface GridAssetStatusChangedPayload {
  assetId: string;
  assetName: string | null;
  assetType: string;
  previousHealthStatus: string | null;
  newHealthStatus: string | null;
  previousCriticality: string | null;
  newCriticality: string | null;
  previousHealthIndex: number;
  newHealthIndex: number;
  previousRiskBand: string | null;
  newRiskBand: string | null;
  changeReason: string | null;
}

// ============================================================================
// Discriminated Union Type for All Events
// ============================================================================

export type RealtimeEvent =
  | GridEventEnvelope<ThreatDetectedPayload> & { eventType: 'ThreatDetected' }
  | GridEventEnvelope<ThreatEscalatedPayload> & { eventType: 'ThreatEscalated' }
  | GridEventEnvelope<IncidentCreatedPayload> & { eventType: 'IncidentCreated' }
  | GridEventEnvelope<IncidentStateChangedPayload> & { eventType: 'IncidentStateChanged' }
  | GridEventEnvelope<RiskScoreUpdatedPayload> & { eventType: 'RiskScoreUpdated' }
  | GridEventEnvelope<SimulationCompletedPayload> & { eventType: 'SimulationCompleted' }
  | GridEventEnvelope<ResponsePlanUpdatedPayload> & { eventType: 'ResponsePlanUpdated' }
  | GridEventEnvelope<RecoveryProgressUpdatedPayload> & { eventType: 'RecoveryProgressUpdated' }
  | GridEventEnvelope<GridAssetStatusChangedPayload> & { eventType: 'GridAssetStatusChanged' };

// ============================================================================
// Type Guards for Discriminated Union
// ============================================================================

export function isThreatDetectedEvent(event: RealtimeEvent): event is GridEventEnvelope<ThreatDetectedPayload> & { eventType: 'ThreatDetected' } {
  return event.eventType === 'ThreatDetected';
}

export function isThreatEscalatedEvent(event: RealtimeEvent): event is GridEventEnvelope<ThreatEscalatedPayload> & { eventType: 'ThreatEscalated' } {
  return event.eventType === 'ThreatEscalated';
}

export function isIncidentCreatedEvent(event: RealtimeEvent): event is GridEventEnvelope<IncidentCreatedPayload> & { eventType: 'IncidentCreated' } {
  return event.eventType === 'IncidentCreated';
}

export function isIncidentStateChangedEvent(event: RealtimeEvent): event is GridEventEnvelope<IncidentStateChangedPayload> & { eventType: 'IncidentStateChanged' } {
  return event.eventType === 'IncidentStateChanged';
}

export function isRiskScoreUpdatedEvent(event: RealtimeEvent): event is GridEventEnvelope<RiskScoreUpdatedPayload> & { eventType: 'RiskScoreUpdated' } {
  return event.eventType === 'RiskScoreUpdated';
}

export function isSimulationCompletedEvent(event: RealtimeEvent): event is GridEventEnvelope<SimulationCompletedPayload> & { eventType: 'SimulationCompleted' } {
  return event.eventType === 'SimulationCompleted';
}

export function isResponsePlanUpdatedEvent(event: RealtimeEvent): event is GridEventEnvelope<ResponsePlanUpdatedPayload> & { eventType: 'ResponsePlanUpdated' } {
  return event.eventType === 'ResponsePlanUpdated';
}

export function isRecoveryProgressUpdatedEvent(event: RealtimeEvent): event is GridEventEnvelope<RecoveryProgressUpdatedPayload> & { eventType: 'RecoveryProgressUpdated' } {
  return event.eventType === 'RecoveryProgressUpdated';
}

export function isGridAssetStatusChangedEvent(event: RealtimeEvent): event is GridEventEnvelope<GridAssetStatusChangedPayload> & { eventType: 'GridAssetStatusChanged' } {
  return event.eventType === 'GridAssetStatusChanged';
}

// ============================================================================
// Connection State Types
// ============================================================================

export type ConnectionState = 
  | 'CONNECTING'
  | 'CONNECTED'
  | 'RECONNECTING'
  | 'DISCONNECTED'
  | 'ERROR';

export interface ConnectionStatus {
  state: ConnectionState;
  lastConnectedAt: string | null;
  lastDisconnectedAt: string | null;
  reconnectAttempts: number;
  error: string | null;
}

// ============================================================================
// Event History Types (for audit/logging)
// ============================================================================

export interface EventHistoryEntry {
  eventId: string;
  eventType: string;
  timestamp: string;
  severity: EventSeverity;
  aggregateType: string;
  aggregateId: string;
  message: string;
  correlationId: CorrelationId;
}

// ============================================================================
// STOMP Destination Constants
// ============================================================================

export const STOMP_DESTINATIONS = {
  GRID: '/topic/grid',
  THREATS: '/topic/threats',
  INCIDENTS: '/topic/incidents',
  RISK: '/topic/risk',
  RECOVERY: '/topic/recovery',
  SIMULATION: '/topic/simulation',
} as const;

export const STOMP_ENDPOINT = '/ws/realtime';