package com.gridsentinel.grid;

import com.gridsentinel.common.CommonKernel;
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
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Grid topology node (bus, substation, junction point).
 * <p>
 * Invariants:
 * <ul>
 *   <li>Node must have a valid geographic position (Point, SRID 4326)</li>
 *   <li>Voltage level must be positive</li>
 *   <li>Node type must be from controlled vocabulary</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "grid_nodes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GridNode {

    @Id
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GridNodeId id;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private NodeType type;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private double voltageLevelKv; // Nominal voltage in kV

    @NotNull
    @Embedded
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.GeometryValueObject geometry; // Point

    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.AssetId primaryAssetId; // Main asset at this node

    @Setter(AccessLevel.PRIVATE)
    private String operator; // Grid operator identifier

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant updatedAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    // Connected edges (not persisted directly, derived from GridEdge)
    private final transient Set<CommonKernel.GridEdgeId> connectedEdgeIds = new HashSet<>();

    private GridNode(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.type = Objects.requireNonNull(builder.type, "Type cannot be null");
        this.voltageLevelKv = builder.voltageLevelKv;
        if (this.voltageLevelKv <= 0) {
            throw new IllegalArgumentException("Voltage level must be positive");
        }
        this.geometry = Objects.requireNonNull(builder.geometry, "Geometry cannot be null");
        if (!(this.geometry.getGeometry() instanceof Point)) {
            throw new IllegalArgumentException("GridNode geometry must be a Point");
        }
        this.primaryAssetId = builder.primaryAssetId;
        this.operator = builder.operator;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    public void addConnectedEdge(CommonKernel.GridEdgeId edgeId) {
        connectedEdgeIds.add(Objects.requireNonNull(edgeId));
    }

    public void removeConnectedEdge(CommonKernel.GridEdgeId edgeId) {
        connectedEdgeIds.remove(edgeId);
    }

    public Set<CommonKernel.GridEdgeId> getConnectedEdgeIds() {
        return Collections.unmodifiableSet(connectedEdgeIds);
    }

    public int degree() {
        return connectedEdgeIds.size();
    }

    public boolean isSubstation() {
        return type == NodeType.SUBSTATION || type == NodeType.SWITCHING_STATION;
    }

    public boolean isGeneration() {
        return type == NodeType.GENERATION;
    }

    public boolean isLoad() {
        return type == NodeType.LOAD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GridNode)) return false;
        GridNode gridNode = (GridNode) o;
        return id != null && id.equals(gridNode.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum NodeType {
        SUBSTATION,           // Transmission/distribution substation
        SWITCHING_STATION,    // Switching only, no transformation
        GENERATION,           // Generation plant connection point
        LOAD,                 // Load center / distribution feeder head
        JUNCTION,             // Line junction / tap point
        HVDC_CONVERTER,       // HVDC converter station
        FACTS_DEVICE,         // FACTS device connection (SVC, STATCOM, etc.)
        INTERCONNECT          // Interconnection with another grid
    }

    public static final class Builder {
        private CommonKernel.GridNodeId id = CommonKernel.GridNodeId.generate();
        private String name;
        private NodeType type;
        private double voltageLevelKv;
        private CommonKernel.GeometryValueObject geometry;
        private CommonKernel.AssetId primaryAssetId;
        private String operator;

        private Builder() {}

        public Builder id(CommonKernel.GridNodeId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(NodeType type) {
            this.type = type;
            return this;
        }

        public Builder voltageLevelKv(double voltageLevelKv) {
            this.voltageLevelKv = voltageLevelKv;
            return this;
        }

        public Builder geometry(CommonKernel.GeometryValueObject geometry) {
            this.geometry = geometry;
            return this;
        }

        public Builder primaryAssetId(CommonKernel.AssetId primaryAssetId) {
            this.primaryAssetId = primaryAssetId;
            return this;
        }

        public Builder operator(String operator) {
            this.operator = operator;
            return this;
        }

        public GridNode build() {
            return new GridNode(this);
        }
    }
}