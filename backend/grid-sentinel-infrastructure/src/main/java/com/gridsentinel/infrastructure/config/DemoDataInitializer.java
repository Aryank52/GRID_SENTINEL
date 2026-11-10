package com.gridsentinel.infrastructure.config;

import com.gridsentinel.auth.Role;
import com.gridsentinel.auth.User;
import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.infrastructure.persistence.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

/**
 * Demo data initialization for development profile.
 * Creates demo users for testing.
 */
@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner initDemoUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only run in dev profile (when using H2 in-memory)
            if (!userRepository.findByUsername("commander").isPresent()) {
                // INCIDENT_COMMANDER
                User commander = User.builder()
                        .id(CommonKernel.UserId.generate())
                        .username("commander")
                        .email("commander@grid-sentinel.local")
                        .passwordHash(passwordEncoder.encode("demo123"))
                        .fullName("Incident Commander")
                        .roles(Set.of(Role.INCIDENT_COMMANDER))
                        .build();
                userRepository.save(commander);
            }

            if (!userRepository.findByUsername("dispatcher").isPresent()) {
                // DISPATCHER
                User dispatcher = User.builder()
                        .id(CommonKernel.UserId.generate())
                        .username("dispatcher")
                        .email("dispatcher@grid-sentinel.local")
                        .passwordHash(passwordEncoder.encode("demo123"))
                        .fullName("Grid Dispatcher")
                        .roles(Set.of(Role.DISPATCHER))
                        .build();
                userRepository.save(dispatcher);
            }

            if (!userRepository.findByUsername("analyst").isPresent()) {
                // RISK_ANALYST
                User analyst = User.builder()
                        .id(CommonKernel.UserId.generate())
                        .username("analyst")
                        .email("analyst@grid-sentinel.local")
                        .passwordHash(passwordEncoder.encode("demo123"))
                        .fullName("Risk Analyst")
                        .roles(Set.of(Role.RISK_ANALYST))
                        .build();
                userRepository.save(analyst);
            }

            if (!userRepository.findByUsername("viewer").isPresent()) {
                // VIEWER
                User viewer = User.builder()
                        .id(CommonKernel.UserId.generate())
                        .username("viewer")
                        .email("viewer@grid-sentinel.local")
                        .passwordHash(passwordEncoder.encode("demo123"))
                        .fullName("Read-only Viewer")
                        .roles(Set.of(Role.VIEWER))
                        .build();
                userRepository.save(viewer);
            }

            if (!userRepository.findByUsername("admin").isPresent()) {
                // ADMINISTRATOR
                User admin = User.builder()
                        .id(CommonKernel.UserId.generate())
                        .username("admin")
                        .email("admin@grid-sentinel.local")
                        .passwordHash(passwordEncoder.encode("demo123"))
                        .fullName("System Administrator")
                        .roles(Set.of(Role.ADMINISTRATOR))
                        .build();
                userRepository.save(admin);
            }
        };
    }
}