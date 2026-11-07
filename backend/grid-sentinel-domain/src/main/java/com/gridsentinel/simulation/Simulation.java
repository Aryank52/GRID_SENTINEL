package com.gridsentinel.simulation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.grid.TopologySnapshot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * Deterministic simulation of a failure scenario.
 * <p>
 * Invariants:
 * <ul>
 *   <li>Same scenario + same topology + same model version = same result</li>
 *   <li>Random seed captured for any stochastic elements</li>
 *   <li>Topology snapshot immutable reference</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "simulations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Simulation {

    @Id
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "simulation_id", nullable = false))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.SimulationId id;

    @NotNull
    @Size(max = 255)
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @NotNull
    @Setter(AccessLevel.PRIVATE)
    private String scenarioId; // References predefined scenario

    @NotNull
    @Enumerated(EnumType.STRING)
    @Setter(AccessLevel.PRIVATE)
    private SimulationStatus status = SimulationStatus.PENDING;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private TopologySnapshot topologySnapshot; // Transient, not persisted directly

    @Setter(AccessLevel.PRIVATE)
    private String topologySnapshotId; // Reference to stored snapshot

    @Setter(AccessLevel.PRIVATE)
    private String strategyName; // ImpactSimulationStrategy implementation

    @Setter(AccessLevel.PRIVATE)
    private String strategyVersion;

    @Setter(AccessLevel.PRIVATE)
    private Long randomSeed;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "randomSeed", column = @Column(name = "parameters_random_seed"))
    })
    @Setter(AccessLevel.PRIVATE)
    private SimulationParameters parameters;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "result_id", unique = true)
    @Setter(AccessLevel.PRIVATE)
    private ImpactResult result;

    @Setter(AccessLevel.PRIVATE)
    private String errorMessage;

    @Transient
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.UserId requestedBy;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "correlation_id", columnDefinition = "uuid"))
    })
    @Setter(AccessLevel.PRIVATE)
    private CommonKernel.CorrelationId correlationId;

    @Setter(AccessLevel.PRIVATE)
    private Instant startedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant completedAt;

    @Setter(AccessLevel.PRIVATE)
    private Instant createdAt;

    @Setter(AccessLevel.PRIVATE)
    private int version;

    private Simulation(Builder builder) {
        this.id = builder.id;
        this.name = Objects.requireNonNull(builder.name, "Name cannot be null");
        this.scenarioId = Objects.requireNonNull(builder.scenarioId, "Scenario ID cannot be null");
        this.topologySnapshot = builder.topologySnapshot;
        this.topologySnapshotId = builder.topologySnapshotId;
        this.strategyName = builder.strategyName;
        this.strategyVersion = builder.strategyVersion;
        this.randomSeed = builder.randomSeed;
        this.parameters = Objects.requireNonNull(builder.parameters, "Parameters cannot be null");
        this.requestedBy = builder.requestedBy;
        this.correlationId = builder.correlationId != null ? builder.correlationId : CommonKernel.CorrelationId.generate();
        this.createdAt = Instant.now();
        this.version = 0;
    }

    public void start() {
        if (status != SimulationStatus.PENDING) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "SIMULATION_ALREADY_STARTED",
                    "Simulation already in state: " + status
            );
        }
        this.status = SimulationStatus.RUNNING;
        this.startedAt = Instant.now();
        this.version++;
    }

    public void complete(ImpactResult result) {
        if (status != SimulationStatus.RUNNING) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "SIMULATION_NOT_RUNNING",
                    "Cannot complete simulation in state: " + status
            );
        }
        this.status = SimulationStatus.COMPLETED;
        this.result = Objects.requireNonNull(result);
        this.completedAt = Instant.now();
        this.version++;
    }

    public void fail(String errorMessage) {
        if (status != SimulationStatus.RUNNING) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "SIMULATION_NOT_RUNNING",
                    "Cannot fail simulation in state: " + status
            );
        }
        this.status = SimulationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
        this.version++;
    }

    public void cancel() {
        if (status == SimulationStatus.COMPLETED || status == SimulationStatus.FAILED) {
            throw new CommonKernel.IllegalStateTransitionException(
                    "SIMULATION_ALREADY_TERMINAL",
                    "Cannot cancel completed simulation"
            );
        }
        this.status = SimulationStatus.CANCELLED;
        this.completedAt = Instant.now();
        this.version++;
    }

    public boolean isCompleted() {
        return status == SimulationStatus.COMPLETED;
    }

    public boolean isTerminal() {
        return status == SimulationStatus.COMPLETED ||
               status == SimulationStatus.FAILED ||
               status == SimulationStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Simulation)) return false;
        Simulation that = (Simulation) o;
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
        private CommonKernel.SimulationId id = CommonKernel.SimulationId.generate();
        private String name;
        private String scenarioId;
        private TopologySnapshot topologySnapshot;
        private String topologySnapshotId;
        private String strategyName;
        private String strategyVersion;
        private Long randomSeed;
        private SimulationParameters parameters;
        private CommonKernel.UserId requestedBy;
        private CommonKernel.CorrelationId correlationId;

        private Builder() {}

        public Builder id(CommonKernel.SimulationId id) {
            this.id = Objects.requireNonNull(id);
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder scenarioId(String scenarioId) {
            this.scenarioId = scenarioId;
            return this;
        }

        public Builder topologySnapshot(TopologySnapshot topologySnapshot) {
            this.topologySnapshot = topologySnapshot;
            return this;
        }

        public Builder topologySnapshotId(String topologySnapshotId) {
            this.topologySnapshotId = topologySnapshotId;
            return this;
        }

        public Builder strategyName(String strategyName) {
            this.strategyName = strategyName;
            return this;
        }

        public Builder strategyVersion(String strategyVersion) {
            this.strategyVersion = strategyVersion;
            return this;
        }

        public Builder randomSeed(Long randomSeed) {
            this.randomSeed = randomSeed;
            return this;
        }

        public Builder parameters(SimulationParameters parameters) {
            this.parameters = parameters;
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

        public Simulation build() {
            return new Simulation(this);
        }
    }
}