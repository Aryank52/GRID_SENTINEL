# Phase 2E - Real-time Grid Events Architecture

## Overview
This document describes the real-time event system architecture for Grid Sentinel Phase 2E, covering the event model, publication flow, STOMP/WS transport, frontend handling, and integration with the Digital Grid Twin.

---

## 1. Event Model

### 1.1 GridEventEnvelope
Generic immutable envelope wrapping all grid events:
```java
public record GridEventEnvelope<T>(
    String eventId,           // UUID
    String eventType,         // Discriminator: "ThreatDetected", "IncidentCreated", etc.
    Instant occurredAt,       // ISO 8601 timestamp
    CorrelationId correlationId,  // For tracing/correlation
    String aggregateType,     // e.g., "ThreatEvent", "Incident", "Asset"
    String aggregateId,       // Entity ID
    Long version,             // Optimistic locking version
    EventSeverity severity,   // INFO, LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY
    T payload,                // Typed payload
    String source             // Originating system: "detection-pipeline", "incident-management", etc.
)
```

### 1.2 EventSeverity Enum
```
INFO < LOW < MEDIUM < HIGH < CRITICAL < EMERGENCY
```

### 1.3 GridEvent Hierarchy (Sealed)
```
GridEvent<P> (abstract sealed)
├── ThreatDetectedEvent
├── ThreatEscalatedEvent
├── IncidentCreatedEvent
├── IncidentStateChangedEvent
├── RiskScoreUpdatedEvent
├── SimulationCompletedEvent
├── ResponsePlanUpdatedEvent
├── RecoveryProgressUpdatedEvent
└── GridAssetStatusChangedEvent
```

Each concrete event carries only the fields needed by consumers - no JPA entities, no domain aggregates.

---

## 2. Event Catalog

| Event Type | Trigger | Payload Key Fields | STOMP Destination |
|------------|---------|-------------------|-------------------|
| ThreatDetected | Detection pipeline creates ThreatEvent | threatEventId, threatType, severity, confidence, detectedAt, affectedAssetId, description | /topic/threats |
| ThreatEscalated | ThreatEvent.escalateSeverity() | threatEventId, previousSeverity, newSeverity, escalationReason | /topic/threats |
| IncidentCreated | Incident entity created | incidentId, incidentNumber, state, severity, originatingThreatId, primaryAssetId, description | /topic/incidents |
| IncidentStateChanged | Incident state transition | incidentId, incidentNumber, previousState, newState, actorId, reason | /topic/incidents |
| RiskScoreUpdated | RiskScore computed/updated | riskScoreId, assetId, previousScore, newScore, previousBand, newBand, modelName | /topic/risk |
| SimulationCompleted | Simulation finishes | simulationId, name, scenarioId, status, strategyName, errorMessage | /topic/simulation |
| ResponsePlanUpdated | ResponsePlan status/action change | responsePlanId, incidentId, name, previousStatus, newStatus, actorId, action | /topic/incidents |
| RecoveryProgressUpdated | Recovery milestone/phase change | recoveryStateId, incidentId, previousPhase, newPhase, progress, milestoneId, milestoneCompleted | /topic/recovery |
| GridAssetStatusChanged | Asset health/criticality change | assetId, assetName, assetType, previousHealthStatus, newHealthStatus, previousCriticality, newCriticality | /topic/grid |

---

## 3. Publication Flow

```
DOMAIN ACTION
    ↓
Domain/Application Event (e.g., ThreatEvent.CreatedEvent)
    ↓
Application Port: RealTimeEventPublisher
    ↓
Infrastructure Adapter: StompRealTimeEventPublisher
    ↓
STOMP over WebSocket (/ws/realtime)
    ↓
Topic: /topic/{threats|incidents|risk|simulation|recovery|grid}
```

**Key Principle**: REST remains authoritative state source. WebSocket is notification transport only.

---

## 4. STOMP Destinations

| Destination | Events | Subscriber Roles |
|-------------|--------|------------------|
| /topic/grid | GridAssetStatusChanged | OPERATOR, DISPATCHER, INCIDENT_COMMANDER, PLANNER, SIMULATION_ENGINEER, SUPERVISOR, MANAGER, ADMINISTRATOR, VIEWER |
| /topic/threats | ThreatDetected, ThreatEscalated | OPERATOR, DISPATCHER, INCIDENT_COMMANDER, SECURITY_ANALYST, SUPERVISOR, MANAGER, ADMINISTRATOR |
| /topic/incidents | IncidentCreated, IncidentStateChanged, ResponsePlanUpdated | OPERATOR, DISPATCHER, INCIDENT_COMMANDER, RECOVERY_MANAGER, SUPERVISOR, MANAGER, ADMINISTRATOR, VIEWER |
| /topic/risk | RiskScoreUpdated | RISK_ANALYST, PLANNER, SUPERVISOR, MANAGER, DIRECTOR, ADMINISTRATOR, VIEWER |
| /topic/recovery | RecoveryProgressUpdated | RECOVERY_MANAGER, INCIDENT_COMMANDER, OPERATOR, SUPERVISOR, MANAGER, ADMINISTRATOR |
| /topic/simulation | SimulationCompleted | SIMULATION_ENGINEER, PLANNER, INCIDENT_COMMANDER, SUPERVISOR, MANAGER, ADMINISTRATOR |

