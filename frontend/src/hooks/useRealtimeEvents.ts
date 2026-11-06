/**
 * Real-time Event Handling Hook for Grid Sentinel
 * 
 * Provides:
 * - Event deduplication using bounded event ID cache
 * - TanStack Query cache invalidation/update
 * - Connection state management
 * - Centralized event routing
 */

import { useEffect, useCallback, useRef, useState, useMemo } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { getRealtimeClient, resetRealtimeClient } from '@services/realtimeClient';
import type {
  RealtimeEvent,
  ConnectionStatus,
  EventHistoryEntry,
} from '../types/realtime';
import {
  isThreatDetectedEvent,
  isThreatEscalatedEvent,
  isIncidentCreatedEvent,
  isIncidentStateChangedEvent,
  isRiskScoreUpdatedEvent,
  isSimulationCompletedEvent,
  isResponsePlanUpdatedEvent,
  isRecoveryProgressUpdatedEvent,
  isGridAssetStatusChangedEvent,
} from '../types/realtime';

// =========================================================================
// Event Message Formatting
// =========================================================================

/**
 * Create a human-readable message from an event
 */
function createEventMessage(event: RealtimeEvent): string {
  // Use type guards for proper payload typing
  if (isThreatDetectedEvent(event)) {
    return `Threat detected: ${event.payload.threatType} (${event.payload.severity}) - ${event.payload.description}`;
  }
  if (isThreatEscalatedEvent(event)) {
    return `Threat escalated: ${event.payload.previousSeverity} → ${event.payload.newSeverity} - ${event.payload.escalationReason}`;
  }
  if (isIncidentCreatedEvent(event)) {
    return `Incident created: ${event.payload.incidentNumber} (${event.payload.state}) - ${event.payload.description}`;
  }
  if (isIncidentStateChangedEvent(event)) {
    return `Incident ${event.payload.incidentNumber}: ${event.payload.previousState} → ${event.payload.newState}`;
  }
  if (isRiskScoreUpdatedEvent(event)) {
    return `Risk score updated for asset ${event.payload.assetId}: ${event.payload.previousScore?.toFixed(2) || 'N/A'} → ${event.payload.newScore?.toFixed(2)} (${event.payload.newBand})`;
  }
  if (isSimulationCompletedEvent(event)) {
    return `Simulation completed: ${event.payload.name} (${event.payload.status})`;
  }
  if (isResponsePlanUpdatedEvent(event)) {
    return `Response plan ${event.payload.action}: ${event.payload.name} (${event.payload.newStatus})`;
  }
  if (isRecoveryProgressUpdatedEvent(event)) {
    return `Recovery progress: ${event.payload.previousPhase} → ${event.payload.newPhase} (${(event.payload.newProgress * 100).toFixed(0)}%)`;
  }
  if (isGridAssetStatusChangedEvent(event)) {
    return `Asset status changed: ${event.payload.assetName} (${event.payload.assetType}) - ${event.payload.previousHealthStatus} → ${event.payload.newHealthStatus}`;
  }
  // Exhaustiveness check - should never reach here if all event types are handled
  const _exhaustive: never = event;
  return `${_exhaustive} event`;
}

// Deduplication cache configuration
const DEDUPLICATION_CACHE_SIZE = 1000;
const DEDUPLICATION_CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

interface ProcessedEventCacheEntry {
  eventId: string;
  timestamp: number;
}

/**
 * Hook for managing real-time grid events
 * 
 * @returns Object with connection status, event history, and control functions
 */
