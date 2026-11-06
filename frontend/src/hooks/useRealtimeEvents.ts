/**
 * Real-time Event Handling Hook for Grid Sentinel
 * 
 * Provides:
 * - Event deduplication using bounded event ID cache
 * - TanStack Query cache invalidation/update
 * - Connection state management
 * - Centralized event routing
 */

import { useEffect, useCallback, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { getRealtimeClient, resetRealtimeClient } from '@services/realtimeClient';
import type {
  RealtimeEvent,
  ConnectionStatus,
  ConnectionState,
  EventHistoryEntry,
} from '@types/realtime';
import { useAuth } from './useAuth';

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
  const { accessToken } = useAuth();
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
  const isInitializedRef = useRef(false);
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
    const { eventType, payload, aggregateId } = event;
    
    switch (eventType) {
      case 'ThreatDetected': {
        // New threat - invalidate threats list and update map
        qc.invalidateQueries({ queryKey: ['threats'] });
        qc.invalidateQueries({ queryKey: ['threats', 'active'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'threats'] });
        break;
      }
      
      case 'ThreatEscalated': {
        // Threat severity changed - invalidate threats
        qc.invalidateQueries({ queryKey: ['threats'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'threats'] });
        // Could also update specific threat in cache if needed
        break;
      }
      
      case 'IncidentCreated': {
        // New incident - invalidate incidents list and active incidents
        qc.invalidateQueries({ queryKey: ['incidents'] });
        qc.invalidateQueries({ queryKey: ['incidents', 'active'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'incidents'] });
        break;
      }
      
      case 'IncidentStateChanged': {
        // Incident state changed - invalidate specific incident and lists
        qc.invalidateQueries({ queryKey: ['incidents', aggregateId] });
        qc.invalidateQueries({ queryKey: ['incidents'] });
        qc.invalidateQueries({ queryKey: ['incidents', 'active'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'incidents'] });
        break;
      }
      
      case 'RiskScoreUpdated': {
        // Risk score updated - invalidate asset risk and risk summaries
        const assetId = (payload as any).assetId;
        if (assetId) {
          qc.invalidateQueries({ queryKey: ['risk', 'asset', assetId] });
        }
        qc.invalidateQueries({ queryKey: ['risk', 'summary'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'risk'] });
        break;
      }
      
      case 'SimulationCompleted': {
        // Simulation completed - invalidate simulation results
        qc.invalidateQueries({ queryKey: ['simulations'] });
        qc.invalidateQueries({ queryKey: ['simulations', aggregateId] });
        qc.invalidateQueries({ queryKey: ['spatial', 'simulation', aggregateId] });
        break;
      }
      
      case 'ResponsePlanUpdated': {
        // Response plan updated - invalidate response plans
        qc.invalidateQueries({ queryKey: ['response-plans'] });
        qc.invalidateQueries({ queryKey: ['response-plans', aggregateId] });
        const incidentId = (payload as any).incidentId;
        if (incidentId) {
          qc.invalidateQueries({ queryKey: ['incidents', incidentId, 'response-plan'] });
        }
        break;
      }
      
      case 'RecoveryProgressUpdated': {
        // Recovery progress updated - invalidate recovery state
        qc.invalidateQueries({ queryKey: ['recovery', aggregateId] });
        qc.invalidateQueries({ queryKey: ['recovery'] });
        const incidentId = (payload as any).incidentId;
        if (incidentId) {
          qc.invalidateQueries({ queryKey: ['incidents', incidentId, 'recovery'] });
        }
        break;
      }
      
      case 'GridAssetStatusChanged': {
        // Asset status changed - invalidate asset and spatial data
        qc.invalidateQueries({ queryKey: ['assets', aggregateId] });
        qc.invalidateQueries({ queryKey: ['assets'] });
        qc.invalidateQueries({ queryKey: ['spatial', 'assets'] });
        qc.invalidateQueries({ queryKey: ['risk', 'asset', aggregateId] });
        break;
      }
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

// Need to import useMemo
import { useMemo } from 'react';

/**
 * Create a human-readable message from an event
 */
function createEventMessage(event: RealtimeEvent): string {
  const payload = event.payload as any;
  
  switch (event.eventType) {
    case 'ThreatDetected':
      return `Threat detected: ${payload.threatType} (${payload.severity}) - ${payload.description}`;
    case 'ThreatEscalated':
      return `Threat escalated: ${payload.previousSeverity} → ${payload.newSeverity} - ${payload.escalationReason}`;
    case 'IncidentCreated':
      return `Incident created: ${payload.incidentNumber} (${payload.state}) - ${payload.description}`;
    case 'IncidentStateChanged':
      return `Incident ${payload.incidentNumber}: ${payload.previousState} → ${payload.newState}`;
    case 'RiskScoreUpdated':
      return `Risk score updated for asset ${payload.assetId}: ${payload.previousScore?.toFixed(2) || 'N/A'} → ${payload.newScore?.toFixed(2)} (${payload.newBand})`;
    case 'SimulationCompleted':
      return `Simulation completed: ${payload.name} (${payload.status})`;
    case 'ResponsePlanUpdated':
      return `Response plan ${payload.action}: ${payload.name} (${payload.newStatus})`;
    case 'RecoveryProgressUpdated':
      return `Recovery progress: ${payload.previousPhase} → ${payload.newPhase} (${(payload.newProgress * 100).toFixed(0)}%)`;
    case 'GridAssetStatusChanged':
      return `Asset status changed: ${payload.assetName} (${payload.assetType}) - ${payload.previousHealthStatus} → ${payload.newHealthStatus}`;
    default:
      return `${event.eventType} event for ${event.aggregateType} ${event.aggregateId}`;
  }
}