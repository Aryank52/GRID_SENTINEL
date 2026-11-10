package com.gridsentinel.application.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.observation.Evidence;
import com.gridsentinel.observation.EvidenceType;
import com.gridsentinel.observation.Observation;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementation of evidence extraction from observations.
 * <p>
 * Extracts structured evidence from raw observation payloads based on source type.
 * Each extractor handles a specific source type and converts raw payload into
 * typed, quantified evidence with confidence scores.
 * </p>
 */
@Component
public class EvidenceExtractorImpl implements IngestObservationUseCase.EvidenceExtractor {

    private static final Pattern THERMAL_TEMP_PATTERN = Pattern.compile("(?i)temperature[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern THERMAL_DELTA_PATTERN = Pattern.compile("(?i)delta[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern WIND_SPEED_PATTERN = Pattern.compile("(?i)wind[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern HUMIDITY_PATTERN = Pattern.compile("(?i)humidity[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern VOLTAGE_PATTERN = Pattern.compile("(?i)voltage[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern CURRENT_PATTERN = Pattern.compile("(?i)current[\\s:=]+(\\d+(?:\\.\\d+)?)");
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("(?i)frequency[\\s:=]+(\\d+(?:\\.\\d+)?)");

    @Override
    public Set<Evidence> extract(Observation observation) {
        return switch (observation.getSourceType()) {
            case THERMAL_CAMERA -> extractThermalEvidence(observation);
            case SCADA_TELEMETRY -> extractScadaEvidence(observation);
            case VISUAL_CAMERA -> extractVisualEvidence(observation);
            case WEATHER_STATION -> extractWeatherEvidence(observation);
            case DRONE_INSPECTION -> extractDroneInspectionEvidence(observation);
            case SATELLITE_IMAGERY -> extractSatelliteEvidence(observation);
            case GIS_LAYER -> extractGisEvidence(observation);
            case GROUND_INSPECTION -> extractGroundInspectionEvidence(observation);
            case OPERATOR_MANUAL -> extractOperatorManualEvidence(observation);
            case SOCIAL_MEDIA_REPORT -> extractSocialMediaEvidence(observation);
            default -> extractGenericEvidence(observation);
        };
    }

    private Set<Evidence> extractThermalEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload();

        // Extract temperature
        var tempMatcher = THERMAL_TEMP_PATTERN.matcher(payload);
        if (tempMatcher.find()) {
            double temp = Double.parseDouble(tempMatcher.group(1));
            // Normalize: assume 40°C baseline, 80°C critical
            double normalized = Math.min(1.0, Math.max(0.0, (temp - 40.0) / 40.0));
            evidence.add(Evidence.builder()
                    .type(EvidenceType.TEMPERATURE_ANOMALY)
                    .value(normalized)
                    .unit("normalized")
                    .confidence(0.85)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("thermal-extractor-v1")
                    .metadata("temperature_celsius=" + temp)
                    .build());
        }

        // Extract temperature delta
        var deltaMatcher = THERMAL_DELTA_PATTERN.matcher(payload);
        if (deltaMatcher.find()) {
            double delta = Double.parseDouble(deltaMatcher.group(1));
            // Normalize: 0-10°C delta
            double normalized = Math.min(1.0, delta / 10.0);
            evidence.add(Evidence.builder()
                    .type(EvidenceType.THERMAL_DELTA)
                    .value(normalized)
                    .unit("normalized")
                    .confidence(0.8)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("thermal-extractor-v1")
                    .metadata("delta_celsius=" + delta)
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractScadaEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload();

        // Extract voltage
        var voltageMatcher = VOLTAGE_PATTERN.matcher(payload);
        if (voltageMatcher.find()) {
            double voltage = Double.parseDouble(voltageMatcher.group(1));
            // Normalize: assume nominal 1.0 pu, 1.1 pu is high
            double normalized = Math.max(0.0, (voltage - 1.0) / 0.1);
            evidence.add(Evidence.builder()
                    .type(EvidenceType.VOLTAGE_DEVIATION)
                    .value(Math.min(1.0, normalized))
                    .unit("normalized")
                    .confidence(0.9)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("scada-extractor-v1")
                    .metadata("voltage_pu=" + voltage)
                    .build());
        }

        // Extract current
        var currentMatcher = CURRENT_PATTERN.matcher(payload);
        if (currentMatcher.find()) {
            double current = Double.parseDouble(currentMatcher.group(1));
            // Normalize: assume 1.0 pu nominal
            double normalized = Math.max(0.0, (current - 1.0) / 0.5);
            evidence.add(Evidence.builder()
                    .type(EvidenceType.CURRENT_OVERLOAD)
                    .value(Math.min(1.0, normalized))
                    .unit("normalized")
                    .confidence(0.9)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("scada-extractor-v1")
                    .metadata("current_pu=" + current)
                    .build());
        }

        // Extract frequency
        var freqMatcher = FREQUENCY_PATTERN.matcher(payload);
        if (freqMatcher.find()) {
            double freq = Double.parseDouble(freqMatcher.group(1));
            // Normalize: nominal 50/60Hz, deviation
            double deviation = Math.abs(freq - 50.0);
            double normalized = Math.min(1.0, deviation / 2.0);
            evidence.add(Evidence.builder()
                    .type(EvidenceType.FREQUENCY_DEVIATION)
                    .value(normalized)
                    .unit("normalized")
                    .confidence(0.85)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("scada-extractor-v1")
                    .metadata("frequency_hz=" + freq)
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractVisualEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("corrosion") || payload.contains("rust")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.STRUCTURAL_CORROSION)
                    .value(0.8)
                    .unit("normalized")
                    .confidence(0.75)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("visual-extractor-v1")
                    .metadata("keywords=corrosion,rust")
                    .build());
        }

        if (payload.contains("damage") || payload.contains("crack") || payload.contains("break")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.STRUCTURAL_DAMAGE)
                    .value(0.85)
                    .unit("normalized")
                    .confidence(0.8)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("visual-extractor-v1")
                    .metadata("keywords=damage,crack,break")
                    .build());
        }

        if (payload.contains("vegetation") || payload.contains("encroach")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.VEGETATION_ENCROACHMENT)
                    .value(0.75)
                    .unit("normalized")
                    .confidence(0.7)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("visual-extractor-v1")
                    .metadata("keywords=vegetation,encroachment")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractWeatherEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload();

        // Extract wind speed
        var windMatcher = WIND_SPEED_PATTERN.matcher(payload);
        if (windMatcher.find()) {
            double wind = Double.parseDouble(windMatcher.group(1));
            // Normalize: 0-50 m/s
            double normalized = Math.min(1.0, wind / 50.0);
            evidence.add(Evidence.builder()
                    .type(EvidenceType.EXTREME_WIND)
                    .value(normalized)
                    .unit("normalized")
                    .confidence(0.85)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("weather-extractor-v1")
                    .metadata("wind_speed_m_s=" + wind)
                    .build());
        }

        // Extract humidity
        var humidityMatcher = HUMIDITY_PATTERN.matcher(payload);
        if (humidityMatcher.find()) {
            double humidity = Double.parseDouble(humidityMatcher.group(1));
            // Normalize: 0-100%
            double normalized = humidity / 100.0;
            evidence.add(Evidence.builder()
                    .type(EvidenceType.HIGH_HUMIDITY)
                    .value(normalized)
                    .unit("normalized")
                    .confidence(0.75)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("weather-extractor-v1")
                    .metadata("humidity_percent=" + humidity)
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractDroneInspectionEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("crack") || payload.contains("fracture")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.STRUCTURAL_DAMAGE)
                    .value(0.9)
                    .unit("normalized")
                    .confidence(0.9)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("drone-extractor-v1")
                    .metadata("keywords=crack,fracture")
                    .build());
        }

        if (payload.contains("hotspot") || payload.contains("hot spot")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.HOTSPOT_DETECTED)
                    .value(0.85)
                    .unit("normalized")
                    .confidence(0.85)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("drone-extractor-v1")
                    .metadata("keywords=hotspot")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractSatelliteEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("fire") || payload.contains("smoke") || payload.contains("burn")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.FIRE)
                    .value(0.95)
                    .unit("normalized")
                    .confidence(0.9)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("satellite-extractor-v1")
                    .metadata("keywords=fire,smoke,burn")
                    .build());
        }

