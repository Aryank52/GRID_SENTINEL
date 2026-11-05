package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.incident.Incident;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Incident entities with spatial query capabilities.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, CommonKernel.IncidentId> {

    /**
     * Find incidents within a bounding box (viewport query).
     */
    @Query(value = """
        SELECT i FROM Incident i
        WHERE i.location.geometry && :envelope
        """)
    List<Incident> findByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find incidents by state within bounding box.
     */
    @Query(value = """
        SELECT i FROM Incident i
        WHERE i.location.geometry && :envelope
        AND i.state = :state
        """)
    List<Incident> findByBboxAndState(@Param("envelope") Envelope envelope,
                                       @Param("state") String state);

    /**
     * Find incidents by severity within bounding box.
     */
    @Query(value = """
        SELECT i FROM Incident i
        WHERE i.location.geometry && :envelope
        AND i.severity = :severity
        """)
    List<Incident> findByBboxAndSeverity(@Param("envelope") Envelope envelope,
                                          @Param("severity") String severity);

    /**
     * Find active incidents (not resolved/closed) within bounding box.
     */
    @Query(value = """
        SELECT i FROM Incident i
        WHERE i.location.geometry && :envelope
        AND i.state IN ('DETECTED', 'TRIAGED', 'CONFIRMED', 'ACTIVE_RESPONSE', 'CONTAINED', 'RECOVERING')
        """)
    List<Incident> findActiveByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find incident by ID with geometry.
     */
    Optional<Incident> findByIdWithGeometry(CommonKernel.IncidentId id);

    /**
     * Find incident by incident number.
     */
    Optional<Incident> findByIncidentNumber(String incidentNumber);

    /**
     * Count incidents within bounding box.
     */
    @Query(value = """
        SELECT COUNT(i) FROM Incident i
        WHERE i.location.geometry && :envelope
        """)
    long countByBbox(@Param("envelope") Envelope envelope);
}