package com.gridsentinel.audit;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable audit log entry for governance and compliance.
 * Written once, never modified. Includes correlation ID for traceability.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AuditLogId id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private AuditEventType eventType;

    @NotNull
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String aggregateType; // e.g., "Incident", "Asset", "Simulation"

    @NotNull
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String aggregateId; // ID of the affected aggregate

    @NotNull
    @Size(max = 500)
    @Setter(AccessLevel.PRIVATE)
    private String action; // What was done: "CREATE", "UPDATE", "TRANSITION", "APPROVE", etc.

    @Setter(AccessLevel.PRIVATE)
    private String details; // JSON with before/after or relevant context

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId actorId; // Who performed the action

    @Setter(AccessLevel.PRIVATE)
    private String actorRole; // Role of actor at time of action

    @Setter(AccessLevel.PRIVATE)
    private String actorIp; // Source IP

    @Setter(AccessLevel.PRIVATE)
    private String userAgent; // Client user agent

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId; // For tracing related actions

    @Setter(AccessLevel.PRIVATE)
    private String causationId; // ID of event that caused this (e.g., threat ID that caused incident)

    @Setter(AccessLevel.PRIVATE)
    private Boolean aiAssisted; // Whether AI Copilot was involved

    @Setter(AccessLevel.PRIVATE)
    private String aiModelVersion; // If AI-assisted, which model

    @Setter(AccessLevel.PRIVATE)
    private String aiRequestId; // Reference to copilot_requests

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Instant timestamp;

    @Setter(AccessLevel.PRIVATE)
    private String hash; // Hash of this entry for tamper evidence (chained)

    private AuditLog(Builder builder) {
        this.id = builder.id;
        this.eventType = Objects.requireNonNull(builder.eventType, "Event type cannot be null");
        this.aggregateType = Objects.requireNonNull(builder.aggregateType, "Aggregate type cannot be null");
        this.aggregateId = Objects.requireNonNull(builder.aggregateId, "Aggregate ID cannot be null");
        this.action = Objects.requireNonNull(builder.action, "Action cannot be null");
        this.details = builder.details;
        this.actorId = builder.actorId;
        this.actorRole = builder.actorRole;
        this.actorIp = builder.actorIp;
        this.userAgent = builder.userAgent;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.causationId = builder.causationId;
        this.aiAssisted = builder.aiAssisted != null ? builder.aiAssisted : false;
        this.aiModelVersion = builder.aiModelVersion;
        this.aiRequestId = builder.aiRequestId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.hash = computeHash();
    }

    private String computeHash() {
        String content = aggregateType + "|" + aggregateId + "|" + action + "|" +
                (details != null ? details : "") + "|" +
                (actorId != null ? actorId.getValue().toString() : "") + "|" +
                timestamp.toString() + "|" +
                (correlationId != null ? correlationId.getValue().toString() : "");
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute audit hash", e);
        }
    }

    public boolean verifyHash() {
        return hash.equals(computeHash());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog)) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && id.equals(auditLog.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum AuditEventType {
        // Asset lifecycle
        ASSET_CREATED,
        ASSET_UPDATED,
        ASSET_DECOMMISSIONED,
        ASSET_HEALTH_CHANGED,
        ASSET_CRITICALITY_CHANGED,

        // Grid topology
        GRID_NODE_CREATED,
        GRID_NODE_UPDATED,
        GRID_EDGE_CREATED,
        GRID_EDGE_UPDATED,
        TOPOLOGY_SNAPSHOT_TAKEN,

        // Observation & Detection
        OBSERVATION_RECEIVED,
        OBSERVATION_VALIDATED,
        THREAT_DETECTED,
        THREAT_TRIAGED,
        THREAT_ESCALATED,
        THREAT_DISMISSED,

        // Incident lifecycle
        INCIDENT_CREATED,
        INCIDENT_TRIAGED,
        INCIDENT_CONFIRMED,
        INCIDENT_RESPONSE_STARTED,
        INCIDENT_CONTAINED,
        INCIDENT_RECOVERING,
        INCIDENT_RESOLVED,
        INCIDENT_CLOSED,
        INCIDENT_CANCELLED,

        // Response
        RESPONSE_PLAN_CREATED,
        RESPONSE_PLAN_APPROVED,
        RESPONSE_ACTION_STARTED,
        RESPONSE_ACTION_COMPLETED,
        RESPONSE_ACTION_FAILED,

        // Recovery
        RECOVERY_STARTED,
        RECOVERY_MILESTONE_COMPLETED,
        RECOVERY_MILESTONE_VERIFIED,
        RECOVERY_COMPLETED,
        LESSONS_LEARNED_RECORDED,

        // Simulation
        SIMULATION_REQUESTED,
        SIMULATION_STARTED,
        SIMULATION_COMPLETED,
        SIMULATION_FAILED,

        // Protection
        PROTECTION_PLAN_CREATED,
        PROTECTION_PLAN_APPROVED,
        PROTECTION_MEASURE_IMPLEMENTED,

        // Risk
        RISK_SCORE_COMPUTED,
        RISK_THRESHOLD_EXCEEDED,

        // Copilot
        COPILOT_REQUESTED,
        COPILOT_RESPONSE_GENERATED,
        COPILOT_RESPONSE_REVIEWED,

        // Security
        USER_LOGIN,
        USER_LOGOUT,
        PERMISSION_DENIED,
        ROLE_CHANGED,
        DATA_EXPORTED,

        // System
        CONFIGURATION_CHANGED,
        MODEL_DEPLOYED,
        MODEL_ROLLED_BACK
    }

    public static final class Builder {
        private CommonKernel.AuditLogId id = CommonKernel.AuditLogId.generate();
        private AuditEventType eventType;
        private String aggregateType;
        private String aggregateId;
        private String action;
        private String details;
        private CommonKernel.UserId actorId;
        private String actorRole;
        private String actorIp;
        private String userAgent;
        private CommonKernel.CorrelationId correlationId;
        private String causationId;
        private Boolean aiAssisted;
        private String aiModelVersion;
        private String aiRequestId;
        private Instant timestamp;

        private Builder() {}

        public Builder id(CommonKernel.AuditLogId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder eventType(AuditEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public Builder actorId(CommonKernel.UserId actorId) {
            this.actorId = actorId;
            return this;
        }

        public Builder actorRole(String actorRole) {
            this.actorRole = actorRole;
            return this;
        }

        public Builder actorIp(String actorIp) {
            this.actorIp = actorIp;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder aiAssisted(boolean aiAssisted) {
            this.aiAssisted = aiAssisted;
            return this;
        }

        public Builder aiModelVersion(String aiModelVersion) {
            this.aiModelVersion = aiModelVersion;
            return this;
        }

        public Builder aiRequestId(String aiRequestId) {
            this.aiRequestId = aiRequestId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}