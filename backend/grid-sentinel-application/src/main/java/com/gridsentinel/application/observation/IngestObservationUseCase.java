package com.gridsentinel.application.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatStatus;
import com.gridsentinel.observation.Evidence;
import com.gridsentinel.observation.EvidenceType;
import com.gridsentinel.observation.Observation;
import com.gridsentinel.observation.ValidationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Use case for ingesting and validating observations.
 * <p>
 * This is the entry point of the detection pipeline. Observations are received,
 * validated, and evidence is extracted for the detection pipeline.
 * </p>
 */
@Service
@Transactional
public class IngestObservationUseCase {

    private final ObservationRepository observationRepository;
    private final EvidenceExtractor evidenceExtractor;
    private final ThreatDetectionPipeline threatDetectionPipeline;

    public IngestObservationUseCase(
            ObservationRepository observationRepository,
            EvidenceExtractor evidenceExtractor,
            ThreatDetectionPipeline threatDetectionPipeline
    ) {
        this.observationRepository = observationRepository;
        this.evidenceExtractor = evidenceExtractor;
        this.threatDetectionPipeline = threatDetectionPipeline;
    }

    /**
     * Ingest a raw observation, validate it, extract evidence, and trigger threat detection.
     *
     * @param command the observation ingestion command
     * @return the ingested observation ID
     */
    public CommonKernel.ObservationId ingest(IngestObservationCommand command) {
        // Create observation entity
        Observation observation = buildObservation(command);

        // Persist observation
        Observation saved = observationRepository.save(observation);

        // Extract evidence from the observation
        Set<Evidence> evidence = evidenceExtractor.extract(saved);

        // Trigger threat detection pipeline asynchronously
        if (!evidence.isEmpty()) {
            threatDetectionPipeline.processEvidence(saved.getId(), evidence);
        }

        return saved.getId();
    }

    /**
     * Validate an existing observation (operator review).
     */
    public void validateObservation(CommonKernel.ObservationId id, ValidationStatus status, String notes, CommonKernel.UserId validatedBy) {
        Observation observation = observationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Observation not found: " + id));
        observation.markValidated(status, notes, validatedBy);
        observationRepository.save(observation);
    }

    private Observation buildObservation(IngestObservationCommand command) {
        return Observation.builder()
                .sourceType(command.sourceType())
                .sourceIdentifier(command.sourceIdentifier())
                .timestamp(command.timestamp())
                .payload(command.payload())
                .associatedAssetId(command.associatedAssetId())
                .nearbyGridNodeId(command.nearbyGridNodeId())
                .geometry(command.geometry())
                .build();
    }

    /**
     * Command for ingesting an observation.
     */
    public record IngestObservationCommand(
            com.gridsentinel.common.ObservationSourceType sourceType,
            String sourceIdentifier,
            java.time.Instant timestamp,
            String payload,
            com.gridsentinel.common.CommonKernel.AssetId associatedAssetId,
            com.gridsentinel.common.CommonKernel.GridNodeId nearbyGridNodeId,
            com.gridsentinel.common.CommonKernel.GeometryValueObject geometry
    ) {
        public IngestObservationCommand {
            if (sourceType == null) throw new IllegalArgumentException("Source type cannot be null");
            if (sourceIdentifier == null || sourceIdentifier.isBlank()) throw new IllegalArgumentException("Source identifier cannot be blank");
            if (timestamp == null) throw new IllegalArgumentException("Timestamp cannot be null");
            if (payload == null || payload.isBlank()) throw new IllegalArgumentException("Payload cannot be blank");
        }
    }

    /**
     * Port for observation persistence.
     */
    public interface ObservationRepository {
        Observation save(Observation observation);
        java.util.Optional<Observation> findById(com.gridsentinel.common.CommonKernel.ObservationId id);
        java.util.List<Observation> findUnvalidated();
        java.util.List<Observation> findByAssetId(com.gridsentinel.common.CommonKernel.AssetId assetId);
        java.util.List<Observation> findByGridNodeId(com.gridsentinel.common.CommonKernel.GridNodeId nodeId);
        java.util.List<Observation> findByTimestampRange(java.time.Instant from, java.time.Instant to);
    }

    /**
     * Service for extracting evidence from validated observations.
     */
    public interface EvidenceExtractor {
        Set<Evidence> extract(Observation observation);
    }

    /**
     * Pipeline for processing extracted evidence and detecting threats.
     */
    public interface ThreatDetectionPipeline {
        void processEvidence(com.gridsentinel.common.CommonKernel.ObservationId observationId, Set<Evidence> evidence);
    }

    /**
     * Repository for threat events.
     */
    public interface ThreatEventRepository {
        ThreatEvent save(ThreatEvent threat);
        java.util.Optional<ThreatEvent> findById(com.gridsentinel.common.CommonKernel.ThreatEventId id);
        java.util.Optional<ThreatEvent> findSimilar(ThreatEvent threat);
        java.util.List<ThreatEvent> findByStatus(ThreatStatus status);
        java.util.List<ThreatEvent> findByAssetId(com.gridsentinel.common.CommonKernel.AssetId assetId);
        java.util.List<ThreatEvent> findByGridNodeId(com.gridsentinel.common.CommonKernel.GridNodeId nodeId);
    }

    /**
     * Store for evidence with deduplication and time-based queries.
     */
    public interface EvidenceStore {
        void store(com.gridsentinel.common.CommonKernel.ObservationId observationId, Set<Evidence> evidence);
        java.util.List<Evidence> getRecentEvidence(java.time.Instant since);
        java.util.List<Evidence> getEvidenceForLocation(LocationKey key);
        void removeExpired(java.time.Instant before);
    }

    /**
     * Registry of threat detection strategies.
     */
    public interface ThreatDetectionStrategyRegistry {
        java.util.List<ThreatDetectionStrategy> getStrategies();
    }

    /**
     * Strategy for detecting threats from evidence.
     */
    public interface ThreatDetectionStrategy {
        boolean appliesTo(LocationKey location, java.util.List<Evidence> evidence);
        java.util.Optional<ThreatEvent> detect(LocationKey location, java.util.List<Evidence> evidence);
    }

    /**
     * Key for grouping evidence by location.
     */
    public record LocationKey(com.gridsentinel.common.CommonKernel.ObservationId observationId) {}

    /**
     * Event publisher for real-time events.
     */
    public interface EventPublisher {
        void publishThreatDetected(ThreatEvent threat);
    }
}