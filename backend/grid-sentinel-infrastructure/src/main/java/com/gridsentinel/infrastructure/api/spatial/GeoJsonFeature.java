package com.gridsentinel.infrastructure.api.spatial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

/**
 * GeoJSON Feature object.
 * Contains a geometry, properties, and optional ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoJsonFeature<T> {
    @JsonProperty("type")
    private final String type = "Feature";

    @JsonProperty("id")
    private final String id;

    @JsonProperty("geometry")
    private final GeoJsonGeometry geometry;

    @JsonProperty("properties")
    private final T properties;

    public GeoJsonFeature(GeoJsonGeometry geometry, T properties) {
        this(geometry, properties, null);
    }

    public GeoJsonFeature(GeoJsonGeometry geometry, T properties, UUID id) {
        this.geometry = geometry;
        this.properties = properties;
        this.id = id != null ? id.toString() : null;
    }

    public String getType() { return type; }
    public String getId() { return id; }
    public GeoJsonGeometry getGeometry() { return geometry; }
    public T getProperties() { return properties; }

    public static <T> GeoJsonFeature<T> of(GeoJsonGeometry geometry, T properties) {
        return new GeoJsonFeature<>(geometry, properties);
    }

    public static <T> GeoJsonFeature<T> of(GeoJsonGeometry geometry, T properties, UUID id) {
        return new GeoJsonFeature<>(geometry, properties, id);
    }
}