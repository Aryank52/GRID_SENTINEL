package com.gridsentinel.infrastructure.api.spatial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * GeoJSON FeatureCollection object.
 * Standard container for multiple features.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoJsonFeatureCollection<T> {
    @JsonProperty("type")
    private final String type = "FeatureCollection";

    @JsonProperty("features")
    private final List<GeoJsonFeature<T>> features;

    @JsonProperty("bbox")
    private final BoundingBox bbox;

    @JsonProperty("properties")
    private final Map<String, Object> properties;

    public GeoJsonFeatureCollection(List<GeoJsonFeature<T>> features) {
        this(features, null, null);
    }

    public GeoJsonFeatureCollection(List<GeoJsonFeature<T>> features, BoundingBox bbox) {
        this(features, bbox, null);
    }

    public GeoJsonFeatureCollection(List<GeoJsonFeature<T>> features,
                                     BoundingBox bbox,
                                     Map<String, Object> properties) {
        this.features = features;
        this.bbox = bbox;
        this.properties = properties;
    }

    public String getType() { return type; }
    public List<GeoJsonFeature<T>> getFeatures() { return features; }
    public BoundingBox getBbox() { return bbox; }
    public Map<String, Object> getProperties() { return properties; }

    public int size() {
        return features != null ? features.size() : 0;
    }

    public static <T> GeoJsonFeatureCollection<T> of(List<GeoJsonFeature<T>> features) {
        return new GeoJsonFeatureCollection<>(features);
    }

    public static <T> GeoJsonFeatureCollection<T> of(List<GeoJsonFeature<T>> features, BoundingBox bbox) {
        return new GeoJsonFeatureCollection<>(features, bbox);
    }

    @SuppressWarnings("unchecked")
    public static <T> GeoJsonFeatureCollection<T> ofRaw(List<GeoJsonFeature<?>> features) {
        return new GeoJsonFeatureCollection<>((List<GeoJsonFeature<T>>) (List<?>) features);
    }

    @SuppressWarnings("unchecked")
    public static <T> GeoJsonFeatureCollection<T> ofRaw(List<GeoJsonFeature<?>> features, BoundingBox bbox) {
        return new GeoJsonFeatureCollection<>((List<GeoJsonFeature<T>>) (List<?>) features, bbox);
    }

    // =========================================================================
    // Bounding Box Helper
    // =========================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BoundingBox {
        @JsonProperty("minX")
        private final double minX; // min longitude

        @JsonProperty("minY")
        private final double minY; // min latitude

        @JsonProperty("maxX")
        private final double maxX; // max longitude

        @JsonProperty("maxY")
        private final double maxY; // max latitude

        public BoundingBox(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public double getMinX() { return minX; }
        public double getMinY() { return minY; }
        public double getMaxX() { return maxX; }
        public double getMaxY() { return maxY; }

        public static BoundingBox of(double minLng, double minLat, double maxLng, double maxLat) {
            return new BoundingBox(minLng, minLat, maxLng, maxLat);
        }
    }
}