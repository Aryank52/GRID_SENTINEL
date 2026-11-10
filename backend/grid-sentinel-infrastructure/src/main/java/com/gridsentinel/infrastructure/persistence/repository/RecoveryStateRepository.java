package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.recovery.RecoveryPhase;
import com.gridsentinel.recovery.RecoveryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for RecoveryState persistence.
 */
@Repository
public interface RecoveryStateRepository extends JpaRepository<RecoveryState, CommonKernel.RecoveryStateId> {

    Optional<RecoveryState> findByIncidentId(CommonKernel.IncidentId incidentId);

    @Query("SELECT r FROM RecoveryState r WHERE r.incidentId = :incidentId AND r.phase != 'COMPLETED' AND r.phase != 'CANCELLED'")
    Optional<RecoveryState> findActiveByIncidentId(@Param("incidentId") CommonKernel.IncidentId incidentId);

    List<RecoveryState> findByPhase(RecoveryPhase phase);

    List<RecoveryState> findByRecoveryManagerId(CommonKernel.UserId recoveryManagerId);
}