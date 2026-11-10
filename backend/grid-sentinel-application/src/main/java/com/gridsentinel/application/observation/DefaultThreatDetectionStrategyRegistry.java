package com.gridsentinel.application.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.common.ThreatSeverity;
import com.gridsentinel.detection.ThreatEvent;
import com.gridsentinel.detection.ThreatType;
import com.gridsentinel.observation.Evidence;
import com.gridsentinel.observation.EvidenceType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default threat detection strategy registry with built-in strategies.
 */
public class DefaultThreatDetectionStrategyRegistry implements IngestObservationUseCase.ThreatDetectionStrategyRegistry {
    private final List<IngestObservationUseCase.ThreatDetectionStrategy> strategies = new ArrayList<>();

    public DefaultThreatDetectionStrategyRegistry() {
        strategies.add(new ThermalThreatStrategy());
        strategies.add(new StructuralThreatStrategy());
        strategies.add(new FireThreatStrategy());
        strategies.add(new WeatherThreatStrategy());
        strategies.add(new ScadaThreatStrategy());
        strategies.add(new SecurityThreatStrategy());
    }

    @Override
    public List<IngestObservationUseCase.ThreatDetectionStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    private static class ThermalThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.TEMPERATURE_ANOMALY ||
                            e.getType() == EvidenceType.HOTSPOT_DETECTED ||
                            e.getType() == EvidenceType.THERMAL_DELTA
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> thermal = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.TEMPERATURE_ANOMALY ||
                            e.getType() == EvidenceType.HOTSPOT_DETECTED ||
                            e.getType() == EvidenceType.THERMAL_DELTA)
                    .toList();

            if (thermal.isEmpty()) return Optional.empty();

            double maxConfidence = thermal.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);
            double avgValue = thermal.stream().mapToDouble(Evidence::getValue).average().orElse(0.0);

            ThreatSeverity severity = avgValue > 0.8 && evidence.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0) > 0.8 ?
                    ThreatSeverity.CRITICAL :
                    (avgValue > 0.6 && evidence.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0) > 0.6 ?
                            ThreatSeverity.HIGH :
                            ThreatSeverity.MEDIUM);

            ThreatEvent threat = ThreatEvent.builder()
                    .type(ThreatType.OVERHEATING_TRANSFORMER)
                    .severity(severity)
                    .confidence(Math.min(1.0, evidence.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0) + 0.1))
                    .detectedAt(Instant.now())
                    .description("Thermal anomaly detected on asset")
                    .rationale("Thermal evidence: " + thermal.size() + " observations")
                    .supportingEvidence(new HashSet<>(thermal))
                    .detectionStrategy("thermal-anomaly-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }

    private static class StructuralThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.STRUCTURAL_DAMAGE ||
                            e.getType() == EvidenceType.CORROSION_DETECTED ||
                            e.getType() == EvidenceType.STRUCTURAL_CORROSION ||
                            e.getType() == EvidenceType.INSULATOR_DAMAGE
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> structural = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.STRUCTURAL_DAMAGE ||
                            e.getType() == EvidenceType.CORROSION_DETECTED ||
                            e.getType() == EvidenceType.STRUCTURAL_CORROSION ||
                            e.getType() == EvidenceType.INSULATOR_DAMAGE)
                    .toList();

            if (structural.isEmpty()) return Optional.empty();

            double maxConfidence = structural.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);
            double avgValue = structural.stream().mapToDouble(Evidence::getValue).average().orElse(0.0);

            ThreatSeverity severity = avgValue > 0.7 && evidence.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0) > 0.7 ?
                    ThreatSeverity.HIGH :
                    ThreatSeverity.MEDIUM;

            ThreatType threatType = evidence.stream()
                    .anyMatch(e -> e.getType() == EvidenceType.INSULATOR_DAMAGE) ?
                            ThreatType.INSULATION_FAILURE :
                            (evidence.stream().anyMatch(e -> e.getType() == EvidenceType.CORROSION_DETECTED) ?
                                    ThreatType.CORROSION :
                                    ThreatType.STRUCTURAL_FAILURE);

            ThreatEvent threat = ThreatEvent.builder()
                    .type(threatType)
                    .severity(severity)
                    .confidence(Math.min(1.0, structural.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0) + 0.1))
                    .detectedAt(Instant.now())
                    .description("Structural threat detected on asset")
                    .rationale("Structural evidence: " + structural.size() + " observations")
                    .supportingEvidence(new HashSet<>(structural))
                    .detectionStrategy("structural-threat-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }

    private static class FireThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.FIRE_DETECTED ||
                            e.getType() == EvidenceType.SMOKE_DETECTED ||
                            e.getType() == EvidenceType.FIRE
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> fire = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.FIRE_DETECTED ||
                            e.getType() == EvidenceType.SMOKE_DETECTED ||
                            e.getType() == EvidenceType.FIRE)
                    .toList();

            if (fire.isEmpty()) return Optional.empty();

            double maxConfidence = fire.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);

            ThreatEvent threat = ThreatEvent.builder()
                    .type(ThreatType.FIRE)
                    .severity(ThreatSeverity.CRITICAL)
                    .confidence(Math.min(1.0, maxConfidence + 0.15))
                    .detectedAt(Instant.now())
                    .description("Fire detected")
                    .rationale("Fire evidence: " + fire.size() + " observations")
                    .supportingEvidence(new HashSet<>(fire))
                    .detectionStrategy("fire-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }

    private static class WeatherThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.EXTREME_WIND ||
                            e.getType() == EvidenceType.LIGHTNING_STRIKE_PROXIMITY ||
                            e.getType() == EvidenceType.FLOODING ||
                            e.getType() == EvidenceType.FLOOD_PROXIMITY
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> weather = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.EXTREME_WIND ||
                            e.getType() == EvidenceType.LIGHTNING_STRIKE_PROXIMITY ||
                            e.getType() == EvidenceType.FLOODING ||
                            e.getType() == EvidenceType.FLOOD_PROXIMITY)
                    .toList();

            if (weather.isEmpty()) return Optional.empty();

            ThreatType threatType;
            if (weather.stream().anyMatch(e -> e.getType() == EvidenceType.LIGHTNING_STRIKE_PROXIMITY)) {
                threatType = ThreatType.LIGHTNING_STRIKE;
            } else if (weather.stream().anyMatch(e -> e.getType() == EvidenceType.FLOODING)) {
                threatType = ThreatType.FLOODING;
            } else {
                threatType = ThreatType.EXTREME_WIND;
            }

            double maxConfidence = weather.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);
            ThreatSeverity severity = maxConfidence > 0.8 ?
                    ThreatSeverity.HIGH :
                    ThreatSeverity.MEDIUM;

            ThreatEvent threat = ThreatEvent.builder()
                    .type(threatType)
                    .severity(severity)
                    .confidence(Math.min(1.0, maxConfidence + 0.1))
                    .detectedAt(Instant.now())
                    .description("Weather threat detected")
                    .rationale("Weather evidence: " + weather.size() + " observations")
                    .supportingEvidence(new HashSet<>(weather))
                    .detectionStrategy("weather-threat-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }

    private static class ScadaThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.CURRENT_OVERLOAD ||
                            e.getType() == EvidenceType.VOLTAGE_DEVIATION ||
                            e.getType() == EvidenceType.FREQUENCY_DEVIATION
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> scada = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.CURRENT_OVERLOAD ||
                            e.getType() == EvidenceType.VOLTAGE_DEVIATION ||
                            e.getType() == EvidenceType.FREQUENCY_DEVIATION)
                    .toList();

            if (scada.isEmpty()) return Optional.empty();

            double maxConfidence = scada.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);

            ThreatEvent threat = ThreatEvent.builder()
                    .type(ThreatType.OVERLOAD)
                    .severity(maxConfidence > 0.8 ? ThreatSeverity.HIGH : ThreatSeverity.MEDIUM)
                    .confidence(Math.min(1.0, maxConfidence + 0.1))
                    .detectedAt(Instant.now())
                    .description("Electrical anomaly detected")
                    .rationale("SCADA evidence: " + scada.size() + " observations")
                    .supportingEvidence(new HashSet<>(scada))
                    .detectionStrategy("scada-threat-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }

    private static class SecurityThreatStrategy implements IngestObservationUseCase.ThreatDetectionStrategy {
        @Override
        public boolean appliesTo(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            return evidence.stream().anyMatch(e ->
                    e.getType() == EvidenceType.PHYSICAL_INTRUSION ||
                            e.getType() == EvidenceType.CYBER_INTRUSION ||
                            e.getType() == EvidenceType.GAS_LEAK ||
                            e.getType() == EvidenceType.MECHANICAL_ANOMALY
            );
        }

        @Override
        public Optional<ThreatEvent> detect(IngestObservationUseCase.LocationKey location, List<Evidence> evidence) {
            List<Evidence> security = evidence.stream()
                    .filter(e -> e.getType() == EvidenceType.PHYSICAL_INTRUSION ||
                            e.getType() == EvidenceType.CYBER_INTRUSION ||
                            e.getType() == EvidenceType.GAS_LEAK ||
                            e.getType() == EvidenceType.MECHANICAL_ANOMALY)
                    .toList();

            if (security.isEmpty()) return Optional.empty();

            ThreatType threatType;
            if (security.stream().anyMatch(e -> e.getType() == EvidenceType.CYBER_INTRUSION)) {
                threatType = ThreatType.CYBER_INTRUSION;
            } else if (security.stream().anyMatch(e -> e.getType() == EvidenceType.PHYSICAL_INTRUSION)) {
                threatType = ThreatType.PHYSICAL_INTRUSION;
            } else {
                threatType = ThreatType.UNAUTHORIZED_ACCESS;
            }

            double maxConfidence = security.stream().mapToDouble(Evidence::getConfidence).max().orElse(0.0);
            ThreatSeverity severity = maxConfidence > 0.7 ?
                    ThreatSeverity.HIGH :
                    ThreatSeverity.MEDIUM;

            ThreatEvent threat = ThreatEvent.builder()
                    .type(threatType)
                    .severity(severity)
                    .confidence(Math.min(1.0, maxConfidence + 0.1))
                    .detectedAt(Instant.now())
                    .description("Security threat detected")
                    .rationale("Security evidence: " + security.size() + " observations")
                    .supportingEvidence(new HashSet<>(security))
                    .detectionStrategy("security-threat-detection-v1")
                    .strategyVersion("1.0")
                    .build();

            return Optional.of(threat);
        }
    }
}