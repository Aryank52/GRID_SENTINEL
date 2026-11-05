package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.risk.RiskScore;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RiskScore entities with spatial query capabilities (via asset join).
 */
@Repository
public interface RiskScoreRepository extends JpaRepository<RiskScore, CommonKernel.RiskScoreId> {

    /**
     * Find latest risk scores for assets within bounding box.
     * Joins with assets table to filter by asset geometry.
     */
    @Query(value = """
        SELECT rs FROM RiskScore rs
        JOIN rs.assetId a
        WHERE a.geometry.geometry && :envelope
        AND rs.computedAt = (
            SELECT MAX(rs2.computedAt) FROM RiskScore rs2 WHERE rs2.assetId = a.id
        )
        """)
    List<RiskScore> findLatestByAssetBbox(@Param("envelope") Envelope envelope);

    /**
     * Find latest risk scores by band within bounding box.
     */
    @Query(value = """
        SELECT rs FROM RiskScore rs
        JOIN rs.assetId a
        WHERE a.geometry.geometry && :envelope
        AND rs.band = :band
        AND rs.computedAt = (
            SELECT MAX(rs2.computedAt) FROM RiskScore rs2 WHERE rs2.assetId = a.id
        )
        """)
    List<RiskScore> findLatestByAssetBboxAndBand(@Param("envelope") Envelope envelope,
                                                  @Param("band") String band);

    /**
     * Find latest risk scores by score range within bounding box.
     */
    @Query(value = """
        SELECT rs FROM RiskScore rs
        JOIN rs.assetId a
        WHERE a.geometry.geometry && :envelope
        AND rs.score BETWEEN :minScore AND :maxScore
        AND rs.computedAt = (
            SELECT MAX(rs2.computedAt) FROM RiskScore rs2 WHERE rs2.assetId = a.id
        )
        """)
    List<RiskScore> findLatestByAssetBboxAndScoreRange(@Param("envelope") Envelope envelope,
                                                        @Param("minScore") Double minScore,
                                                        @Param("maxScore") Double maxScore);

    /**
     * Find latest risk score for a specific asset.
     */
    @Query(value = """
        SELECT rs FROM RiskScore rs
        WHERE rs.assetId = :assetId
        ORDER BY rs.computedAt DESC
        """)
    List<RiskScore> findLatestByAssetId(@Param("assetId") CommonKernel.AssetId assetId);

    /**
     * Find latest risk score for a specific asset (single result).
     */
    @Query(value = """
        SELECT rs FROM RiskScore rs
        WHERE rs.assetId = :assetId
        ORDER BY rs.computedAt DESC
        """)
    Optional<RiskScore> findTopByAssetIdOrderByComputedAtDesc(@Param("assetId") CommonKernel.AssetId assetId);
}