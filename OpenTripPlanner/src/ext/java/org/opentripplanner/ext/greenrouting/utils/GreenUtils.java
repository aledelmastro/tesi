package org.opentripplanner.ext.greenrouting.utils;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.api.resource.CoordinateArrayListSequence;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;

public class GreenUtils {

    public static double getGeometryLengthMeters(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        double d = 0;
        for (int i = 1; i < coordinates.length; ++i) {
            d += SphericalDistanceLibrary.distance(coordinates[i - 1], coordinates[i]);
        }
        return d;
    }

    public static double getBufferWidthMeters(Geometry geometry, double bufferSize) {
        var interiorPoint = geometry.getInteriorPoint();
        var buffer = interiorPoint.buffer(bufferSize);
        var coordinates = new ArrayList<Coordinate>();
        coordinates.add(buffer.getCoordinate());
        coordinates.add(interiorPoint.getCoordinate());
        var ls = buffer.getFactory().createLineString(new CoordinateArrayListSequence(coordinates));
        return getGeometryLengthMeters(ls);
    }

    /**
     * Breaks a line into its segments.
     *
     * @param ls the line.
     * @return a list of the segments that make up the line.
     */
    public static List<LineSegment> toLineSegments(LineString ls) {
        List<LineSegment> segments = new ArrayList<>();
        var points = ls.getCoordinates();

        for (int i = 1; i < points.length; i++) {
            segments.add(new LineSegment(points[i - 1], points[i]));
        }

        return segments;
    }
}
