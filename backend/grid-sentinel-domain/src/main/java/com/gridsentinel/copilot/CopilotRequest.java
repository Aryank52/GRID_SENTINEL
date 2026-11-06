package com.gridsentinel.copilot;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Request to the AI Copilot for grounded assistance.
 */
@Entity
@Table(name = "copilot_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CopilotRequest {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private String requestId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private RequestType type;

    @NotNull
    @Size(max = 4000)
    @Setter(AccessLevel.PRIVATE)
    private String prompt; // User's question or task

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "incidentId.value", column = @Column(name = "grounding_incident_id")),
        @AttributeOverride(name = "assetId.value", column = @Column(name = "grounding_asset_id")),
        @AttributeOverride(name = "simulationId.value", column = @Column(name = "grounding_simulation_id")),
        @AttributeOverride(name = "riskScoreId.value", column = @Column(name = "grounding_risk_score_id")),
        @AttributeOverride(name = "threatEventId.value", column = @Column(name = "grounding_threat_event_id"))
    })
    @Setter(AccessLevel.PRIVATE)
    private GroundingContext groundingContext; // Structured system state for grounding

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId requestedBy;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    private CopilotRequest(Builder builder) {
        this.requestId = "COP-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        this.type = Objects.requireNonNull(builder.type, "Request type cannot be null");
        this.prompt = Objects.requireNonNull(builder.prompt, "Prompt cannot be null");
        this.groundingContext = builder.groundingContext;
        this.requestedBy = builder.requestedBy;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CopilotRequest)) return false;
        CopilotRequest that = (CopilotRequest) o;
        return requestId != null && requestId.equals(that.requestId);
    }

    @Override
    public int hashCode() {
        return requestId != null ? requestId.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum RequestType {
        SUMMARIZE_INCIDENT,
        EXPLAIN_RISK,
        EXPLAIN_SIMULATION,
        DRAFT_INCIDENT_REPORT,
        ANSWER_QUESTION,
        SUGGEST_ACTIONS,
        COMPARE_SCENARIOS
    }

    public static final class Builder {
        private RequestType type;
        private String prompt;
        private GroundingContext groundingContext;
        private CommonKernel.UserId requestedBy;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder type(RequestType type) {
            this.type = type;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder groundingContext(GroundingContext groundingContext) {
            this.groundingContext = groundingContext;
            return this;
        }

        public Builder requestedBy(CommonKernel.UserId requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public CopilotRequest build() {
            return new CopilotRequest(this);
        }
    }
}