export function useRealtimeEvents() {
  const queryClient = useQueryClient();
  // Get token from localStorage directly since auth context doesn't expose it
  const accessToken = useMemo(() => localStorage.getItem('access_token'), []);
  const isAuthenticated = !!accessToken;
  
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>({
    state: 'DISCONNECTED',
    lastConnectedAt: null,
    lastDisconnectedAt: null,
    reconnectAttempts: 0,
    error: null,
  });
  
  const [eventHistory, setEventHistory] = useState<EventHistoryEntry[]>([]);
  const [latestEvent, setLatestEvent] = useState<RealtimeEvent | null>(null);
  
  const processedEventsRef = useRef<ProcessedEventCacheEntry[]>([]);
  const eventHistoryRef = useRef<EventHistoryEntry[]>([]);

  // =========================================================================
  // Deduplication Logic
  // =========================================================================

  const isEventProcessed = useCallback((eventId: string): boolean => {
    const now = Date.now();
    const cache = processedEventsRef.current;
    
    // Clean expired entries
    const validEntries = cache.filter(entry => now - entry.timestamp < DEDUPLICATION_CACHE_TTL_MS);
    processedEventsRef.current = validEntries;
    
    return validEntries.some(entry => entry.eventId === eventId);
  }, []);

  const markEventProcessed = useCallback((eventId: string): void => {
    const now = Date.now();
    const cache = processedEventsRef.current;
    
    // Remove if already exists (update timestamp)
    const index = cache.findIndex(entry => entry.eventId === eventId);
    if (index >= 0) {
      cache[index].timestamp = now;
      return;
    }
    
    // Add new entry
    cache.push({ eventId, timestamp: now });
    
    // Trim to max size
    if (cache.length > DEDUPLICATION_CACHE_SIZE) {
      // Remove oldest entries
      cache.sort((a, b) => a.timestamp - b.timestamp);
      cache.splice(0, cache.length - DEDUPLICATION_CACHE_SIZE);
    }
  }, []);

  // =========================================================================
  // Event Processing
  // =========================================================================

  const processEvent = useCallback((event: RealtimeEvent): void => {
    // Deduplication check
    if (isEventProcessed(event.eventId)) {
      console.debug(`Duplicate event ignored: ${event.eventType} (${event.eventId})`);
      return;
    }
    
    markEventProcessed(event.eventId);
    
    // Create history entry
    const historyEntry: EventHistoryEntry = {
      eventId: event.eventId,
      eventType: event.eventType,
      timestamp: event.occurredAt,
      severity: event.severity,
      aggregateType: event.aggregateType,
      aggregateId: event.aggregateId,
      message: createEventMessage(event),
      correlationId: event.correlationId,
    };
    
    // Update history (bounded to last 100 events)
    eventHistoryRef.current = [historyEntry, ...eventHistoryRef.current].slice(0, 100);
    setEventHistory([...eventHistoryRef.current]);
    setLatestEvent(event);
    
    // Route to appropriate query invalidation/update
    routeEventToQueryClient(event, queryClient);
  }, [isEventProcessed, markEventProcessed, queryClient]);

  // =========================================================================
  // Query Invalidation Routing
  // =========================================================================

  const routeEventToQueryClient = useCallback((
    event: RealtimeEvent,
    qc: ReturnType<typeof useQueryClient>
  ): void => {
    const { aggregateId } = event;
    
    // Use type guards for type-safe payload access
    if (isThreatDetectedEvent(event) || isThreatEscalatedEvent(event)) {
      // Threat events - invalidate threats list and spatial threats
      qc.invalidateQueries({ queryKey: ['threats'] });
      qc.invalidateQueries({ queryKey: ['threats', 'active'] });
      qc.invalidateQueries({ queryKey: ['spatial', 'threats'] });
      return;
    }
    
    if (isIncidentCreatedEvent(event) || isIncidentStateChangedEvent(event)) {
      // Incident events - invalidate incidents list and spatial incidents
      qc.invalidateQueries({ queryKey: ['incidents'] });
      qc.invalidateQueries({ queryKey: ['incidents', 'active'] });
      qc.invalidateQueries({ queryKey: ['spatial', 'incidents'] });
      if (isIncidentStateChangedEvent(event)) {
        qc.invalidateQueries({ queryKey: ['incidents', aggregateId] });
      }
      return;
    }
    
    if (isRiskScoreUpdatedEvent(event)) {
      // Risk score updated - invalidate asset risk and risk summaries
      const assetId = event.payload.assetId;
      if (assetId) {
        qc.invalidateQueries({ queryKey: ['risk', 'asset', assetId] });
      }
      qc.invalidateQueries({ queryKey: ['risk', 'summary'] });
      qc.invalidateQueries({ queryKey: ['spatial', 'risk'] });
      return;
    }
    
    if (isSimulationCompletedEvent(event)) {
      // Simulation completed - invalidate simulation results
      qc.invalidateQueries({ queryKey: ['simulations'] });
      qc.invalidateQueries({ queryKey: ['simulations', aggregateId] });
      qc.invalidateQueries({ queryKey: ['spatial', 'simulation', aggregateId] });
      return;
    }
    
    if (isResponsePlanUpdatedEvent(event)) {
      // Response plan updated - invalidate response plans
      qc.invalidateQueries({ queryKey: ['response-plans'] });
      qc.invalidateQueries({ queryKey: ['response-plans', aggregateId] });
      const incidentId = event.payload.incidentId;
      if (incidentId) {
        qc.invalidateQueries({ queryKey: ['incidents', incidentId, 'response-plan'] });
      }
      return;
    }
    
    if (isRecoveryProgressUpdatedEvent(event)) {
      // Recovery progress updated - invalidate recovery state
      qc.invalidateQueries({ queryKey: ['recovery', aggregateId] });
      qc.invalidateQueries({ queryKey: ['recovery'] });
      const incidentId = event.payload.incidentId;
      if (incidentId) {
        qc.invalidateQueries({ queryKey: ['incidents', incidentId, 'recovery'] });
      }
      return;
    }
    
    if (isGridAssetStatusChangedEvent(event)) {
      // Asset status changed - invalidate asset and spatial data
      qc.invalidateQueries({ queryKey: ['assets', aggregateId] });
      qc.invalidateQueries({ queryKey: ['assets'] });
      qc.invalidateQueries({ queryKey: ['spatial', 'assets'] });
      qc.invalidateQueries({ queryKey: ['risk', 'asset', aggregateId] });
      return;
    }
  }, []);

  // =========================================================================
  // Connection Management
  // =========================================================================

  const connect = useCallback(async (): Promise<void> => {
    if (!isAuthenticated || !accessToken) {
      console.warn('Cannot connect: not authenticated');
      return;
    }
    
    const client = getRealtimeClient();
    
    try {
      await client.connect(accessToken);
      client.subscribeToAll();
    } catch (error) {
      console.error('Failed to connect to realtime service:', error);
      throw error;
    }
  }, [isAuthenticated, accessToken]);

  const disconnect = useCallback((): void => {
    const client = getRealtimeClient();
    client.disconnect();
  }, []);

  const reconnect = useCallback(async (): Promise<void> => {
    resetRealtimeClient();
    await connect();
  }, [connect]);

  // =========================================================================
  // Event Handlers Registration
  // =========================================================================

  useEffect(() => {
    if (!isAuthenticated || !accessToken) {
      return;
    }

    const client = getRealtimeClient();
    
    // Register event handler
    const unsubscribeEvent = client.onEvent(processEvent);
    
    // Register connection state handler
    const unsubscribeConnection = client.onConnectionStateChange(setConnectionStatus);
    
    // Connect
    connect().catch(console.error);
    
    // Cleanup
    return () => {
      unsubscribeEvent();
      unsubscribeConnection();
      // Note: We don't disconnect here to keep connection alive across component unmounts
      // Use disconnect() explicitly when needed
    };
  }, [isAuthenticated, accessToken, processEvent, connect]);

  // Cleanup on auth change
  useEffect(() => {
    if (!isAuthenticated) {
      disconnect();
      // Clear history on logout
      setEventHistory([]);
      setLatestEvent(null);
      processedEventsRef.current = [];
      eventHistoryRef.current = [];
    }
  }, [isAuthenticated, disconnect]);

  // =========================================================================
  // Computed Values
  // =========================================================================

  const isConnected = connectionStatus.state === 'CONNECTED';
  const isReconnecting = connectionStatus.state === 'RECONNECTING';
  const isOffline = connectionStatus.state === 'DISCONNECTED' || connectionStatus.state === 'ERROR';
  
  // Get connection status display text
  const connectionDisplayText = useMemo((): string => {
    switch (connectionStatus.state) {
      case 'CONNECTED': return 'LIVE';
      case 'CONNECTING': return 'CONNECTING...';
      case 'RECONNECTING': return 'RECONNECTING...';
      case 'DISCONNECTED': return 'OFFLINE';
      case 'ERROR': return 'ERROR';
      default: return 'UNKNOWN';
    }
  }, [connectionStatus.state]);

  // Get active threats count from event history
  const activeThreatsCount = useMemo((): number => {
    return eventHistory.filter(e => 
      e.eventType === 'ThreatDetected' && 
      e.severity !== 'INFO' &&
      e.severity !== 'LOW'
    ).length;
  }, [eventHistory]);

  // Get active incidents count
  const activeIncidentsCount = useMemo((): number => {
    return eventHistory.filter(e => 
      e.eventType === 'IncidentCreated' ||
      (e.eventType === 'IncidentStateChanged' && 
       ['DETECTED', 'TRIAGED', 'CONFIRMED', 'ACTIVE_RESPONSE', 'CONTAINED', 'RECOVERING'].includes((e as any).payload?.newState))
    ).length;
  }, [eventHistory]);

  return {
    // Connection state
    connectionStatus,
    isConnected,
    isReconnecting,
    isOffline,
    connectionDisplayText,
    
    // Events
    eventHistory,
    latestEvent,
    activeThreatsCount,
    activeIncidentsCount,
    
    // Controls
    connect,
    disconnect,
    reconnect,
    
    // Internal (for testing)
    _processEvent: processEvent,
    _isEventProcessed: isEventProcessed,
  };
}