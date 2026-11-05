package com.gridsentinel.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.gridsentinel.infrastructure.persistence.repository.UserRepository;
import com.gridsentinel.auth.User;

/**
 * Spring Security configuration with JWT-based authentication and RBAC.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**", "/actuator/health", "/actuator/info",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/assets/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "RISK_ANALYST", "PLANNER", "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR", "VIEWER", "EXECUTIVE_VIEWER")
                        .requestMatchers("/api/v1/grid/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "PLANNER", "SIMULATION_ENGINEER", "SUPERVISOR", "MANAGER", "ADMINISTRATOR", "VIEWER")
                        .requestMatchers("/api/v1/observations/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "RISK_ANALYST", "SECURITY_ANALYST", "SUPERVISOR", "MANAGER", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/risk/**").hasAnyRole("RISK_ANALYST", "PLANNER", "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR", "VIEWER", "EXECUTIVE_VIEWER")
                        .requestMatchers("/api/v1/threats/**", "/api/v1/detection/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "SECURITY_ANALYST", "SUPERVISOR", "MANAGER", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/incidents/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "RECOVERY_MANAGER", "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR", "VIEWER", "EXECUTIVE_VIEWER")
                        .requestMatchers("/api/v1/simulations/**").hasAnyRole("SIMULATION_ENGINEER", "PLANNER", "INCIDENT_COMMANDER",
                                "SUPERVISOR", "MANAGER", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/response/**").hasAnyRole("INCIDENT_COMMANDER", "OPERATOR", "DISPATCHER",
                                "RECOVERY_MANAGER", "SUPERVISOR", "MANAGER", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/recovery/**").hasAnyRole("RECOVERY_MANAGER", "INCIDENT_COMMANDER",
                                "OPERATOR", "SUPERVISOR", "MANAGER", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/protection/**").hasAnyRole("PLANNER", "RISK_ANALYST", "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR")
                        .requestMatchers("/api/v1/copilot/**").hasAnyRole("OPERATOR", "DISPATCHER", "INCIDENT_COMMANDER",
                                "RISK_ANALYST", "PLANNER", "SUPERVISOR", "MANAGER", "DIRECTOR", "ADMINISTRATOR", "VIEWER", "EXECUTIVE_VIEWER")
                        .requestMatchers("/api/v1/audit/**").hasAnyRole("AUDITOR", "ADMINISTRATOR", "DIRECTOR")
                        .requestMatchers("/actuator/**").hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private org.springframework.security.core.userdetails.UserDetails toUserDetails(com.gridsentinel.auth.User user) {
        return org.springframework.security.core.userdetails.User.builder()
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
    }
}