---

## 5. Authentication & Authorization

### 5.1 WebSocket CONNECT
- JWT token in `Authorization: Bearer <token>` header
- Validated via `JwtTokenProvider` (same key as REST)
- On failure: connection rejected

### 5.2 STOMP SUBSCRIBE
- Role-based authorization per destination
- All operational roles can subscribe to topic/*
- `/user/*` queues for user-specific messages
- JWT never logged

---

## 6. Reconnection Strategy

```
CONNECTED
    ↓ (connection lost)
RECONNECTING (exponential backoff: 1s, 2s, 4s, 8s, 16s... capped at 30s)
    ↓ (max 10 attempts)
ERROR (after max attempts)
    ↓ (manual intervention or token refresh)
CONNECTING
```

- Heartbeat: 10s incoming/outgoing
- Jitter: ±1s added to backoff
- State exposed to UI: CONNECTING | CONNECTED | RECONNECTING | DISCONNECTED | ERROR

---

## 7. Resynchronization on Reconnect

```
WebSocket Reconnect
    ↓
Re-subscribe to all topics
    ↓
Refresh critical TanStack Query datasets:
  - Active threats
  - Active incidents  
  - Risk summaries
  - Recovery summaries
  - Spatial threat/incident/asset data
    ↓
Resume realtime subscription
```

Ensures eventual consistency after downtime.

---

## 8. Frontend Event Handling

### 8.1 RealtimeClient (Singleton)
- One connection per authenticated session
- Exponential backoff reconnection
- Heartbeat support
- Event validation with type guards
- Connection state exposure

### 8.2 useRealtimeEvents Hook
- Bounded deduplication (1000 events, 5min TTL)
- TanStack Query cache invalidation routing
- Event history (last 100)
- Connection status: LIVE | RECONNECTING | OFFLINE | ERROR

### 8.3 Query Invalidation Routing
| Event | Query Keys Invalidated |
|-------|------------------------|
| ThreatDetected/Escalated | ['threats'], ['threats','active'], ['spatial','threats'] |
| IncidentCreated/StateChanged | ['incidents'], ['incidents','active'], ['spatial','incidents'], ['incidents',{id}] |
| RiskScoreUpdated | ['risk','asset',{id}], ['risk','summary'], ['spatial','risk'] |
| SimulationCompleted | ['simulations'], ['simulations',{id}], ['spatial','simulation',{id}] |
| ResponsePlanUpdated | ['response-plans'], ['response-plans',{id}], ['incidents',{id},'response-plan'] |
| RecoveryProgressUpdated | ['recovery',{id}], ['recovery'], ['incidents',{id},'recovery'] |
| GridAssetStatusChanged | ['assets',{id}], ['assets'], ['spatial','assets'], ['risk','asset',{id}] |

---

## 9. Digital Grid Twin Integration

### 9.1 MapLibre Update Model
```
Event
    ↓
Query Invalidation / Targeted Cache Update
    ↓
Spatial Data Refresh (useViewportSpatialData hook)
    ↓
GeoJSON Source Update (map.getSource().setData())
    ↓
Specific Layer Update (no map remount)
```

- Map instance remains stable
- Only affected layers update
- No full-page rerenders

### 9.2 Event-to-Layer Mapping
| Event | Affected Layers |
|-------|----------------|
| ThreatDetected/Escalated | ThreatLayer |
| IncidentCreated/StateChanged | IncidentLayer |
| RiskScoreUpdated | RiskLayer (heatmap), AssetLayer |
| GridAssetStatusChanged | AssetLayer |
| SimulationCompleted | Simulation overlay (if active) |

---

## 10. REST Fallback

When WebSocket unavailable:
- Command Center remains fully usable via REST
- Live Grid Map loads spatial data via REST
- Connection status shows: OFFLINE
- No application functionality lost

---

## 11. Audit Compatibility

- Every state change produces domain event → audit record → WebSocket notification
- WebSocket delivery is NOT the audit log
- CorrelationId enables end-to-end tracing
- Event envelope includes all audit-relevant fields

---

## 12. Limitations

1. **In-memory STOMP broker** - Not clustered; single-instance deployment
2. **No persistent event store** - Missed events during downtime recovered via resync
3. **Demo endpoints** - `/api/v1/dev/realtime/*` for testing only; not for production
4. **Event payload size** - Keep under 64KB for STOMP frame limits
5. **Reconnection** - Max 10 attempts; manual refresh needed after

---

## 13. Security Considerations

- JWT validation on both CONNECT and SUBSCRIBE
- Role-based destination access
- No sensitive data in event payloads (IDs, enums, timestamps only)
- CorrelationId enables tracing without exposing internals
- Heartbeat prevents stale connections