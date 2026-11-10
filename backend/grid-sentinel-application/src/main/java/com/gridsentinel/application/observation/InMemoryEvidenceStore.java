package com.gridsentinel.application.observation;

import com.gridsentinel.common.CommonKernel;
import com.gridsentinel.observation.Evidence;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory evidence store for development/testing.
 */
public class InMemoryEvidenceStore implements IngestObservationUseCase.EvidenceStore {

    private final Map<IngestObservationUseCase.LocationKey, List<Evidence>> store = new ConcurrentHashMap<>();
    private final Map<CommonKernel.ObservationId, Set<Evidence>> byObservation = new ConcurrentHashMap<>();

    @Override
    public void store(CommonKernel.ObservationId observationId, Set<Evidence> evidence) {
        IngestObservationUseCase.LocationKey key = new IngestObservationUseCase.LocationKey(observationId);
        store.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>())).addAll(evidence);
        byObservation.put(observationId, new HashSet<>(evidence));
    }

    @Override
    public List<Evidence> getRecentEvidence(Instant since) {
        return byObservation.values().stream()
                .flatMap(Set::stream)
                .filter(e -> e.getExtractedAt().isAfter(since))
                .collect(Collectors.toList());
    }

    @Override
    public List<Evidence> getEvidenceForLocation(IngestObservationUseCase.LocationKey key) {
        return store.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public void removeExpired(Instant before) {
        byObservation.values().forEach(set -> set.removeIf(e -> e.getExtractedAt().isBefore(before)));
        store.values().forEach(list -> list.removeIf(e -> e.getExtractedAt().isBefore(before)));
    }
}