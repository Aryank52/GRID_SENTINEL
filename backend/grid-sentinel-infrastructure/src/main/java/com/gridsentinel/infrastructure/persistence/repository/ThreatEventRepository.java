package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ThreatEvent entities with spatial query capabilities.
 */
@Repository
public interface ThreatEventRepository extends JpaRepository<ThreatEvent, CommonKernel.ThreatEventId> {

    /**
     * Find threat events within a bounding box (viewport query).
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        """)
    List<ThreatEvent> findByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find threat events by severity within bounding box.
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        AND t.severity = :severity
        """)
    List<ThreatEvent> findByBboxAndSeverity(@Param("envelope") Envelope envelope,
                                             @Param("severity") String severity);

    /**
     * Find threat events by type within bounding box.
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        AND t.type = :type
        """)
    List<ThreatEvent> findByBboxAndType(@Param("envelope") Envelope envelope,
                                         @Param("type") String type);

    /**
     * Find threat events by status within bounding box.
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        AND t.status = :status
        """)
    List<ThreatEvent> findByBboxAndStatus(@Param("envelope") Envelope envelope,
                                           @Param("status") String status);

    /**
     * Find threat events by confidence threshold within bounding box.
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        AND t.confidence >= :minConfidence
        """)
    List<ThreatEvent> findByBboxAndMinConfidence(@Param("envelope") Envelope envelope,
                                                  @Param("minConfidence") Double minConfidence);

    /**
     * Find actionable threats (high confidence, actionable severity).
     */
    @Query(value = """
        SELECT t FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        AND t.confidence >= 0.7
        AND t.severity IN ('MEDIUM', 'HIGH', 'CRITICAL')
        AND t.status IN ('NEW', 'TRIAGED')
        ORDER BY t.severity DESC, t.confidence DESC, t.detectedAt ASC
        """)
    List<ThreatEvent> findActionableByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find threat event by ID with geometry.
     */
    Optional<ThreatEvent> findByIdWithGeometry(CommonKernel.ThreatEventId id);

    /**
     * Count threat events within bounding box.
     */
    @Query(value = """
        SELECT COUNT(t) FROM ThreatEvent t
        WHERE t.location.geometry && :envelope
        """)
    long countByBbox(@Param("envelope") Envelope envelope);
}