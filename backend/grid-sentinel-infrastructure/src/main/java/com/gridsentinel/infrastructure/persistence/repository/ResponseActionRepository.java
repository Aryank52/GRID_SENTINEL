package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.response.ResponseAction;
import com.gridsentinel.response.ResponseAction.ActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ResponseAction entities.
 * Note: ResponseAction is an embeddable within ResponsePlan, so this repository
 * provides query capabilities for actions extracted from plans.
 */
@Repository
public interface ResponseActionRepository extends JpaRepository<ResponseAction, CommonKernel.ResponseActionId> {

    /**
     * Find response action by ID.
     */
    Optional<ResponseAction> findById(CommonKernel.ResponseActionId id);

    /**
     * Find response actions by status.
     */
    @Query(value = """
        SELECT a FROM ResponseAction a
        WHERE a.status = :status
        """)
    List<ResponseAction> findByStatus(@Param("status") ActionStatus status);

    /**
     * Find response actions by assigned user.
     */
    @Query(value = """
        SELECT a FROM ResponseAction a
        WHERE a.assignedTo = :userId
        ORDER BY a.plannedStartAt ASC
        """)
    List<ResponseAction> findByAssignedTo(@Param("userId") CommonKernel.UserId userId);

    /**
     * Find response actions by target asset.
     */
    @Query(value = """
        SELECT a FROM ResponseAction a
        WHERE a.targetAssetId = :assetId
        """)
    List<ResponseAction> findByTargetAssetId(@Param("assetId") CommonKernel.AssetId assetId);

    /**
     * Find response actions by target grid node.
     */
    @Query(value = """
        SELECT a FROM ResponseAction a
        WHERE a.targetGridNodeId = :nodeId
        """)
    List<ResponseAction> findByTargetGridNodeId(@Param("nodeId") CommonKernel.GridNodeId nodeId);

    /**
     * Find response actions by correlation ID.
     */
    @Query(value = """
        SELECT a FROM ResponseAction a
        WHERE a.correlationId = :correlationId
        """)
    List<ResponseAction> findByCorrelationId(@Param("correlationId") CommonKernel.CorrelationId correlationId);

    /**
     * Count response actions by status.
     */
    @Query(value = """
        SELECT COUNT(a) FROM ResponseAction a
        WHERE a.status = :status
        """)
    long countByStatus(@Param("status") ActionStatus status);
}