package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatStatus;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ThreatEvent entities.
 */
@Repository
public interface ThreatEventRepository extends JpaRepository<com.gridsentinel.detection.ThreatEvent, CommonKernel.ThreatEventId> {

    Optional<ThreatEvent> findById(CommonKernel.ThreatEventId id);

    List<ThreatEvent> findByStatus(ThreatStatus status);

    @Query("SELECT t FROM ThreatEvent t WHERE t.affectedAssetId = :assetId")
    List<ThreatEvent> findByAffectedAssetId(@Param("assetId") CommonKernel.AssetId assetId);

    @Query("SELECT t FROM ThreatEvent t WHERE t.nearbyGridNodeId = :nodeId")
    List<ThreatEvent> findByNearbyGridNodeId(@Param("nodeId") CommonKernel.GridNodeId nodeId);

    @Query("SELECT t FROM ThreatEvent t WHERE t.status IN ('NEW', 'TRIAGED', 'CONFIRMED') AND t.type = :type AND t.affectedAssetId = :assetId")
    Optional<ThreatEvent> findSimilar(@Param("type") com.gridsentinel.detection.ThreatType type, @Param("assetId") CommonKernel.AssetId assetId);

    @Query("SELECT t FROM ThreatEvent t WHERE t.correlationId = :correlationId")
    List<ThreatEvent> findByCorrelationId(@Param("correlationId") CommonKernel.CorrelationId correlationId);

    @Query("SELECT t FROM ThreatEvent t WHERE t.detectedAt BETWEEN :from AND :to")
    List<ThreatEvent> findByDetectedAtBetween(@Param("from") java.time.Instant from, @Param("to") java.time.Instant to);

    // Spatial query methods for ThreatEvent
    @Query(value = "SELECT t FROM ThreatEvent t WHERE ST_Intersects(t.location, :envelope) AND UPPER(t.severity) = UPPER(:severity)")
    List<ThreatEvent> findByBboxAndSeverity(@Param("envelope") Envelope envelope, @Param("severity") String severity);

    @Query(value = "SELECT t FROM ThreatEvent t WHERE ST_Intersects(t.location, :envelope) AND UPPER(t.type) = UPPER(:type)")
    List<ThreatEvent> findByBboxAndType(@Param("envelope") Envelope envelope, @Param("type") String type);

    @Query(value = "SELECT t FROM ThreatEvent t WHERE ST_Intersects(t.location, :envelope) AND UPPER(t.status) = UPPER(:status)")
    List<ThreatEvent> findByBboxAndStatus(@Param("envelope") Envelope envelope, @Param("status") String status);

    @Query(value = "SELECT t FROM ThreatEvent t WHERE ST_Intersects(t.location, :envelope) AND t.confidence >= :minConfidence")
    List<ThreatEvent> findByBboxAndMinConfidence(@Param("envelope") Envelope envelope, @Param("minConfidence") Double minConfidence);

    @Query(value = "SELECT t FROM ThreatEvent t WHERE ST_Intersects(t.location, :envelope)")
    List<ThreatEvent> findByBbox(@Param("envelope") Envelope envelope);
}