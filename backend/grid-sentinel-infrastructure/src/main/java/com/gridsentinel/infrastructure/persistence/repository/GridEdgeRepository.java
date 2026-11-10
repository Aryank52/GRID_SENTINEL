package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.grid.GridEdge;
import org.locationtech.jts.geom.Envelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GridEdge entities with spatial query capabilities.
 */
@Repository
public interface GridEdgeRepository extends JpaRepository<GridEdge, CommonKernel.GridEdgeId> {

    /**
     * Find grid edges within a bounding box (viewport query).
     */
    @Query(value = """
        SELECT e FROM GridEdge e
        WHERE ST_Intersects(e.geometry.geometry, :envelope)
        """)
    List<GridEdge> findByBbox(@Param("envelope") Envelope envelope);

    /**
     * Find grid edges by type within bounding box.
     */
    @Query(value = """
        SELECT e FROM GridEdge e
        WHERE ST_Intersects(e.geometry.geometry, :envelope)
        AND e.type = :type
        """)
    List<GridEdge> findByBboxAndType(@Param("envelope") Envelope envelope,
                                      @Param("type") String type);

    /**
     * Find grid edges connected to a specific node within bounding box.
     */
    @Query(value = """
        SELECT e FROM GridEdge e
        WHERE ST_Intersects(e.geometry.geometry, :envelope)
        AND (e.fromNodeId = :nodeId OR e.toNodeId = :nodeId)
        """)
    List<GridEdge> findByBboxAndNodeId(@Param("envelope") Envelope envelope,
                                        @Param("nodeId") CommonKernel.GridNodeId nodeId);

    /**
     * Find grid edges by voltage level range within bounding box.
     */
    @Query(value = """
        SELECT e FROM GridEdge e
        WHERE ST_Intersects(e.geometry.geometry, :envelope)
        AND e.voltageLevelKv BETWEEN :minKv AND :maxKv
        """)
    List<GridEdge> findByBboxAndVoltageRange(@Param("envelope") Envelope envelope,
                                              @Param("minKv") Double minKv,
                                              @Param("maxKv") Double maxKv);

    /**
     * Find grid edge by ID with geometry.
     */
    Optional<GridEdge> findById(CommonKernel.GridEdgeId id);

    /**
     * Find edges connected to a node (for topology inspection).
     */
    @Query(value = """
        SELECT e FROM GridEdge e
        WHERE e.fromNodeId = :nodeId OR e.toNodeId = :nodeId
        """)
    List<GridEdge> findByNodeId(@Param("nodeId") CommonKernel.GridNodeId nodeId);

    /**
     * Count grid edges within bounding box.
     */
    @Query(value = """
        SELECT COUNT(e) FROM GridEdge e
        WHERE ST_Intersects(e.geometry.geometry, :envelope)
        """)
    long countByBbox(@Param("envelope") Envelope envelope);
}