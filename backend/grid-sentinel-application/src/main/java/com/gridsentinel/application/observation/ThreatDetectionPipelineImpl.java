package com.gridsentinel.application.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.observation.Evidence;
import com.gridsentinel.observation.EvidenceType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the threat detection pipeline.
 */
@Component
public class ThreatDetectionPipelineImpl implements IngestObservationUseCase.ThreatDetectionPipeline {

    private final IngestObservationUseCase.ThreatEventRepository threatEventRepository;
    private final IngestObservationUseCase.EvidenceStore evidenceStore;
    private final IngestObservationUseCase.ThreatDetectionStrategyRegistry strategyRegistry;
    private final IngestObservationUseCase.EventPublisher eventPublisher;

    // High severity evidence types
    private static final Set<EvidenceType> HIGH_SEVERITY_EVIDENCE = Set.of(
            EvidenceType.FIRE_DETECTED,
            EvidenceType.FIRE,
            EvidenceType.CYBER_INTRUSION,
            EvidenceType.LIGHTNING_STRIKE_PROXIMITY,
            EvidenceType.WILDFIRE_PERIMETER,
            EvidenceType.EXTREME_WIND,
            EvidenceType.CURRENT_OVERLOAD,
            EvidenceType.VOLTAGE_DEVIATION,
            EvidenceType.FREQUENCY_DEVIATION,
            EvidenceType.INSULATOR_DAMAGE,
            EvidenceType.PHYSICAL_INTRUSION,
            EvidenceType.GAS_LEAK,
            EvidenceType.HOTSPOT_DETECTED
    );

    // Medium severity evidence types
    private static final Set<EvidenceType> MEDIUM_SEVERITY_EVIDENCE = Set.of(
            EvidenceType.TEMPERATURE_ANOMALY,
            EvidenceType.THERMAL_GRADIENT,
            EvidenceType.THERMAL_DELTA,
            EvidenceType.VEGETATION_ENCROACHMENT,
            EvidenceType.STRUCTURAL_DAMAGE,
            EvidenceType.CORROSION_DETECTED,
            EvidenceType.INSULATOR_DAMAGE,
            EvidenceType.FOREIGN_OBJECT,
            EvidenceType.STRUCTURAL_CORROSION,
            EvidenceType.FLOOD_PROXIMITY,
            EvidenceType.LANDSLIDE_RISK_ZONE,
            EvidenceType.SEISMIC_ZONE,
            EvidenceType.WIND_SPEED,
            EvidenceType.WIND_GUST,
            EvidenceType.PRECIPITATION_RATE,
            EvidenceType.TEMPERATURE_EXTREME,
            EvidenceType.POWER_SWING,
            EvidenceType.PROTECTION_OPERATION,
            EvidenceType.COMMUNICATION_LOSS,
            EvidenceType.INSPECTION_FINDING,
            EvidenceType.CONDITION_ASSESSMENT,
            EvidenceType.LANDSLIDE_RISK,
            EvidenceType.HIGH_HUMIDITY,
            EvidenceType.MECHANICAL_ANOMALY,
            EvidenceType.FLOODING
    );

    public ThreatDetectionPipelineImpl(
            IngestObservationUseCase.ThreatEventRepository threatEventRepository,
            IngestObservationUseCase.EvidenceStore evidenceStore,
            IngestObservationUseCase.ThreatDetectionStrategyRegistry strategyRegistry,
            IngestObservationUseCase.EventPublisher eventPublisher
    ) {
        this.threatEventRepository = threatEventRepository;
        this.evidenceStore = evidenceStore;
        this.strategyRegistry = strategyRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void processEvidence(CommonKernel.ObservationId observationId, Set<Evidence> evidence) {
        evidenceStore.store(observationId, evidence);

        List<Evidence> recentEvidence = evidenceStore.getRecentEvidence(Instant.now().minusSeconds(3600));
        Map<IngestObservationUseCase.LocationKey, List<Evidence>> grouped = groupByLocation(recentEvidence);

        for (Map.Entry<IngestObservationUseCase.LocationKey, List<Evidence>> entry : grouped.entrySet()) {
            List<Evidence> locationEvidence = entry.getValue();
            if (locationEvidence.size() < 2) continue;

            for (IngestObservationUseCase.ThreatDetectionStrategy strategy : strategyRegistry.getStrategies()) {
                if (strategy.appliesTo(entry.getKey(), locationEvidence)) {
                    Optional<ThreatEvent> threat = strategy.detect(entry.getKey(), locationEvidence);
                    threat.ifPresent(this::publishThreat);
                }
            }
        }
    }

    private void publishThreat(ThreatEvent threat) {
        Optional<ThreatEvent> existing = threatEventRepository.findSimilar(threat);
        if (existing.isPresent()) {
            ThreatEvent existingThreat = existing.get();
            threat.getSupportingEvidence().forEach(existingThreat::addEvidence);
            recalculateThreat(existingThreat);
            threatEventRepository.save(existingThreat);
            return;
        }

        ThreatEvent saved = threatEventRepository.save(threat);
        eventPublisher.publishThreatDetected(saved);
    }

    private void recalculateThreat(ThreatEvent threat) {
        double totalWeight = threat.getSupportingEvidence().stream()
                .mapToDouble(Evidence::weightedValue)
                .sum();
        double maxPossible = threat.getSupportingEvidence().size();
        double newConfidence = Math.min(1.0, totalWeight / maxPossible);

        ThreatSeverity newSeverity = calculateSeverity(threat);
        if (newSeverity.ordinal() > threat.getSeverity().ordinal()) {
            threat.escalateSeverity(newSeverity);
        }
    }

    private ThreatSeverity calculateSeverity(ThreatEvent threat) {
        boolean hasHighSeverityEvidence = threat.getSupportingEvidence().stream()
                .anyMatch(e -> HIGH_SEVERITY_EVIDENCE.contains(e.getType()) && e.supportsThreat(0.7));

        boolean hasMediumEvidence = threat.getSupportingEvidence().stream()
                .anyMatch(e -> MEDIUM_SEVERITY_EVIDENCE.contains(e.getType()) && e.supportsThreat(0.5));

        double avgConfidence = threat.getSupportingEvidence().stream()
                .mapToDouble(Evidence::getConfidence)
                .average()
                .orElse(0.0);

        if (hasHighSeverityEvidence && avgConfidence > 0.8) {
            return ThreatSeverity.CRITICAL;
        } else if (hasHighSeverityEvidence || (hasMediumEvidence && avgConfidence > 0.7)) {
            return ThreatSeverity.HIGH;
        } else if (hasMediumEvidence || avgConfidence > 0.6) {
            return ThreatSeverity.MEDIUM;
        } else if (avgConfidence > 0.4) {
            return ThreatSeverity.LOW;
        }
        return ThreatSeverity.INFO;
    }

    private Map<IngestObservationUseCase.LocationKey, List<Evidence>> groupByLocation(List<Evidence> evidence) {
        return evidence.stream()
                .filter(e -> e.getSourceObservationId() != null)
                .collect(Collectors.groupingBy(e -> new IngestObservationUseCase.LocationKey(e.getSourceObservationId())));
    }
}