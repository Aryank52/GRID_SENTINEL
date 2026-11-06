package com.gridsentinel.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket/STOMP configuration for real-time grid events.
 * <p>
 * Configures STOMP over WebSocket with topic-based routing for operational events.
 * Destinations:
 * <ul>
 *   <li>/topic/grid - General grid events</li>
 *   <li>/topic/threats - Threat detection and escalation events</li>
 *   <li>/topic/incidents - Incident lifecycle events</li>
 *   <li>/topic/risk - Risk score updates</li>
 *   <li>/topic/recovery - Recovery progress events</li>
 *   <li>/topic/simulation - Simulation completion events</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthenticationInterceptor stompAuthenticationInterceptor;

    private static final String[] ALLOWED_ORIGINS = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://grid-sentinel.example.com"
    };

    public WebSocketConfig(StompAuthenticationInterceptor stompAuthenticationInterceptor) {
        this.stompAuthenticationInterceptor = stompAuthenticationInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker for topic subscriptions
        registry.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/realtime")
                .setAllowedOrigins(ALLOWED_ORIGINS)
                .withSockJS(); // Fallback for environments without WebSocket support
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthenticationInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthenticationInterceptor);
    }
}