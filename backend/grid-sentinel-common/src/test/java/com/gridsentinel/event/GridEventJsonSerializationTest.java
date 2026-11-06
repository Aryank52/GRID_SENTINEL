package com.gridsentinel.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gridsentinel.common.CommonKernel.CorrelationId;
import com.gridsentinel.common.EventSeverity;
import com.gridsentinel.common.GridEventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JSON serialization tests for grid events.
 * Verifies that events serialize to JSON correctly with proper field names,
 * no circular references, and correct type information.
 */
class GridEventJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void threatDetectedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new ThreatDetectedEvent(
                correlationId,
                "THR-001",
                "FIRE",
                "HIGH",
                0.95,
                Instant.parse("2026-01-15T10:30:00Z"),
                "ASSET-001",
                "NODE-001",
                "Wildfire detected near transmission line",
                Set.of("EVD-001", "EVD-002"),
                "thermal-detection-v1",
                "1.2.0",
                "detection-pipeline"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"ThreatDetected\"");
        assertThat(json).contains("\"THR-001\"");
        assertThat(json).contains("\"FIRE\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("0.95");
        assertThat(json).contains("\"2026-01-15T10:30:00Z\"");
        assertThat(json).contains("\"ASSET-001\"");
        assertThat(json).contains("\"NODE-001\"");
        assertThat(json).contains("Wildfire detected near transmission line");
        assertThat(json).contains("\"EVD-001\"");
        assertThat(json).contains("\"EVD-002\"");
        assertThat(json).contains("thermal-detection-v1");
        assertThat(json).contains("\"1.2.0\"");
        assertThat(json).contains("detection-pipeline");
        
        // Verify envelope fields
        assertThat(json).contains("\"eventId\"");
        assertThat(json).contains("\"occurredAt\"");
        assertThat(json).contains("\"correlationId\"");
        assertThat(json).contains("\"aggregateType\":\"ThreatEvent\"");
        assertThat(json).contains("\"aggregateId\":\"THR-001\"");
        assertThat(json).contains("\"version\":0");
        assertThat(json).contains("\"severity\":\"HIGH\"");
        assertThat(json).contains("\"source\":\"detection-pipeline\"");
    }

    @Test
    void threatEscalatedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new ThreatEscalatedEvent(
                correlationId,
                "THR-001",
                "MEDIUM",
                "CRITICAL",
                "Fire spread to adjacent assets",
                "threat-pipeline"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"ThreatEscalated\"");
        assertThat(json).contains("\"THR-001\"");
        assertThat(json).contains("\"MEDIUM\"");
        assertThat(json).contains("\"CRITICAL\"");
        assertThat(json).contains("Fire spread to adjacent assets");
        assertThat(json).contains("threat-pipeline");
    }

    @Test
    void incidentCreatedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new IncidentCreatedEvent(
                correlationId,
                "INC-001",
                "INC-2026-000123",
                "DETECTED",
                "HIGH",
                "THR-001",
                "ASSET-001",
                "NODE-001",
                "Wildfire threat to transmission corridor",
                "OPERATOR-001",
                "COMMANDER-001",
                "incident-management"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"IncidentCreated\"");
        assertThat(json).contains("\"INC-001\"");
        assertThat(json).contains("\"INC-2026-000123\"");
        assertThat(json).contains("\"DETECTED\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("\"THR-001\"");
        assertThat(json).contains("\"ASSET-001\"");
        assertThat(json).contains("\"NODE-001\"");
        assertThat(json).contains("Wildfire threat to transmission corridor");
        assertThat(json).contains("\"OPERATOR-001\"");
        assertThat(json).contains("\"COMMANDER-001\"");
    }

    @Test
    void incidentStateChangedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new IncidentStateChangedEvent(
                correlationId,
                "INC-001",
                "INC-2026-000123",
                "DETECTED",
                "CONFIRMED",
                "OPERATOR-001",
                "Confirmed by field crew",
                "incident-management"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"IncidentStateChanged\"");
        assertThat(json).contains("\"INC-001\"");
        assertThat(json).contains("\"INC-2026-000123\"");
        assertThat(json).contains("\"DETECTED\"");
        assertThat(json).contains("\"CONFIRMED\"");
        assertThat(json).contains("\"OPERATOR-001\"");
        assertThat(json).contains("Confirmed by field crew");
    }

    @Test
    void riskScoreUpdatedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new RiskScoreUpdatedEvent(
                correlationId,
                "RISK-001",
                "ASSET-001",
                0.45,
                0.78,
                "MEDIUM",
                "HIGH",
                "wildfire-risk-model",
                "2.1.0",
                "bayesian-inference",
                "risk-engine"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"RiskScoreUpdated\"");
        assertThat(json).contains("\"RISK-001\"");
        assertThat(json).contains("\"ASSET-001\"");
        assertThat(json).contains("0.45");
        assertThat(json).contains("0.78");
        assertThat(json).contains("\"MEDIUM\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("wildfire-risk-model");
        assertThat(json).contains("2.1.0");
        assertThat(json).contains("bayesian-inference");
    }

    @Test
    void simulationCompletedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new SimulationCompletedEvent(
                correlationId,
                "SIM-001",
                "Wildfire Impact Simulation",
                "SCENARIO-001",
                "COMPLETED",
                "cascade-failure-strategy",
                "1.0.0",
                12345L,
                null,
                "simulation-engine"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"SimulationCompleted\"");
        assertThat(json).contains("\"SIM-001\"");
        assertThat(json).contains("Wildfire Impact Simulation");
        assertThat(json).contains("\"SCENARIO-001\"");
        assertThat(json).contains("\"COMPLETED\"");
        assertThat(json).contains("cascade-failure-strategy");
        assertThat(json).contains("1.0.0");
        assertThat(json).contains("12345");
    }

    @Test
    void responsePlanUpdatedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new ResponsePlanUpdatedEvent(
                correlationId,
                "PLAN-001",
                "INC-001",
                "Wildfire Response Plan",
                "DRAFT",
                "APPROVED",
                "COMMANDER-001",
                "APPROVED",
                "response-planning"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"ResponsePlanUpdated\"");
        assertThat(json).contains("\"PLAN-001\"");
        assertThat(json).contains("\"INC-001\"");
        assertThat(json).contains("Wildfire Response Plan");
        assertThat(json).contains("\"DRAFT\"");
        assertThat(json).contains("\"APPROVED\"");
        assertThat(json).contains("\"COMMANDER-001\"");
        assertThat(json).contains("APPROVED");
    }

    @Test
    void recoveryProgressUpdatedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new RecoveryProgressUpdatedEvent(
                correlationId,
                "REC-001",
                "INC-001",
                "IN_PROGRESS",
                "COMPLETED",
                0.65,
                1.0,
                "MILESTONE-001",
                "Power restored to all customers",
                true,
                "recovery-management"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"RecoveryProgressUpdated\"");
        assertThat(json).contains("\"REC-001\"");
        assertThat(json).contains("\"INC-001\"");
        assertThat(json).contains("\"IN_PROGRESS\"");
        assertThat(json).contains("\"COMPLETED\"");
        assertThat(json).contains("0.65");
        assertThat(json).contains("1.0");
        assertThat(json).contains("\"MILESTONE-001\"");
        assertThat(json).contains("Power restored to all customers");
        assertThat(json).contains("true");
    }

    @Test
    void gridAssetStatusChangedEvent_serializesCorrectly() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new GridAssetStatusChangedEvent(
                correlationId,
                "ASSET-001",
                "Transmission Line 500kV #1",
                "TRANSMISSION_LINE",
                "HEALTHY",
                "DEGRADED",
                "MEDIUM",
                "HIGH",
                0.95,
                0.45,
                "LOW",
                "HIGH",
                "Thermal anomaly detected",
                "asset-management"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        assertThat(json).contains("\"eventType\":\"GridAssetStatusChanged\"");
        assertThat(json).contains("\"ASSET-001\"");
        assertThat(json).contains("Transmission Line 500kV #1");
        assertThat(json).contains("TRANSMISSION_LINE");
        assertThat(json).contains("\"HEALTHY\"");
        assertThat(json).contains("\"DEGRADED\"");
        assertThat(json).contains("\"MEDIUM\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("0.95");
        assertThat(json).contains("0.45");
        assertThat(json).contains("\"LOW\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("Thermal anomaly detected");
    }

    @Test
    void eventEnvelope_withAllFields_includesEventType() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var event = new ThreatDetectedEvent(
                correlationId,
                "THR-001",
                "FIRE",
                "HIGH",
                0.95,
                Instant.parse("2026-01-15T10:30:00Z"),
                "ASSET-001",
                "NODE-001",
                "Test threat",
                Set.of(),
                "test-strategy",
                "1.0.0",
                "test-source"
        );

        String json = objectMapper.writeValueAsString(event.toEnvelope());

        // Verify the envelope contains eventType at the top level (for frontend discrimination)
        assertThat(json).contains("\"eventType\":\"ThreatDetected\"");
    }

    @Test
    void eventDeserialization_roundTrip_preservesData() throws JsonProcessingException {
        var correlationId = CorrelationId.generate();
        var originalEvent = new ThreatDetectedEvent(
                correlationId,
                "THR-001",
                "FIRE",
                "HIGH",
                0.95,
                Instant.parse("2026-01-15T10:30:00Z"),
                "ASSET-001",
                "NODE-001",
                "Wildfire detected",
                Set.of("EVD-001"),
                "thermal-detection-v1",
                "1.2.0",
                "detection-pipeline"
        );

        String json = objectMapper.writeValueAsString(originalEvent.toEnvelope());
        
        // Verify JSON contains all expected fields (serialization test)
        assertThat(json).contains("\"eventType\":\"ThreatDetected\"");
        assertThat(json).contains("\"THR-001\"");
        assertThat(json).contains("\"FIRE\"");
        assertThat(json).contains("\"HIGH\"");
        assertThat(json).contains("0.95");
        assertThat(json).contains("\"2026-01-15T10:30:00Z\"");
        assertThat(json).contains("\"ASSET-001\"");
        assertThat(json).contains("\"NODE-001\"");
        assertThat(json).contains("Wildfire detected");
        assertThat(json).contains("\"EVD-001\"");
        assertThat(json).contains("thermal-detection-v1");
        assertThat(json).contains("\"1.2.0\"");
        assertThat(json).contains("detection-pipeline");
        
        // Deserialize to Map to verify structure (avoiding generic type erasure)
        @SuppressWarnings("unchecked")
        var map = objectMapper.readValue(json, java.util.Map.class);
        
        assertThat(map.get("eventType")).isEqualTo("ThreatDetected");
        assertThat(map.get("eventId")).isNotNull();
        assertThat(map.get("occurredAt")).isNotNull();
        assertThat(map.get("correlationId")).isNotNull();
        assertThat(map.get("aggregateType")).isEqualTo("ThreatEvent");
        assertThat(map.get("aggregateId")).isEqualTo("THR-001");
        assertThat(map.get("version")).isEqualTo(0);
        assertThat(map.get("severity")).isEqualTo("HIGH");
        assertThat(map.get("source")).isEqualTo("detection-pipeline");
        
        @SuppressWarnings("unchecked")
        var payload = (java.util.Map<String, Object>) map.get("payload");
        assertThat(payload).isNotNull();
        assertThat(payload.get("threatEventId")).isEqualTo("THR-001");
        assertThat(payload.get("threatType")).isEqualTo("FIRE");
        assertThat(payload.get("severity")).isEqualTo("HIGH");
        assertThat(payload.get("confidence")).isEqualTo(0.95);
        assertThat(payload.get("affectedAssetId")).isEqualTo("ASSET-001");
        assertThat(payload.get("nearbyGridNodeId")).isEqualTo("NODE-001");
        assertThat(payload.get("description")).isEqualTo("Wildfire detected");
        assertThat(payload.get("evidenceIds")).isEqualTo(java.util.List.of("EVD-001"));
        assertThat(payload.get("detectionStrategy")).isEqualTo("thermal-detection-v1");
        assertThat(payload.get("strategyVersion")).isEqualTo("1.2.0");
    }
}