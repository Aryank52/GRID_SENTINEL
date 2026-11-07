package com.gridsentinel.detection;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.observation.Evidence;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Evidence-backed physical threat/anomaly candidate.
 * <p>
 * Created by the detection pipeline from fused evidence.
 * Not yet confirmed as an incident - requires operator triage or automatic confirmation.
 * </p>
 */
@Entity
@Table(name = "threat_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThreatEvent {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "threat_event_id", nullable = false))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.ThreatEventId id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ThreatType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ThreatSeverity severity;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Double confidence; // [0.0, 1.0]

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private Instant detectedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant firstObservedAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "affected_asset_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId affectedAssetId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "nearby_grid_node_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId nearbyGridNodeId;

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject location;

    @Setter(AccessLevel.PRIVATE)
    private String description; // Human-readable: what happened

    @Setter(AccessLevel.PRIVATE)
    private String rationale; // Why the system thinks it matters

    // @ElementCollection - Disabled due to Hibernate enum issue with inner enum
    // @CollectionTable(name = "threat_event_evidence", joinColumns = @JoinColumn(name = "threat_event_id"))
    // @AttributeOverrides({
    //     @AttributeOverride(name = "type", column = @Column(name = "evidence_type")),
    //     @AttributeOverride(name = "value", column = @Column(name = "evidence_value"))
    // })
    // @Setter(AccessLevel.PRIVATE)
    // private Set<Evidence> supportingEvidence = new HashSet<>();
    
    @Transient
    @Setter(AccessLevel.PRIVATE)
    private Set<Evidence> supportingEvidence = new HashSet<>();

    @Setter(AccessLevel.PRIVATE)
    private String detectionStrategy; // ThreatDetectionStrategy implementation

    @Setter(AccessLevel.PRIVATE)
    private String strategyVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private ThreatStatus status = ThreatStatus.NEW;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private ThreatEvent(Builder builder) {
        this.id = builder.id;
        this.type = Objects.requireNonNull(builder.type, "Threat type cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.confidence = Objects.requireNonNull(builder.confidence, "Confidence cannot be null");
        if (this.confidence < 0.0 || this.confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be in [0.0, 1.0]");
        }
        this.detectedAt = builder.detectedAt != null ? builder.detectedAt : Instant.now();
        this.firstObservedAt = builder.firstObservedAt;
        this.affectedAssetId = builder.affectedAssetId;
        this.nearbyGridNodeId = builder.nearbyGridNodeId;
        this.location = builder.location;
        this.description = Objects.requireNonNull(builder.description, "Description cannot be null");
        this.rationale = builder.rationale;
        this.supportingEvidence = builder.supportingEvidence != null ?
                Set.copyOf(builder.supportingEvidence) : Collections.emptySet();
        this.detectionStrategy = builder.detectionStrategy;
        this.strategyVersion = builder.strategyVersion;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.version = 0;
    }

    public void addEvidence(Evidence evidence) {
        supportingEvidence.add(Objects.requireNonNull(evidence));
        this.version++;
    }

    public void updateStatus(ThreatStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
        this.version++;
    }

    public void escalateSeverity(ThreatSeverity newSeverity) {
        if (newSeverity.ordinal() > this.severity.ordinal()) {
            this.severity = newSeverity;
            this.version++;
        }
    }

    public boolean isActionable() {
        return confidence >= 0.7 && severity.ordinal() >= ThreatSeverity.MEDIUM.ordinal();
    }

    public Set<Evidence> getSupportingEvidence() {
        return Collections.unmodifiableSet(supportingEvidence);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreatEvent)) return false;
        ThreatEvent that = (ThreatEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.ThreatEventId id = CommonKernel.ThreatEventId.generate();
        private ThreatType type;
        private ThreatSeverity severity;
        private Double confidence;
        private Instant detectedAt;
        private Instant firstObservedAt;
        private CommonKernel.AssetId affectedAssetId;
        private CommonKernel.GridNodeId nearbyGridNodeId;
        private CommonKernel.GeometryValueObject location;
        private String description;
        private String rationale;
        private Set<Evidence> supportingEvidence = new HashSet<>();
        private String detectionStrategy;
        private String strategyVersion;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.ThreatEventId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder type(ThreatType type) {
            this.type = type;
            return this;
        }

        public Builder severity(ThreatSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder detectedAt(Instant detectedAt) {
            this.detectedAt = detectedAt;
            return this;
        }

        public Builder firstObservedAt(Instant firstObservedAt) {
            this.firstObservedAt = firstObservedAt;
            return this;
        }

        public Builder affectedAssetId(CommonKernel.AssetId affectedAssetId) {
            this.affectedAssetId = affectedAssetId;
            return this;
        }

        public Builder nearbyGridNodeId(CommonKernel.GridNodeId nearbyGridNodeId) {
            this.nearbyGridNodeId = nearbyGridNodeId;
            return this;
        }

        public Builder location(CommonKernel.GeometryValueObject location) {
            this.location = location;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rationale(String rationale) {
            this.rationale = rationale;
            return this;
        }

        public Builder supportingEvidence(Set<Evidence> supportingEvidence) {
            this.supportingEvidence = Objects.requireNonNullElseGet(supportingEvidence, HashSet::new);
            return this;
        }

        public Builder addEvidence(Evidence evidence) {
            this.supportingEvidence.add(Objects.requireNonNull(evidence));
            return this;
        }

        public Builder detectionStrategy(String detectionStrategy) {
            this.detectionStrategy = detectionStrategy;
            return this;
        }

        public Builder strategyVersion(String strategyVersion) {
            this.strategyVersion = strategyVersion;
            return this;
        }

        public Builder correlationId(CommonKernel.CorrelationId correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public ThreatEvent build() {
            return new ThreatEvent(this);
        }
    }
}