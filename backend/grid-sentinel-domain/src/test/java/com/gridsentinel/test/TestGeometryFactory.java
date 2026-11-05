package com.gridsentinel.test;

import com.gridsentinel.common.CommonKernel;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Coordinate;

/**
 * Test utility for creating JTS geometries.
 */
public final class TestGeometryFactory {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);

    private TestGeometryFactory() {}

    public static CommonKernel.GeometryValueObject point(double longitude, double latitude) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
        return CommonKernel.GeometryValueObject.of(point);
    }

    public static CommonKernel.GeometryValueObject polygon(double[][] coordinates) {
        Coordinate[] coords = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            coords[i] = new Coordinate(coordinates[i][0], coordinates[i][1]);
        }
        Polygon polygon = GEOMETRY_FACTORY.createPolygon(coords);
        return CommonKernel.GeometryValueObject.of(polygon);
    }

    public static CommonKernel.GeometryValueObject point(double longitude, double latitude, double z) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude, z));
        return CommonKernel.GeometryValueObject.of(point);
    }

    public static CommonKernel.GeometryValueObject lineString(double[][] coordinates) {
        Coordinate[] coords = new Coordinate[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            coords[i] = new Coordinate(coordinates[i][0], coordinates[i][1]);
        }
        LineString lineString = GEOMETRY_FACTORY.createLineString(coords);
        return CommonKernel.GeometryValueObject.of(lineString);
    }
}