package com.gridsentinel.event;

import com.gridsentinel.common.CommonKernel.CorrelationId;

/**
 * Application-level port for publishing real-time grid events.
 * This preserves OOAD, dependency inversion, testability, and transport independence.
 * The application layer depends on this port; infrastructure implements the actual WebSocket transport.
 */
public interface RealTimeEventPublisher {

    /**
     * Publishes a grid event to the real-time event stream.
     * 
     * @param event the grid event to publish
     * @param correlationId the correlation ID for tracing
     */
    void publish(GridEvent event, CorrelationId correlationId);

    /**
     * Publishes a grid event to the real-time event stream with a specific source.
     * 
     * @param event the grid event to publish
     * @param correlationId the correlation ID for tracing
     * @param source the source of the event (e.g., "detection-pipeline", "incident-management")
     */
    void publish(GridEvent event, CorrelationId correlationId, String source);

    /**
     * Checks if the real-time event publisher is connected and ready to publish.
     * 
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Gets the connection state of the real-time event publisher.
     * 
     * @return the connection state
     */
    ConnectionState getConnectionState();

    /**
     * Connection state of the real-time event publisher.
     */
    enum ConnectionState {
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTED,
        ERROR
    }
}