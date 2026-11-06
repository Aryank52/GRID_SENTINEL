/**
 * Real-time WebSocket Client for Grid Sentinel
 * 
 * Manages STOMP over WebSocket connection with:
 * - Authentication via JWT
 * - Automatic reconnection with exponential backoff
 * - Heartbeat support
 * - Event subscription management
 * - Connection state exposure
 */

import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type {
  RealtimeEvent,
  ConnectionState,
  ConnectionStatus,
} from '../types/realtime';
import {
  STOMP_DESTINATIONS,
  STOMP_ENDPOINT,
} from '../types/realtime';

type EventHandler = (event: RealtimeEvent) => void;
type ConnectionStateHandler = (status: ConnectionStatus) => void;

// Configuration constants
const RECONNECT_BASE_DELAY_MS = 1000;
const RECONNECT_MAX_DELAY_MS = 30000;
const RECONNECT_MAX_ATTEMPTS = 10;
const HEARTBEAT_INTERVAL_MS = 10000;
const CONNECTION_TIMEOUT_MS = 10000;

export class RealtimeClient {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private eventHandlers: Set<EventHandler> = new Set();
  private connectionStateHandlers: Set<ConnectionStateHandler> = new Set();
  
  private connectionStatus: ConnectionStatus = {
    state: 'DISCONNECTED',
    lastConnectedAt: null,
    lastDisconnectedAt: null,
    reconnectAttempts: 0,
    error: null,
  };
  
  private reconnectAttempts = 0;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private isIntentionallyDisconnected = false;
  private stompEndpoint: string;
  private jwtToken: string | null = null;

  constructor(stompEndpoint: string = STOMP_ENDPOINT) {
    this.stompEndpoint = stompEndpoint;
  }

  // =========================================================================
  // Public API
  // =========================================================================

  /**
   * Connect to the WebSocket/STOMP endpoint with JWT authentication
   */
  connect(jwtToken: string): Promise<void> {
    this.jwtToken = jwtToken;
    this.isIntentionallyDisconnected = false;
    
    return new Promise((resolve, reject) => {
      if (this.client?.connected) {
        resolve();
        return;
      }

      this.createClient();
      
      // Set up connection timeout
      const timeoutId = setTimeout(() => {
        if (this.connectionStatus.state === 'CONNECTING') {
          this.handleError(new Error('Connection timeout'));
          reject(new Error('Connection timeout'));
        }
      }, CONNECTION_TIMEOUT_MS);

      // Wait for connection
      const checkConnection = () => {
        if (this.connectionStatus.state === 'CONNECTED') {
          clearTimeout(timeoutId);
          resolve();
        } else if (this.connectionStatus.state === 'ERROR' || this.connectionStatus.state === 'DISCONNECTED') {
          clearTimeout(timeoutId);
          reject(new Error(this.connectionStatus.error || 'Connection failed'));
        } else {
          setTimeout(checkConnection, 100);
        }
      };
      
      checkConnection();
    });
  }

  /**
   * Disconnect from the WebSocket/STOMP endpoint
   */
  disconnect(): void {
    this.isIntentionallyDisconnected = true;
    this.clearReconnectTimer();
    
    if (this.client) {
      // Unsubscribe from all topics
      this.subscriptions.forEach((subscription) => {
        try {
          subscription.unsubscribe();
        } catch (e) {
          console.warn('Failed to unsubscribe:', e);
        }
      });
      this.subscriptions.clear();
      
      this.client.deactivate();
      this.client = null;
    }
    
    this.updateConnectionState('DISCONNECTED', null);
  }

  /**
   * Subscribe to a STOMP destination
   */
  subscribe(destination: string): void {
    if (!this.client?.connected) {
      console.warn(`Cannot subscribe to ${destination}: not connected`);
      return;
    }

    if (this.subscriptions.has(destination)) {
      console.debug(`Already subscribed to ${destination}`);
      return;
    }

    try {
      const subscription = this.client.subscribe(destination, (message: IMessage) => {
        this.handleMessage(message, destination);
      });
      
      this.subscriptions.set(destination, subscription);
      console.debug(`Subscribed to ${destination}`);
    } catch (error) {
      console.error(`Failed to subscribe to ${destination}:`, error);
    }
  }

