package com.gridsentinel.infrastructure.api.spatial;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Base interface for GeoJSON geometry types.
 * Supports Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class GeoJsonGeometry {
    @NotNull
    @JsonProperty("type")
    private final String type;

    @NotNull
    @JsonProperty("coordinates")
    private final Object coordinates;

    protected GeoJsonGeometry(String type, Object coordinates) {
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getType() { return type; }
    public Object getCoordinates() { return coordinates; }

    // =========================================================================
    // Concrete Geometry Types
    // =========================================================================

    public static class Point extends GeoJsonGeometry {
        public Point(double longitude, double latitude) {
            super("Point", List.of(longitude, latitude));
        }

        public Point(@JsonProperty("coordinates") List<Double> coordinates) {
            super("Point", coordinates);
        }
    }

    public static class LineString extends GeoJsonGeometry {
        public LineString(List<List<Double>> coordinates) {
            super("LineString", coordinates);
        }
    }

    public static class Polygon extends GeoJsonGeometry {
        public Polygon(List<List<List<Double>>> coordinates) {
            super("Polygon", coordinates);
        }
    }

    public static class MultiPoint extends GeoJsonGeometry {
        public MultiPoint(List<List<Double>> coordinates) {
            super("MultiPoint", coordinates);
        }
    }

    public static class MultiLineString extends GeoJsonGeometry {
        public MultiLineString(List<List<List<Double>>> coordinates) {
            super("MultiLineString", coordinates);
        }
    }

    public static class MultiPolygon extends GeoJsonGeometry {
        public MultiPolygon(List<List<List<List<Double>>>> coordinates) {
            super("MultiPolygon", coordinates);
        }
    }

    // =========================================================================
    // Factory Methods
    // =========================================================================

    public static Point point(double longitude, double latitude) {
        return new Point(longitude, latitude);
    }

    public static LineString lineString(List<List<Double>> coordinates) {
        return new LineString(coordinates);
    }

    public static Polygon polygon(List<List<List<Double>>> coordinates) {
        return new Polygon(coordinates);
    }
}