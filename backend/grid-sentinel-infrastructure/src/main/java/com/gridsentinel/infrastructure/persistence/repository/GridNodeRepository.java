package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridNode;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GridNode entities with spatial query capabilities.
 */
@Repository
public interface GridNodeRepository extends JpaRepository<GridNode, CommonKernel.GridNodeId> {

    /**
     * Find grid nodes within a bounding box (viewport query).
     */
    @Query(value = """
        SELECT n FROM GridNode n
        WHERE ST_Intersects(n.geometry.geometry, :envelope)
        """)
    List<GridNode> findByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find grid nodes by type within bounding box.
     */
    @Query(value = """
        SELECT n FROM GridNode n
        WHERE ST_Intersects(n.geometry.geometry, :envelope)
        AND n.type = :type
        """)
    List<GridNode> findByBboxAndType(@Param("envelope") Envelope envelope,
                                      @Param("type") String type);

    /**
     * Find grid nodes by voltage level range within bounding box.
     */
    @Query(value = """
        SELECT n FROM GridNode n
        WHERE ST_Intersects(n.geometry.geometry, :envelope)
        AND n.voltageLevelKv BETWEEN :minKv AND :maxKv
        """)
    List<GridNode> findByBboxAndVoltageRange(@Param("envelope") Envelope envelope,
                                              @Param("minKv") Double minKv,
                                              @Param("maxKv") Double maxKv);

    /**
     * Find grid node by ID with geometry.
     */
    Optional<GridNode> findById(CommonKernel.GridNodeId id);

    /**
     * Count grid nodes within bounding box.
     */
    @Query(value = """
        SELECT COUNT(n) FROM GridNode n
        WHERE ST_Intersects(n.geometry.geometry, :envelope)
        """)
    long countByBbox(@Param("envelope") Envelope envelope);
}