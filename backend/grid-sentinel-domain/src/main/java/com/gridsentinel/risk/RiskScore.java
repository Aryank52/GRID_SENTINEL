package com.gridsentinel.risk;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.RiskBand;
import com.gridsentinel.asset.Asset;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Explainable risk score for an asset.
 * <p>
 * Invariants:
 * <ul>
 *   <li>Score must be in [0.0, 1.0]</li>
 *   <li>Band must be consistent with score</li>
 *   <li>Model version must be specified for reproducibility</li>
 *   <li>Factor breakdown must sum to approximately 1.0 (weights)</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "risk_scores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskScore {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "risk_score_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.RiskScoreId id;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "asset_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId assetId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double score; // [0.0, 1.0]

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private RiskBand band;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String modelName;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String modelVersion;

    @Setter(AccessLevel.PRIVATE)
    private String strategyName; // RiskScoringStrategy implementation

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private FactorBreakdown factorBreakdown;

    @Setter(AccessLevel.PRIVATE)
    private Instant computedAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "correlation_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    private RiskScore(Builder builder) {
        this.id = builder.id;
        this.assetId = Objects.requireNonNull(builder.assetId, "AssetId cannot be null");
        this.score = Objects.requireNonNull(builder.score, "Score cannot be null");
        if (this.score < 0.0 || this.score > 1.0) {
            throw new IllegalArgumentException("Score must be in [0.0, 1.0]");
        }
        this.band = Objects.requireNonNull(builder.band, "Band cannot be null");
        // Validate band consistency
        RiskBand expectedBand = RiskBand.fromScore(this.score);
        if (this.band != expectedBand) {
            throw new IllegalArgumentException("Band " + this.band + " inconsistent with score " + this.score + " (expected " + expectedBand + ")");
        }
        this.modelName = Objects.requireNonNull(builder.modelName, "Model name cannot be null");
        this.modelVersion = Objects.requireNonNull(builder.modelVersion, "Model version cannot be null");
        this.strategyName = builder.strategyName;
        this.factorBreakdown = Objects.requireNonNull(builder.factorBreakdown, "Factor breakdown cannot be null");
        this.computedAt = builder.computedAt != null ? builder.computedAt : Instant.now();
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
    }

    public double getScore() {
        return score;
    }

    public RiskBand getBand() {
        return band;
    }

    public FactorBreakdown getFactorBreakdown() {
        return factorBreakdown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RiskScore)) return false;
        RiskScore riskScore = (RiskScore) o;
        return id != null && id.equals(riskScore.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.RiskScoreId id = CommonKernel.RiskScoreId.generate();
        private CommonKernel.AssetId assetId;
        private Double score;
        private RiskBand band;
        private String modelName;
        private String modelVersion;
        private String strategyName;
        private FactorBreakdown factorBreakdown;
        private Instant computedAt;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.RiskScoreId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder assetId(CommonKernel.AssetId assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder score(double score) {
            this.score = score;
            this.band = RiskBand.fromScore(score);
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder modelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public Builder factorBreakdown(FactorBreakdown factorBreakdown) {
            this.factorBreakdown = factorBreakdown;
            return this;
        }

        public Builder computedAt(Instant computedAt) {
            this.computedAt = computedAt;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public RiskScore build() {
            return new RiskScore(this);
        }
    }
}