package com.gridsentinel.application.risk;

import java.util.List;

/**
 * Historical context for risk scoring.
 */
public record HistoricalContext(
        int incidentCountLastYear,
        double avgTimeToResolveHours,
        List<String> previousFailureModes
) {}