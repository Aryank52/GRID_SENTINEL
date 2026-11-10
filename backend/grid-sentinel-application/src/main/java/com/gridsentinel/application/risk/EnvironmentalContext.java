package com.gridsentinel.application.risk;

/**
 * Environmental context for risk scoring.
 */
public record EnvironmentalContext(
        double temperatureCelsius,
        double windSpeedKmh,
        double humidityPercent,
        String weatherAlertLevel
) {}