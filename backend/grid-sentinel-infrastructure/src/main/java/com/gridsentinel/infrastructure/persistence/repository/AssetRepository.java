package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.asset.Asset;
import com.gridsentinel.common.CommonKernel;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Asset entities with spatial query capabilities.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, CommonKernel.AssetId> {

    /**
     * Find assets within a bounding box (viewport query).
     * Uses PostGIS && operator for bbox intersection with spatial index.
     */
    @Query(value = """
        SELECT a FROM Asset a
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        AND a.type = :type
        """)
    List<Asset> findByBboxAndType(@Param("envelope") Envelope envelope,
                                   @Param("type") String type);

    /**
     * Find assets within a bounding box.
     */
    @Query(value = """
        SELECT a FROM Asset a
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        """)
    List<Asset> findByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find assets by criticality within bounding box.
     */
    @Query(value = """
        SELECT a FROM Asset a
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        AND a.criticality = :criticality
        """)
    List<Asset> findByBboxAndCriticality(@Param("envelope") Envelope envelope,
                                          @Param("criticality") String criticality);

    /**
     * Find assets by risk band (joins with latest risk score).
     */
    @Query(value = """
        SELECT a FROM Asset a
        LEFT JOIN risk_scores rs ON rs.asset_id = a.id
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        AND rs.band = :riskBand
        AND rs.computed_at = (
            SELECT MAX(rs2.computed_at) FROM risk_scores rs2 WHERE rs2.asset_id = a.id
        )
        """, nativeQuery = true)
    List<Asset> findByBboxAndRiskBand(@Param("envelope") Envelope envelope,
                                       @Param("riskBand") String riskBand);

    /**
     * Find assets by health index range within bounding box.
     */
    @Query(value = """
        SELECT a FROM Asset a
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        AND a.healthIndex BETWEEN :minHealth AND :maxHealth
        """)
    List<Asset> findByBboxAndHealthRange(@Param("envelope") Envelope envelope,
                                          @Param("minHealth") Double minHealth,
                                          @Param("maxHealth") Double maxHealth);

    /**
     * Find asset by ID with geometry.
     */
    Optional<Asset> findById(CommonKernel.AssetId id);

    /**
     * Count assets within bounding box.
     */
    @Query(value = """
        SELECT COUNT(a) FROM Asset a
        WHERE ST_Intersects(a.geometry.geometry, :envelope)
        """)
    long countByBbox(@Param("envelope") Envelope envelope);
}