        if (payload.contains("flood") || payload.contains("water")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.FLOODING)
                    .value(0.85)
                    .unit("normalized")
                    .confidence(0.8)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("satellite-extractor-v1")
                    .metadata("keywords=flood,water")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractGisEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("landslide") || payload.contains("slope")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.LANDSLIDE_RISK)
                    .value(0.7)
                    .unit("normalized")
                    .confidence(0.65)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("gis-extractor-v1")
                    .metadata("keywords=landslide,slope")
                    .build());
        }

        if (payload.contains("flood") || payload.contains("floodplain")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.FLOOD_PROXIMITY)
                    .value(0.75)
                    .unit("normalized")
                    .confidence(0.7)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("gis-extractor-v1")
                    .metadata("keywords=flood,floodplain")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractGroundInspectionEvidence(Observation obs) {
        // Ground inspection is similar to visual but more detailed
        return extractVisualEvidence(obs);
    }

    private Set<Evidence> extractOperatorManualEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("smell") || payload.contains("odor") || payload.contains("gas")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.GAS_LEAK)
                    .value(0.8)
                    .unit("normalized")
                    .confidence(0.7)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("operator-extractor-v1")
                    .metadata("keywords=smell,odor,gas")
                    .build());
        }

        if (payload.contains("noise") || payload.contains("vibration") || payload.contains("humming")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.MECHANICAL_ANOMALY)
                    .value(0.75)
                    .unit("normalized")
                    .confidence(0.7)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("operator-extractor-v1")
                    .metadata("keywords=noise,vibration,humming")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractSocialMediaEvidence(Observation obs) {
        Set<Evidence> evidence = new HashSet<>();
        String payload = obs.getPayload().toLowerCase();

        if (payload.contains("fire") || payload.contains("smoke")) {
            evidence.add(Evidence.builder()
                    .type(EvidenceType.FIRE)
                    .value(0.6)
                    .unit("normalized")
                    .confidence(0.4)
                    .sourceObservationId(obs.getId())
                    .extractedAt(Instant.now())
                    .extractorVersion("social-extractor-v1")
                    .metadata("keywords=fire,smoke;source=social")
                    .build());
        }

        return evidence;
    }

    private Set<Evidence> extractGenericEvidence(Observation obs) {
        // Fallback for unknown source types
        return Set.of();
    }
}