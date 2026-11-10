package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.simulation.ImpactResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ImpactResult entities.
 */
@Repository
public interface ImpactResultRepository extends JpaRepository<ImpactResult, String> {

    /**
     * Find impact result by simulation ID.
     */
    @Query(value = """
        SELECT ir FROM ImpactResult ir
        WHERE ir.simulationId = :simulationId
        """)
    Optional<ImpactResult> findBySimulationId(@Param("simulationId") CommonKernel.SimulationId simulationId);

    /**
     * Find impact result by scenario ID.
     */
    @Query(value = """
        SELECT ir FROM ImpactResult ir
        WHERE ir.scenarioId = :scenarioId
        """)
    Optional<ImpactResult> findByScenarioId(@Param("scenarioId") String scenarioId);

    /**
     * Find impact results by date range.
     */
    @Query(value = """
        SELECT ir FROM ImpactResult ir
        WHERE ir.computedAt BETWEEN :from AND :to
        ORDER BY ir.computedAt DESC
        """)
    List<ImpactResult> findByComputedAtBetween(
            @Param("from") java.time.Instant from,
            @Param("to") java.time.Instant to);
}