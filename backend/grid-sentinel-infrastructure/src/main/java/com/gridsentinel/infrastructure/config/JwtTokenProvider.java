package com.gridsentinel.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

/**
 * JWT token provider for WebSocket/STOMP authentication.
 * Reuses the same signing key and validation logic as the HTTP filter.
 */
@Component
public class JwtTokenProvider {

    private final Key signingKey;
    private final String issuer;

    public JwtTokenProvider(@Value("${app.jwt.secret:grid-sentinel-development-secret-key-at-least-32-chars-long-for-hmac-sha256}") String secret,
                            @Value("${app.jwt.issuer:grid-sentinel}") String issuer) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    /**
     * Validates a JWT token.
     * 
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the username (subject) from a JWT token.
     * 
     * @param token the JWT token
     * @return the username, or null if invalid
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts the roles from a JWT token.
     * 
     * @param token the JWT token
     * @return the list of roles, or empty list if invalid
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("roles", List.class);
        } catch (JwtException | IllegalArgumentException e) {
            return List.of();
        }
    }
}