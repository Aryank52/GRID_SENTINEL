package com.gridsentinel.infrastructure.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.GridEventEnvelope;
import com.gridsentinel.event.GridEvent;
import com.gridsentinel.event.RealTimeEventPublisher;

/**
 * Infrastructure implementation of RealTimeEventPublisher using Spring STOMP.
 * Publishes grid events to appropriate STOMP topics based on event type.
 */
@Component
public class StompRealTimeEventPublisher implements RealTimeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StompRealTimeEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    @Autowired
    public StompRealTimeEventPublisher(SimpMessagingTemplate messagingTemplate,
                                        SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    @Override
    public void publish(GridEvent event, CorrelationId correlationId) {
        publish(event, correlationId, event.source());
    }

    @Override
    public void publish(GridEvent event, CorrelationId correlationId, String source) {
        String destination = resolveDestination(event);
        GridEventEnvelope envelope = event.toEnvelope();
        
        // Override source if provided
        GridEventEnvelope finalEnvelope = source != null && !source.equals(event.source())
                ? envelope.withSource(source)
                : envelope;

        try {
            messagingTemplate.convertAndSend(destination, finalEnvelope);
            log.debug("Published event {} to destination {} (correlationId: {})",
                    event.eventType(), destination, correlationId);
        } catch (Exception e) {
            log.error("Failed to publish event {} to destination {}: {}",
                    event.eventType(), destination, e.getMessage(), e);
            throw new RuntimeException("Failed to publish real-time event", e);
        }
    }

    @Override
    public boolean isConnected() {
        // Check if there are any connected users/sessions
        return userRegistry.getUserCount() > 0;
    }

    @Override
    public ConnectionState getConnectionState() {
        // In a simple broker, we consider it connected if there are active sessions
        // For a more robust implementation, this could check the broker connection status
        return userRegistry.getUserCount() > 0 ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED;
    }

    /**
     * Resolves the STOMP destination topic based on the event type.
     * 
     * @param event the grid event
     * @return the STOMP destination topic
     */
    private String resolveDestination(GridEvent event) {
        return switch (event.eventType()) {
            case "ThreatDetected", "ThreatEscalated" -> "/topic/threats";
            case "IncidentCreated", "IncidentStateChanged" -> "/topic/incidents";
            case "RiskScoreUpdated" -> "/topic/risk";
            case "SimulationCompleted" -> "/topic/simulation";
            case "ResponsePlanUpdated" -> "/topic/incidents"; // Response plans are incident-related
            case "RecoveryProgressUpdated" -> "/topic/recovery";
            case "GridAssetStatusChanged" -> "/topic/grid";
            default -> "/topic/grid";
        };
    }
}