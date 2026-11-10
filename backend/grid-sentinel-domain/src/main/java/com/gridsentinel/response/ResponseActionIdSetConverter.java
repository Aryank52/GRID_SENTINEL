package com.gridsentinel.response;

import com.gridsentinel.common.CommonKernel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA AttributeConverter for Set<ResponseActionId> to comma-separated string.
 * Used for storing dependencies in ResponseAction embeddable.
 */
@Converter(autoApply = false)
public class ResponseActionIdSetConverter implements AttributeConverter<Set<CommonKernel.ResponseActionId>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<CommonKernel.ResponseActionId> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream()
                .map(CommonKernel.ResponseActionId::value)
                .map(UUID::toString)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<CommonKernel.ResponseActionId> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new java.util.HashSet<>();
        }
        return java.util.Arrays.stream(dbData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .map(CommonKernel.ResponseActionId::of)
                .collect(Collectors.toSet());
    }
}