package com.gridsentinel.infrastructure.api.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gridsentinel.infrastructure.persistence.repository.UserRepository;
import com.gridsentinel.auth.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Authentication controller for JWT-based authentication.
 * Provides login, token refresh, and user info endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    
    @Value("${app.jwt.secret:grid-sentinel-development-secret-key-at-least-32-chars-long-for-hmac-sha256}")
    private String jwtSecret;
    
    @Value("${app.jwt.issuer:grid-sentinel}")
    private String jwtIssuer;
    
    @Value("${app.jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    private Key signingKey;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // Authenticate user
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Invalid username or password"
            ));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Get user from database for additional info
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        // Generate tokens
        String accessToken = generateAccessToken(userDetails, user);
        String refreshToken = generateRefreshToken(userDetails.getUsername());

        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken,
                "token_type", "Bearer",
                "expires_in", accessTokenExpiration
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        try {
            var claims = Jwts.parser()
                    .verifyWith((javax.crypto.SecretKey) signingKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String username = claims.getSubject();
            
            // Verify it's a refresh token
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", "invalid_token",
                        "message", "Invalid token type"
                ));
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPasswordHash())
                    .authorities(user.getRoles().stream()
                            .map(r -> "ROLE_" + r.name())
                            .toArray(String[]::new))
                    .accountExpired(false)
                    .accountLocked(user.isAccountLocked())
                    .credentialsExpired(false)
                    .disabled(!user.isEnabled())
                    .build();

            String newAccessToken = generateAccessToken(userDetails, user);
            String newRefreshToken = generateRefreshToken(username);

            return ResponseEntity.ok(Map.of(
                    "access_token", newAccessToken,
                    "refresh_token", newRefreshToken,
                    "token_type", "Bearer",
                    "expires_in", accessTokenExpiration
            ));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_token",
                    "message", "Invalid or expired refresh token"
            ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        // In a stateless JWT setup, logout is typically handled client-side
        // by removing the tokens. Server-side we could maintain a blocklist if needed.
        return ResponseEntity.ok(Map.of(
                "message", "Logged out successfully"
        ));
    }

    @PostMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "unauthorized",
                    "message", "Not authenticated"
            ));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toList()),
                "enabled", user.getEnabled(),
                "mfaEnabled", user.getMfaEnabled()
        ));
    }

    private String generateAccessToken(UserDetails userDetails, User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenExpiration);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(jwtIssuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("userId", user.getId().toString())
                .claim("type", "access")
                .signWith(signingKey)
                .compact();
    }

    private String generateRefreshToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuer(jwtIssuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .signWith(signingKey)
                .compact();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
}