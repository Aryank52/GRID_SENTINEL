package com.gridsentinel.simulation;

import com.gridsentinel.common.CommonKernel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SimulationTest {

    @Test
    void simulationCreation() {
        CommonKernel.SimulationId id = CommonKernel.SimulationId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();

        // SimulationParameters is required
        SimulationParameters parameters = new SimulationParameters();

        Simulation sim = Simulation.builder()
                .id(id)
                .name("Test Simulation")
                .scenarioId("wildfire_north")
                .parameters(parameters)
                .build();

        assertThat(sim.getId()).isEqualTo(id);
        assertThat(sim.getName()).isEqualTo("Test Simulation");
        assertThat(sim.getScenarioId()).isEqualTo("wildfire_north");
        assertThat(sim.getStatus()).isEqualTo(CommonKernel.SimulationStatus.PENDING);
        assertThat(sim.getParameters()).isNotNull();
        assertThat(sim.getCreatedAt()).isNotNull();
        assertThat(sim.getVersion()).isEqualTo(0);
    }

    @Test
    void simulationNameCannotBeNull() {
        assertThatThrownBy(() -> Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name(null)
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void simulationScenarioIdCannotBeNull() {
        assertThatThrownBy(() -> Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId(null)
                .parameters(new SimulationParameters())
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void simulationParametersCannotBeNull() {
        assertThatThrownBy(() -> Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void simulationStart() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        int oldVersion = sim.getVersion();
        sim.start();

        assertThat(sim.getStatus()).isEqualTo(CommonKernel.SimulationStatus.RUNNING);
        assertThat(sim.getStartedAt()).isNotNull();
        assertThat(sim.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void simulationComplete() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim.start();
        int oldVersion = sim.getVersion();

        ImpactResult result = new ImpactResult();
        sim.complete(result);

        assertThat(sim.getStatus()).isEqualTo(CommonKernel.SimulationStatus.COMPLETED);
        assertThat(sim.getCompletedAt()).isNotNull();
        assertThat(sim.getResult()).isEqualTo(result);
        assertThat(sim.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void simulationFail() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim.start();
        int oldVersion = sim.getVersion();

        sim.fail("Timeout after 300s");

        assertThat(sim.getStatus()).isEqualTo(CommonKernel.SimulationStatus.FAILED);
        assertThat(sim.getCompletedAt()).isNotNull();
        assertThat(sim.getErrorMessage()).isEqualTo("Timeout after 300s");
        assertThat(sim.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void simulationCancel() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        int oldVersion = sim.getVersion();
        sim.cancel();

        assertThat(sim.getStatus()).isEqualTo(CommonKernel.SimulationStatus.CANCELLED);
        assertThat(sim.getCompletedAt()).isNotNull();
        assertThat(sim.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void simulationInvalidTransitionPendingToCompleted() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        assertThatThrownBy(() -> sim.complete(new ImpactResult()))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void simulationInvalidTransitionCompletedToRunning() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim.start();
        sim.complete(new ImpactResult());

        assertThatThrownBy(() -> sim.start())
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void simulationInvalidTransitionFailedToRunning() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim.start();
        sim.fail("Error");

        assertThatThrownBy(() -> sim.start())
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void simulationIsCompleted() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        assertThat(sim.isCompleted()).isFalse();

        sim.start();
        sim.complete(new ImpactResult());

        assertThat(sim.isCompleted()).isTrue();
    }

    @Test
    void simulationIsTerminal() {
        Simulation sim = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        assertThat(sim.isTerminal()).isFalse();

        sim.start();
        assertThat(sim.isTerminal()).isFalse();

        sim.complete(new ImpactResult());
        assertThat(sim.isTerminal()).isTrue();

        Simulation sim2 = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim2.start();
        sim2.fail("Error");
        assertThat(sim2.isTerminal()).isTrue();

        Simulation sim3 = Simulation.builder()
                .id(CommonKernel.SimulationId.generate())
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .build();

        sim3.cancel();
        assertThat(sim3.isTerminal()).isTrue();
    }

    @Test
    void simulationEquality() {
        CommonKernel.SimulationId id = CommonKernel.SimulationId.generate();
        CommonKernel.CorrelationId correlationId = CommonKernel.CorrelationId.generate();
        Instant createdAt = Instant.now();

        Simulation sim1 = Simulation.builder()
                .id(id)
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .correlationId(correlationId)
                .build();

        Simulation sim2 = Simulation.builder()
                .id(id)
                .name("Test")
                .scenarioId("test")
                .parameters(new SimulationParameters())
                .correlationId(correlationId)
                .build();

        assertThat(sim1).isEqualTo(sim2);
        assertThat(sim1.hashCode()).isEqualTo(sim2.hashCode());
    }
}