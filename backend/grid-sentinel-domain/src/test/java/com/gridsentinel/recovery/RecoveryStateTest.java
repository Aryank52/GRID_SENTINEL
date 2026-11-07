package com.gridsentinel.recovery;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.recovery.MilestoneType;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RecoveryStateTest {

    @Test
    void recoveryStateCreation() {
        CommonKernel.RecoveryStateId id = CommonKernel.RecoveryStateId.generate();
        CommonKernel.IncidentId incidentId = CommonKernel.IncidentId.generate();

        RecoveryState state = RecoveryState.builder()
                .id(id)
                .incidentId(incidentId)
                .estimatedCompletionAt(Instant.now().plusSeconds(86400 * 7))
                .build();

        assertThat(state.getId()).isEqualTo(id);
        assertThat(state.getIncidentId()).isEqualTo(incidentId);
        assertThat(state.getPhase()).isEqualTo(RecoveryPhase.NOT_STARTED);
        assertThat(state.getOverallProgress()).isEqualTo(0.0);
        assertThat(state.getMilestones()).isEmpty();
        assertThat(state.getEstimatedCompletionAt()).isNotNull();
        assertThat(state.getVersion()).isEqualTo(0);
    }

    @Test
    void recoveryStateIncidentIdCannotBeNull() {
        assertThatThrownBy(() -> RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(null)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void recoveryStateAddMilestone() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        RecoveryMilestone milestone = RecoveryMilestone.builder()
                .id("MS-001")
                .name("Damage assessment")
                .description("Assess damage to transformer")
                .type(MilestoneType.DAMAGE_ASSESSMENT)
                .build();

        int oldVersion = state.getVersion();
        state.addMilestone(milestone);

        assertThat(state.getMilestones()).hasSize(1);
        assertThat(state.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void recoveryStateStart() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        CommonKernel.UserId managerId = CommonKernel.UserId.generate();
        int oldVersion = state.getVersion();

        state.start(managerId);

        assertThat(state.getPhase()).isEqualTo(RecoveryPhase.IN_PROGRESS);
        assertThat(state.getRecoveryManagerId()).isEqualTo(managerId);
        assertThat(state.getStartedAt()).isNotNull();
        assertThat(state.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void recoveryStateCannotStartTwice() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        state.start(CommonKernel.UserId.generate());

        assertThatThrownBy(() -> state.start(CommonKernel.UserId.generate()))
                .isInstanceOf(CommonKernel.IllegalStateTransitionException.class);
    }

    @Test
    void recoveryStateCompleteMilestone() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        state.start(CommonKernel.UserId.generate());

        RecoveryMilestone milestone = RecoveryMilestone.builder()
                .id("MS-001")
                .name("Damage assessment")
                .description("Assess damage to transformer")
                .type(MilestoneType.DAMAGE_ASSESSMENT)
                .build();

        state.addMilestone(milestone);

        // First start the milestone
        milestone.start(CommonKernel.UserId.generate());

        CommonKernel.UserId verifiedBy = CommonKernel.UserId.generate();
        state.completeMilestone("MS-001", verifiedBy, "Assessment complete");

        assertThat(state.getOverallProgress()).isEqualTo(1.0);
        assertThat(state.getPhase()).isEqualTo(RecoveryPhase.COMPLETED);
        assertThat(state.getActualCompletionAt()).isNotNull();
    }

    @Test
    void recoveryStateCompleteNonExistentMilestoneThrows() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        state.start(CommonKernel.UserId.generate());

        assertThatThrownBy(() -> state.completeMilestone("NON-EXISTENT", CommonKernel.UserId.generate(), "Test"))
                .isInstanceOf(CommonKernel.NotFoundException.class);
    }

    @Test
    void recoveryStateSetLessonsLearned() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        int oldVersion = state.getVersion();
        state.setLessonsLearned("Lesson 1: Always check thermal cameras first");

        assertThat(state.getLessonsLearned()).isEqualTo("Lesson 1: Always check thermal cameras first");
        assertThat(state.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void recoveryStateSetAfterActionReport() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        int oldVersion = state.getVersion();
        state.setAfterActionReport("After action report content");

        assertThat(state.getAfterActionReport()).isEqualTo("After action report content");
        assertThat(state.getVersion()).isEqualTo(oldVersion + 1);
    }

    @Test
    void recoveryStateGetPendingMilestones() {
        RecoveryState state = RecoveryState.builder()
                .id(CommonKernel.RecoveryStateId.generate())
                .incidentId(CommonKernel.IncidentId.generate())
                .build();

        RecoveryMilestone milestone1 = RecoveryMilestone.builder()
                .id("MS-001")
                .name("Damage assessment")
                .description("Assess damage")
                .type(MilestoneType.DAMAGE_ASSESSMENT)
                .build();

        RecoveryMilestone milestone2 = RecoveryMilestone.builder()
                .id("MS-002")
                .name("Equipment replacement")
                .description("Replace transformer")
                .type(MilestoneType.EQUIPMENT_REPLACEMENT)
                .build();

        state.addMilestone(milestone1);
        state.addMilestone(milestone2);

        List<RecoveryMilestone> pending = state.getPendingMilestones();
        assertThat(pending).hasSize(2);
    }

    @Test
    void recoveryStateAllPhases() {
        for (RecoveryPhase phase : RecoveryPhase.values()) {
            assertThat(phase).isNotNull();
        }
    }

    @Test
    void recoveryStateEquality() {
        CommonKernel.RecoveryStateId id = CommonKernel.RecoveryStateId.generate();
        CommonKernel.IncidentId incidentId = CommonKernel.IncidentId.generate();
        Instant createdAt = Instant.now();

        RecoveryState state1 = RecoveryState.builder()
                .id(id)
                .incidentId(incidentId)
                .correlationId(CommonKernel.CorrelationId.generate())
                .build();

        RecoveryState state2 = RecoveryState.builder()
                .id(id)
                .incidentId(incidentId)
                .correlationId(CommonKernel.CorrelationId.generate())
                .build();

        assertThat(state1).isEqualTo(state2);
        assertThat(state1.hashCode()).isEqualTo(state2.hashCode());
    }
}