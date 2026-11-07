package com.gridsentinel.grid;

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
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Grid topology edge (transmission line, transformer, cable, etc.).
 * <p>
 * Invariants:
 * <ul>
 *   <li>Edge must connect two distinct nodes</li>
 *   <li>Geometry must be a LineString (SRID 4326)</li>
 *   <li>Thermal rating must be positive</li>
 *   <li>Length must be non-negative</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "grid_edges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GridEdge {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "grid_edge_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridEdgeId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private EdgeType type;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "from_node_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId fromNodeId;

    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "to_node_id", nullable = false, columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId toNodeId;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private double voltageLevelKv;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private double thermalRatingAmps; // MVA rating at nominal voltage

    @Setter(AccessLevel.PRIVATE)
    private double lengthKm;

    @NotNull
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject geometry; // LineString

    @Setter(AccessLevel.PRIVATE)
    private int circuitCount; // Number of parallel circuits

    @Setter(AccessLevel.PRIVATE)
    private String conductorType;

    @Setter(AccessLevel.PRIVATE)
    private int yearInstalled;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "primary_asset_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId primaryAssetId;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private GridEdge(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.fromNodeId = Objects.requireNonNull(builder.fromNodeId, "From node cannot be null");
        this.toNodeId = Objects.requireNonNull(builder.toNodeId, "To node cannot be null");
        if (this.fromNodeId.equals(this.toNodeId)) {
            throw new IllegalArgumentException("Edge cannot connect a node to itself");
        }
        this.voltageLevelKv = builder.voltageLevelKv;
        if (this.voltageLevelKv <= 0) {
            throw new IllegalArgumentException("Voltage level must be positive");
        }
        this.thermalRatingAmps = builder.thermalRatingAmps;
        if (this.thermalRatingAmps <= 0) {
            throw new IllegalArgumentException("Thermal rating must be positive");
        }
        this.lengthKm = Math.max(0, builder.lengthKm);
        this.geometry = Objects.requireNonNull(builder.geometry, "Geometry cannot be null");
        if (!(this.geometry.getGeometry() instanceof LineString)) {
            throw new IllegalArgumentException("GridEdge geometry must be a LineString");
        }
        this.circuitCount = Math.max(1, builder.circuitCount);
        this.conductorType = builder.conductorType;
        this.yearInstalled = builder.yearInstalled;
        this.primaryAssetId = builder.primaryAssetId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    /**
     * Get the MVA rating at nominal voltage (3-phase).
     */
    public double getMvaRating() {
        return Math.sqrt(3) * voltageLevelKv * thermalRatingAmps / 1000.0;
    }

    /**
     * Check if this edge connects the given node.
     */
    public boolean connects(CommonKernel.GridNodeId nodeId) {
        return fromNodeId.equals(nodeId) || toNodeId.equals(nodeId);
    }

    /**
     * Get the other node given one endpoint.
     */
    public CommonKernel.GridNodeId getOtherNode(CommonKernel.GridNodeId nodeId) {
        if (fromNodeId.equals(nodeId)) return toNodeId;
        if (toNodeId.equals(nodeId)) return fromNodeId;
        throw new IllegalArgumentException("Node is not an endpoint of this edge");
    }

    /**
     * Check if edge is a transformer (voltage change).
     */
    public boolean isTransformer() {
        return type == EdgeType.TRANSFORMER || type == EdgeType.PHASE_SHIFTER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridEdge)) return false;
        GridEdge gridEdge = (GridEdge) o;
        return id != null && id.equals(gridEdge.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private CommonKernel.GridEdgeId id = CommonKernel.GridEdgeId.generate();
        private String name;
        private EdgeType type;
        private CommonKernel.GridNodeId fromNodeId;
        private CommonKernel.GridNodeId toNodeId;
        private double voltageLevelKv;
        private double thermalRatingAmps;
        private double lengthKm;
        private CommonKernel.GeometryValueObject geometry;
        private int circuitCount = 1;
        private String conductorType;
        private int yearInstalled;
        private CommonKernel.AssetId primaryAssetId;

        private Builder() {}

        public Builder id(CommonKernel.GridEdgeId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(EdgeType type) {
            this.type = type;
            return this;
        }

        public Builder fromNodeId(CommonKernel.GridNodeId fromNodeId) {
            this.fromNodeId = fromNodeId;
            return this;
        }

        public Builder toNodeId(CommonKernel.GridNodeId toNodeId) {
            this.toNodeId = toNodeId;
            return this;
        }

        public Builder voltageLevelKv(double voltageLevelKv) {
            this.voltageLevelKv = voltageLevelKv;
            return this;
        }

        public Builder thermalRatingAmps(double thermalRatingAmps) {
            this.thermalRatingAmps = thermalRatingAmps;
            return this;
        }

        public Builder lengthKm(double lengthKm) {
            this.lengthKm = lengthKm;
            return this;
        }

        public Builder geometry(CommonKernel.GeometryValueObject geometry) {
            this.geometry = geometry;
            return this;
        }

        public Builder circuitCount(int circuitCount) {
            this.circuitCount = circuitCount;
            return this;
        }

        public Builder conductorType(String conductorType) {
            this.conductorType = conductorType;
            return this;
        }

        public Builder yearInstalled(int yearInstalled) {
            this.yearInstalled = yearInstalled;
            return this;
        }

        public Builder primaryAssetId(CommonKernel.AssetId primaryAssetId) {
            this.primaryAssetId = primaryAssetId;
            return this;
        }

        public GridEdge build() {
            return new GridEdge(this);
        }
    }
}