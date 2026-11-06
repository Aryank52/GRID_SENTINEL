package com.gridsentinel.infrastructure.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.gridsentinel.auth.User;

import java.util.List;
import java.util.Optional;

/**
 * STOMP channel interceptor for WebSocket authentication and authorization.
 * <p>
 * Validates JWT tokens on CONNECT and SUBSCRIBE commands.
 * Enforces role-based access to STOMP destinations.
 * </p>
 */
@Component
public class StompAuthenticationInterceptor implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    public StompAuthenticationInterceptor(UserDetailsService userDetailsService,
                                          JwtTokenProvider jwtTokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> handleConnect(accessor);
            case SUBSCRIBE -> handleSubscribe(accessor);
            case DISCONNECT -> handleDisconnect(accessor);
            default -> {}
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        String token = extractToken(authHeader);

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired JWT token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        accessor.setUser(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        var principal = accessor.getUser();
        if (principal == null || !(principal instanceof Authentication auth) || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated subscription attempt");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        // Check authorization for the destination
        if (!isAuthorizedForDestination(auth, destination)) {
            throw new SecurityException("Access denied to destination: " + destination);
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        // Clear security context on disconnect
        SecurityContextHolder.clearContext();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isAuthorizedForDestination(Authentication auth, String destination) {
        // Define destination access rules
        // All authenticated users can subscribe to general topics
        if (destination.startsWith("/topic/grid") ||
            destination.startsWith("/topic/threats") ||
            destination.startsWith("/topic/incidents") ||
            destination.startsWith("/topic/risk") ||
            destination.startsWith("/topic/recovery") ||
            destination.startsWith("/topic/simulation")) {
            
            return hasAnyRole(auth, 
                "OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER", 
                "RISK_ANALYST", "PLANNER", "RECOVERY_MANAGER",
                "SIMULATION_ENGINEER", "SECURITY_ANALYST",
                "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR",
                "VIEWER", "EXECUTIVE_VIEWER", "AUDITOR");
        }

        // User-specific destinations
        if (destination.startsWith("/user/")) {
            return true; // User can only subscribe to their own queue
        }

        return false;
    }

    private boolean hasAnyRole(Authentication auth, String... roles) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority();
                    for (String role : roles) {
                        if (authority.equals("ROLE_" + role)) {
                            return true;
                        }
                    }
                    return false;
                });
    }
}