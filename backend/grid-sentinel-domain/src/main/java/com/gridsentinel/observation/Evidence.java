package com.gridsentinel.observation;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Normalized feature evidence extracted from an observation.
 * <p>
 * Evidence is the atomic unit of detection: a validated, typed, quantified
 * observation feature that can be fused with other evidence.
 * </p>
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Evidence {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private EvidenceType type;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double value; // Normalized [0.0, 1.0] or physical quantity with unit

    @Setter(AccessLevel.PRIVATE)
    private String unit; // e.g., "celsius", "m/s", "pixels", "normalized"

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double confidence; // [0.0, 1.0] confidence in this evidence

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ObservationId sourceObservationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant extractedAt;

    @Setter(AccessLevel.PRIVATE)
    private String extractorVersion; // Version of the feature extractor

    @Setter(AccessLevel.PRIVATE)
    private String metadata; // JSON for additional context

    private Evidence(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "Evidence type cannot be null");
        this.value = Objects.requireNonNull(builder.value, "Value cannot be null");
        this.unit = builder.unit;
        this.confidence = Objects.requireNonNull(builder.confidence, "Confidence cannot be null");
        if (this.confidence < 0.0 || this.confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be in [0.0, 1.0]");
        }
        this.sourceObservationId = builder.sourceObservationId;
        this.extractedAt = builder.extractedAt != null ? builder.extractedAt : Instant.now();
        this.extractorVersion = builder.extractorVersion;
        this.metadata = builder.metadata;
    }

    /**
     * Check if this evidence supports a threat hypothesis.
     * Threshold depends on evidence type.
     */
    public boolean supportsThreat(double threshold) {
        return value >= threshold && confidence >= 0.5;
    }

    /**
     * Get weighted evidence value (value * confidence).
     */
    public double weightedValue() {
        return value * confidence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private EvidenceType type;
        private Double value;
        private String unit;
        private Double confidence;
        private CommonKernel.ObservationId sourceObservationId;
        private Instant extractedAt;
        private String extractorVersion;
        private String metadata;

        private Builder() {}

        public Builder type(EvidenceType type) {
            this.type = type;
            return this;
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder sourceObservationId(CommonKernel.ObservationId sourceObservationId) {
            this.sourceObservationId = sourceObservationId;
            return this;
        }

        public Builder extractedAt(Instant extractedAt) {
            this.extractedAt = extractedAt;
            return this;
        }

        public Builder extractorVersion(String extractorVersion) {
            this.extractorVersion = extractorVersion;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public Evidence build() {
            return new Evidence(this);
        }
    }
}