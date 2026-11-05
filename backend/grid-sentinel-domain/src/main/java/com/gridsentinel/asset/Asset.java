package com.gridsentinel.asset;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridNode;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
public class Asset {

    CommonKernel.AssetId id;

    @NotNull
    String name;

    @NotNull
    AssetType type;

    @NotNull
    CommonKernel.Criticality criticality;

    @NotNull
    CommonKernel.HealthIndex healthIndex;

    int redundancyLevel; // 0 = no redundancy, 1 = N+1, 2 = N+2, etc.

    CommonKernel.GeometryValueObject geometry;

    CommonKernel.GridNodeId gridNodeId; // Associated grid topology node

    @Size(max = 1000)
    String description;

    Instant installedAt;

    Instant lastInspectedAt;

    Instant createdAt;

    Instant updatedAt;

    int version; // Optimistic locking

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

    // Explicit getters (standard JavaBean style)
    public CommonKernel.AssetId getId() { return id; }
    public String getName() { return name; }
    public AssetType getType() { return type; }
    public CommonKernel.Criticality getCriticality() { return criticality; }
    public CommonKernel.HealthIndex getHealthIndex() { return healthIndex; }
    public int getRedundancyLevel() { return redundancyLevel; }
    public CommonKernel.GeometryValueObject getGeometry() { return geometry; }
    public CommonKernel.GridNodeId getGridNodeId() { return gridNodeId; }
    public String getDescription() { return description; }
    public Instant getInstalledAt() { return installedAt; }
    public Instant getLastInspectedAt() { return lastInspectedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public int getVersion() { return version; }

    // Fluent-style getters (for test compatibility)
    public CommonKernel.AssetId id() { return id; }
    public String name() { return name; }
    public AssetType type() { return type; }
    public CommonKernel.Criticality criticality() { return criticality; }
    public CommonKernel.HealthIndex healthIndex() { return healthIndex; }
    public int redundancyLevel() { return redundancyLevel; }
    public CommonKernel.GeometryValueObject geometry() { return geometry; }
    public String description() { return description; }
    public Instant installedAt() { return installedAt; }
    public Instant lastInspectedAt() { return lastInspectedAt; }
    public int version() { return version; }
    public CommonKernel.GridNodeId gridNodeId() { return gridNodeId; }

    /**
     * Update asset health index and record inspection time.
     */
    public void updateHealth(CommonKernel.HealthIndex newHealthIndex, Instant inspectedAt) {
        this.healthIndex = Objects.requireNonNull(newHealthIndex);
        this.lastInspectedAt = Objects.requireNonNull(inspectedAt);
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * Update asset criticality (rare, requires authorization).
     */
    public void updateCriticality(CommonKernel.Criticality newCriticality) {
        this.criticality = Objects.requireNonNull(newCriticality);
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * Update asset geometry (e.g., after survey correction).
     */
    public void updateGeometry(CommonKernel.GeometryValueObject newGeometry) {
        this.geometry = Objects.requireNonNull(newGeometry);
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * Associate with a grid topology node.
     */
    public void associateWithGridNode(CommonKernel.GridNodeId gridNodeId) {
        this.gridNodeId = Objects.requireNonNull(gridNodeId);
        this.updatedAt = Instant.now();
        this.version++;
    }

    /**
     * Check if asset is currently healthy.
     */
    public boolean isHealthy() {
        return healthIndex.isHealthy();
    }

    /**
     * Check if asset has any redundancy.
     */
    public boolean hasRedundancy() {
        return redundancyLevel > 0;
    }

    /**
     * Get redundancy factor for risk calculations (0.0 = no redundancy, 1.0 = full N+1).
     */
    public double redundancyFactor() {
        if (redundancyLevel <= 0) return 0.0;
        return Math.min(1.0, redundancyLevel * 0.33); // Diminishing returns
    }

    /**
     * Add a domain event to be published after transaction commits.
     */
    void addDomainEvent(CommonKernel.DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event));
    }

    /**
     * Get and clear pending domain events.
     */
    public Set<CommonKernel.DomainEvent> popDomainEvents() {
        Set<CommonKernel.DomainEvent> events = new HashSet<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableSet(events);
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
        private CommonKernel.HealthIndex healthIndex = CommonKernel.HealthIndex.healthy();
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
            this.type = type;
            return this;
        }

        public Builder criticality(CommonKernel.Criticality criticality) {
            this.criticality = criticality;
            return this;
        }

        public Builder healthIndex(CommonKernel.HealthIndex healthIndex) {
            this.healthIndex = healthIndex;
            return this;
        }

        public Builder redundancyLevel(int redundancyLevel) {
            this.redundancyLevel = redundancyLevel;
            return this;
        }

        public Builder geometry(CommonKernel.GeometryValueObject geometry) {
            this.geometry = geometry;
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