package com.gridsentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Grid Sentinel Main Application Entry Point.
 * <p>
 * Grid Resilience & Physical Protection of Energy Infrastructure.
 * Implements the closed operational loop: PREVENT → DETECT → ASSESS → SIMULATE → MITIGATE → RECOVER → LEARN.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {
        "com.gridsentinel.infrastructure",
        "com.gridsentinel.application",
        "com.gridsentinel.api"
})
@EntityScan(basePackages = {
        "com.gridsentinel.asset",
        "com.gridsentinel.grid",
        "com.gridsentinel.observation",
        "com.gridsentinel.risk",
        "com.gridsentinel.detection",
        "com.gridsentinel.threat",
        "com.gridsentinel.incident",
        "com.gridsentinel.simulation",
        "com.gridsentinel.response",
        "com.gridsentinel.recovery",
        "com.gridsentinel.protection",
        "com.gridsentinel.copilot",
        "com.gridsentinel.audit",
        "com.gridsentinel.auth",
        "com.gridsentinel.user"
})
@EnableJpaRepositories(basePackages = "com.gridsentinel.infrastructure.persistence.repository")
@EnableAsync
@EnableScheduling
public class GridSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridSentinelApplication.class, args);
    }
}