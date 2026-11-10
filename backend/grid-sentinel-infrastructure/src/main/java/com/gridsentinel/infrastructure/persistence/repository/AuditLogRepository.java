package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.audit.AuditLog;
import com.gridsentinel.audit.AuditEventType;
import com.gridsentinel.common.CommonKernel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, CommonKernel.AuditLogId> {

    /**
     * Find audit log by ID.
     */
    Optional<AuditLog> findById(CommonKernel.AuditLogId id);

    /**
     * Find audit logs by aggregate type and ID.
     */
    @Query(value = """
        SELECT a FROM AuditLog a
        WHERE a.aggregateType = :aggregateType
        AND a.aggregateId = :aggregateId
        ORDER BY a.timestamp DESC
        """)
    List<AuditLog> findByAggregate(@Param("aggregateType") String aggregateType, @Param("aggregateId") String aggregateId);

    /**
     * Find audit logs by actor.
     */
    @Query(value = """
        SELECT a FROM AuditLog a
        WHERE a.actorId = :actorId
        ORDER BY a.timestamp DESC
        """)
    List<AuditLog> findByActorId(@Param("actorId") CommonKernel.UserId actorId);

    /**
     * Find audit logs by correlation ID.
     */
    @Query(value = """
        SELECT a FROM AuditLog a
        WHERE a.correlationId = :correlationId
        ORDER BY a.timestamp DESC
        """)
    List<AuditLog> findByCorrelationId(@Param("correlationId") CommonKernel.CorrelationId correlationId);

    /**
     * Find audit logs by event type.
     */
    @Query(value = """
        SELECT a FROM AuditLog a
        WHERE a.eventType = :eventType
        ORDER BY a.timestamp DESC
        """)
    List<AuditLog> findByEventType(@Param("eventType") AuditEventType eventType);

    /**
     * Find audit logs by timestamp range.
     */
    @Query(value = """
        SELECT a FROM AuditLog a
        WHERE a.timestamp BETWEEN :from AND :to
        ORDER BY a.timestamp DESC
        """)
    List<AuditLog> findByTimestampBetween(
            @Param("from") Instant from,
            @Param("to") Instant to);

    /**
     * Count audit logs by event type.
     */
    @Query(value = """
        SELECT COUNT(a) FROM AuditLog a
        WHERE a.eventType = :eventType
        """)
    long countByEventType(@Param("eventType") AuditEventType eventType);
}