  /**
   * Unsubscribe from a STOMP destination
   */
  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      try {
        subscription.unsubscribe();
      } catch (e) {
        console.warn(`Failed to unsubscribe from ${destination}:`, e);
      }
      this.subscriptions.delete(destination);
      console.debug(`Unsubscribed from ${destination}`);
    }
  }

  /**
   * Subscribe to all default operational topics
   */
  subscribeToAll(): void {
    Object.values(STOMP_DESTINATIONS).forEach(dest => this.subscribe(dest));
  }

  /**
   * Register an event handler for real-time events
   */
  onEvent(handler: EventHandler): () => void {
    this.eventHandlers.add(handler);
    return () => this.eventHandlers.delete(handler);
  }

  /**
   * Register a connection state change handler
   */
  onConnectionStateChange(handler: ConnectionStateHandler): () => void {
    this.connectionStateHandlers.add(handler);
    // Immediately notify of current state
    handler(this.connectionStatus);
    return () => this.connectionStateHandlers.delete(handler);
  }

  /**
   * Get current connection status
   */
  getConnectionStatus(): ConnectionStatus {
    return { ...this.connectionStatus };
  }

  /**
   * Check if currently connected
   */
  isConnected(): boolean {
    return this.connectionStatus.state === 'CONNECTED';
  }

  // =========================================================================
  // Private Methods
  // =========================================================================

  private createClient(): void {
    this.client = new Client({
      // Use SockJS for fallback support
      webSocketFactory: () => new SockJS(this.stompEndpoint),
      
      // Connection headers with JWT auth
      connectHeaders: this.jwtToken ? {
        Authorization: `Bearer ${this.jwtToken}`,
      } : {},
      
      // Heartbeat configuration
      heartbeatIncoming: HEARTBEAT_INTERVAL_MS,
      heartbeatOutgoing: HEARTBEAT_INTERVAL_MS,
      
      // Reconnection handled manually (more control)
      reconnectDelay: 0,
      
      // Callbacks
      onConnect: (frame) => this.handleConnect(frame),
      onDisconnect: (frame) => this.handleDisconnect(frame),
      onStompError: (frame) => this.handleStompError(frame),
      onWebSocketError: (error) => this.handleWebSocketError(error),
      onWebSocketClose: (event) => this.handleWebSocketClose(event),
    });

    this.updateConnectionState('CONNECTING', null);
    this.client.activate();
  }

  private handleConnect(frame: any): void {
    console.debug('STOMP connected:', frame);
    this.reconnectAttempts = 0;
    this.updateConnectionState('CONNECTED', null);
    
    // Re-subscribe to all topics after reconnect
    this.subscriptions.forEach((_, destination) => {
      this.subscribe(destination);
    });
  }

  private handleDisconnect(frame: any): void {
    console.debug('STOMP disconnected:', frame);
    const wasConnected = this.connectionStatus.state === 'CONNECTED';
    this.updateConnectionState('DISCONNECTED', wasConnected ? 'Server disconnected' : null);
    
    if (!this.isIntentionallyDisconnected && wasConnected) {
      this.scheduleReconnect();
    }
  }

  private handleStompError(frame: any): void {
    const error = frame.headers?.message || 'STOMP error';
    console.error('STOMP error:', error, frame);
    this.handleError(new Error(error));
  }

  private handleWebSocketError(error: Event): void {
    console.error('WebSocket error:', error);
    this.handleError(new Error('WebSocket error'));
  }

  private handleWebSocketClose(event: CloseEvent): void {
    console.debug('WebSocket closed:', event.code, event.reason);
    if (!this.isIntentionallyDisconnected) {
      this.handleError(new Error(`WebSocket closed: ${event.code} ${event.reason}`));
    }
  }

  private handleError(error: Error): void {
    this.updateConnectionState('ERROR', error.message);
    
    if (!this.isIntentionallyDisconnected) {
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= RECONNECT_MAX_ATTEMPTS) {
      console.error('Max reconnection attempts reached');
      this.updateConnectionState('ERROR', 'Max reconnection attempts reached');
      return;
    }

    this.reconnectAttempts++;
    const delay = Math.min(
      RECONNECT_BASE_DELAY_MS * Math.pow(2, this.reconnectAttempts - 1),
      RECONNECT_MAX_DELAY_MS
    );
    
    // Add jitter
    const jitteredDelay = delay + Math.random() * 1000;
    
    console.debug(`Scheduling reconnect attempt ${this.reconnectAttempts} in ${jitteredDelay}ms`);
    this.updateConnectionState('RECONNECTING', null);
    
    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      if (!this.isIntentionallyDisconnected && this.jwtToken) {
        this.connect(this.jwtToken).catch((err) => {
          console.error('Reconnection failed:', err);
        });
      }
    }, jitteredDelay);
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  private handleMessage(message: IMessage, _destination: string): void {
    try {
      const event = JSON.parse(message.body) as RealtimeEvent;
      
      // Validate event structure
      if (!this.validateEvent(event)) {
        console.warn('Received invalid event:', event);
        return;
      }
      
      // Notify all handlers
      this.eventHandlers.forEach(handler => {
        try {
          handler(event);
        } catch (handlerError) {
          console.error('Event handler error:', handlerError);
        }
      });
    } catch (parseError) {
      console.error('Failed to parse event message:', parseError, message.body);
    }
  }

  private validateEvent(event: any): event is RealtimeEvent {
    return (
      event &&
      typeof event === 'object' &&
      typeof event.eventId === 'string' &&
      typeof event.eventType === 'string' &&
      typeof event.occurredAt === 'string' &&
      event.correlationId &&
      typeof event.aggregateType === 'string' &&
      typeof event.aggregateId === 'string' &&
      typeof event.version === 'number' &&
      typeof event.severity === 'string' &&
      event.payload &&
      typeof event.source === 'string'
    );
  }

  private updateConnectionState(state: ConnectionState, error: string | null): void {
    const now = new Date().toISOString();
    
    this.connectionStatus = {
      ...this.connectionStatus,
      state,
      error,
      lastConnectedAt: state === 'CONNECTED' ? now : this.connectionStatus.lastConnectedAt,
      lastDisconnectedAt: state === 'DISCONNECTED' || state === 'ERROR' ? now : this.connectionStatus.lastDisconnectedAt,
      reconnectAttempts: this.reconnectAttempts,
    };
    
    // Notify all connection state handlers
    this.connectionStateHandlers.forEach(handler => {
      try {
        handler(this.connectionStatus);
      } catch (handlerError) {
        console.error('Connection state handler error:', handlerError);
      }
    });
  }
}

// Singleton instance
let realtimeClientInstance: RealtimeClient | null = null;

/**
 * Get or create the singleton RealtimeClient instance
 */
export function getRealtimeClient(): RealtimeClient {
  if (!realtimeClientInstance) {
    realtimeClientInstance = new RealtimeClient();
  }
  return realtimeClientInstance;
}

/**
 * Reset the singleton instance (for testing or re-authentication)
 */
export function resetRealtimeClient(): void {
  if (realtimeClientInstance) {
    realtimeClientInstance.disconnect();
    realtimeClientInstance = null;
  }
}