package com.gridsentinel.asset;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridNode;
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
import jakarta.validation.constraints.Size;
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
 * Aggregate root representing a critical grid infrastructure asset.
 * <p>
 * Invariants:
 * <ul>
 *   <li>Asset must have a valid geometry with SRID 4326</li>
 *   <li>Criticality cannot be null</li>
 *   <li>HealthIndex must be in [0.0, 1.0]</li>
 *   <li>Redundancy level must be non-negative</li>
 *   <li>Asset type must be from controlled vocabulary</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "assets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "asset_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private AssetType type;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.Criticality criticality;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.HealthIndex healthIndex;

    @Setter(AccessLevel.PRIVATE)
    private int redundancyLevel; // 0 = no redundancy, 1 = N+1, 2 = N+2, etc.

    @NotNull
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject geometry;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "grid_node_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId gridNodeId; // Associated grid topology node

    @Size(max = 1000)
    @Setter(AccessLevel.PRIVATE)
    private String description;

    @Setter(AccessLevel.PRIVATE)
    private Instant installedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant lastInspectedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version; // Optimistic locking

    // Domain events (not persisted)
    private final transient Set<CommonKernel.DomainEvent> domainEvents = new HashSet<>();

    private Asset(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.criticality = Objects.requireNonNull(builder.criticality, "Criticality cannot be null");
        this.healthIndex = Objects.requireNonNull(builder.healthIndex, "HealthIndex cannot be null");
        this.redundancyLevel = Math.max(0, builder.redundancyLevel);
        this.geometry = Objects.requireNonNull(builder.geometry, "Geometry cannot be null");
        this.gridNodeId = builder.gridNodeId;
        this.description = builder.description;
        this.installedAt = builder.installedAt;
        this.lastInspectedAt = builder.lastInspectedAt;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    public void updateGeometry(CommonKernel.GeometryValueObject geometry) {
        this.geometry = Objects.requireNonNull(geometry, "Geometry cannot be null");
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void updateHealthIndex(CommonKernel.HealthIndex healthIndex) {
        this.healthIndex = Objects.requireNonNull(healthIndex, "HealthIndex cannot be null");
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void updateCriticality(CommonKernel.Criticality criticality) {
        this.criticality = Objects.requireNonNull(criticality, "Criticality cannot be null");
        this.updatedAt = Instant.now();
        this.version++;
    }

    public void updateRedundancyLevel(int redundancyLevel) {
        this.redundancyLevel = Math.max(0, redundancyLevel);
        this.updatedAt = Instant.now();
        this.version++;
    }

    public boolean hasRedundancy() {
        return redundancyLevel > 0;
    }

    public void addDomainEvent(CommonKernel.DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event));
    }

    public Set<CommonKernel.DomainEvent> getDomainEvents() {
        return Collections.unmodifiableSet(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset)) return false;
        Asset asset = (Asset) o;
        return id != null && id.equals(asset.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.AssetId id = CommonKernel.AssetId.generate();
        private String name;
        private AssetType type;
        private CommonKernel.Criticality criticality;
        private CommonKernel.HealthIndex healthIndex;
        private int redundancyLevel = 0;
        private CommonKernel.GeometryValueObject geometry;
        private CommonKernel.GridNodeId gridNodeId;
        private String description;
        private Instant installedAt;
        private Instant lastInspectedAt;

        private Builder() {}

        public Builder id(CommonKernel.AssetId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(AssetType type) {
            this.type = Objects.requireNonNull(type);
            return this;
        }

        public Builder criticality(CommonKernel.Criticality criticality) {
            this.criticality = Objects.requireNonNull(criticality);
            return this;
        }

        public Builder healthIndex(CommonKernel.HealthIndex healthIndex) {
            this.healthIndex = Objects.requireNonNull(healthIndex);
            return this;
        }

        public Builder redundancyLevel(int redundancyLevel) {
            this.redundancyLevel = Math.max(0, redundancyLevel);
            return this;
        }

        public Builder geometry(CommonKernel.GeometryValueObject geometry) {
            this.geometry = Objects.requireNonNull(geometry);
            return this;
        }

        public Builder gridNodeId(CommonKernel.GridNodeId gridNodeId) {
            this.gridNodeId = gridNodeId;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder installedAt(Instant installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public Builder lastInspectedAt(Instant lastInspectedAt) {
            this.lastInspectedAt = lastInspectedAt;
            return this;
        }

        public Asset build() {
            return new Asset(this);
        }
    }
}