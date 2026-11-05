package com.gridsentinel.copilot;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Structured system state provided as grounding context for AI responses.
 * Contains only factual, verifiable data from the system - no LLM-generated content.
 */
@Embeddable
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroundingContext {

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.IncidentId incidentId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId assetId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.SimulationId simulationId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.RiskScoreId riskScoreId;

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ThreatEventId threatEventId;

    @ElementCollection
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> structuredData = new HashMap<>(); // Key-value pairs of system facts

    private GroundingContext(Builder builder) {
        this.incidentId = builder.incidentId;
        this.assetId = builder.assetId;
        this.simulationId = builder.simulationId;
        this.riskScoreId = builder.riskScoreId;
        this.threatEventId = builder.threatEventId;
        this.structuredData = Map.copyOf(builder.structuredData);
    }

    public Map<String, String> getStructuredData() {
        return Collections.unmodifiableMap(structuredData);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.IncidentId incidentId;
        private CommonKernel.AssetId assetId;
        private CommonKernel.SimulationId simulationId;
        private CommonKernel.RiskScoreId riskScoreId;
        private CommonKernel.ThreatEventId threatEventId;
        private Map<String, String> structuredData = new HashMap<>();

        private Builder() {}

        public Builder incidentId(CommonKernel.IncidentId incidentId) {
            this.incidentId = incidentId;
            return this;
        }

        public Builder assetId(CommonKernel.AssetId assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder simulationId(CommonKernel.SimulationId simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public Builder riskScoreId(CommonKernel.RiskScoreId riskScoreId) {
            this.riskScoreId = riskScoreId;
            return this;
        }

        public Builder threatEventId(CommonKernel.ThreatEventId threatEventId) {
            this.threatEventId = threatEventId;
            return this;
        }

        public Builder addFact(String key, String value) {
            this.structuredData.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
            return this;
        }

        public Builder addFacts(Map<String, String> facts) {
            this.structuredData.putAll(facts);
            return this;
        }

        public GroundingContext build() {
            return new GroundingContext(this);
        }
    }
}