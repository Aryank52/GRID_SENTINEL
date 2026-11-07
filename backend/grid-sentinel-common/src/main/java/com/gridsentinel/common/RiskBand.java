package com.gridsentinel.common;

/**
 * Risk score bands.
 */
public enum RiskBand {
    NEGLIGIBLE(0.0, 0.1),
    LOW(0.1, 0.3),
    MEDIUM(0.3, 0.6),
    HIGH(0.6, 0.8),
    CRITICAL(0.8, 1.0);

    private final double minInclusive;
    private final double maxExclusive;

    RiskBand(double minInclusive, double maxExclusive) {
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
    }

    public double minInclusive() {
        return minInclusive;
    }

    public double maxExclusive() {
        return maxExclusive;
    }

    public static RiskBand fromScore(double score) {
        for (RiskBand band : values()) {
            if (score >= band.minInclusive && score < band.maxExclusive) {
                return band;
            }
        }
        return CRITICAL; // score >= 1.0 or edge case
    }
}