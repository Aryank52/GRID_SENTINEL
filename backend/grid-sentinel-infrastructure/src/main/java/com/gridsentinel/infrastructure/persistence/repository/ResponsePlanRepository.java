package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.response.ResponsePlan;
import com.gridsentinel.response.ResponsePlan.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ResponsePlan entities.
 */
@Repository
public interface ResponsePlanRepository extends JpaRepository<ResponsePlan, CommonKernel.ResponsePlanId> {

    /**
     * Find response plan by ID.
     */
    Optional<ResponsePlan> findById(CommonKernel.ResponsePlanId id);

    /**
     * Find response plans by incident ID.
     */
    @Query(value = """
        SELECT rp FROM ResponsePlan rp
        WHERE rp.incidentId = :incidentId
        ORDER BY rp.createdAt DESC
        """)
    List<ResponsePlan> findByIncidentId(@Param("incidentId") CommonKernel.IncidentId incidentId);

    /**
     * Find response plans by status.
     */
    List<ResponsePlan> findByStatus(PlanStatus status);

    /**
     * Find response plans by created by user.
     */
    @Query(value = """
        SELECT rp FROM ResponsePlan rp
        WHERE rp.createdBy = :userId
        ORDER BY rp.createdAt DESC
        """)
    List<ResponsePlan> findByCreatedBy(@Param("userId") CommonKernel.UserId userId);

    /**
     * Find response plans by correlation ID.
     */
    @Query(value = """
        SELECT rp FROM ResponsePlan rp
        WHERE rp.correlationId = :correlationId
        ORDER BY rp.createdAt DESC
        """)
    List<ResponsePlan> findByCorrelationId(@Param("correlationId") CommonKernel.CorrelationId correlationId);

    /**
     * Count response plans by status.
     */
    @Query(value = """
        SELECT COUNT(rp) FROM ResponsePlan rp
        WHERE rp.status = :status
        """)
    long countByStatus(@Param("status") PlanStatus status);

    /**
     * Find response plans by approved by user.
     */
    @Query(value = """
        SELECT rp FROM ResponsePlan rp
        WHERE rp.approvedBy = :userId
        ORDER BY rp.approvedAt DESC
        """)
    List<ResponsePlan> findByApprovedBy(@Param("userId") CommonKernel.UserId userId);
}