package com.gridsentinel.response;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter for Set<UUID> to comma-separated string.
 * Used for storing dependencies in ResponseAction embeddable.
 */
@Converter(autoApply = false)
public class ResponseActionIdSetConverter implements AttributeConverter<Set<UUID>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<UUID> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<UUID> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptySet();
        }
        return java.util.Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }
}