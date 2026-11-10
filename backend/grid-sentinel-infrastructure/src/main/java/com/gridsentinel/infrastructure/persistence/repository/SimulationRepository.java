package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.SimulationStatus;
import com.gridsentinel.simulation.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Simulation entities.
 */
@Repository
public interface SimulationRepository extends JpaRepository<Simulation, CommonKernel.SimulationId> {

    /**
     * Find simulation by ID.
     */
    Optional<Simulation> findById(CommonKernel.SimulationId id);

    /**
     * Find simulations by status.
     */
    List<Simulation> findByStatus(SimulationStatus status);

    /**
     * Find simulations by incident ID (via correlation).
     */
    @Query(value = """
        SELECT s FROM Simulation s
        WHERE s.correlationId = :correlationId
        ORDER BY s.createdAt DESC
        """)
    List<Simulation> findByCorrelationId(@Param("correlationId") CommonKernel.CorrelationId correlationId);

    /**
     * Find simulations by scenario ID.
     */
    @Query(value = """
        SELECT s FROM Simulation s
        WHERE s.scenarioId = :scenarioId
        ORDER BY s.createdAt DESC
        """)
    List<Simulation> findByScenarioId(@Param("scenarioId") String scenarioId);

    /**
     * Find simulations created within a time range.
     */
    @Query(value = """
        SELECT s FROM Simulation s
        WHERE s.createdAt BETWEEN :from AND :to
        ORDER BY s.createdAt DESC
        """)
    List<Simulation> findByCreatedAtBetween(
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * Count simulations by status.
     */
    @Query(value = """
        SELECT COUNT(s) FROM Simulation s
        WHERE s.status = :status
        """)
    long countByStatus(@Param("status") SimulationStatus status);
}