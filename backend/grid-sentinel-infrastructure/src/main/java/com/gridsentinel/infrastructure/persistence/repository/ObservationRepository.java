package com.gridsentinel.infrastructure.persistence.repository;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.observation.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Observation entities.
 */
@Repository
public interface ObservationRepository extends JpaRepository<Observation, CommonKernel.ObservationId> {

    Optional<Observation> findById(CommonKernel.ObservationId id);

    List<Observation> findByValidationStatus(com.gridsentinel.observation.ValidationStatus status);

    @Query("SELECT o FROM Observation o WHERE o.associatedAssetId = :assetId")
    List<Observation> findByAssociatedAssetId(@Param("assetId") CommonKernel.AssetId assetId);

    @Query("SELECT o FROM Observation o WHERE o.nearbyGridNodeId = :nodeId")
    List<Observation> findByNearbyGridNodeId(@Param("nodeId") CommonKernel.GridNodeId nodeId);

    @Query("SELECT o FROM Observation o WHERE o.timestamp BETWEEN :from AND :to")
    List<Observation> findByTimestampRange(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT o FROM Observation o WHERE o.validationStatus = 'PENDING' ORDER BY o.createdAt ASC")
    List<Observation> findUnvalidated();
}