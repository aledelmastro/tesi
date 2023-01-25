package org.opentripplanner.ext.greenrouting;

import static org.locationtech.jts.algorithm.LineIntersector.COLLINEAR_INTERSECTION;
import static org.locationtech.jts.algorithm.LineIntersector.POINT_INTERSECTION;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.api.resource.CoordinateArrayListSequence;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.ext.greenrouting.utils.Relation;

public class GreenFeature {

    long id;
    double combinedScore;
    double length;
    Map<String, Double> scores;
    Map<String, Boolean> features;
    Geometry geometry;

    GreenFeature(long id, Map<String, Double> scores, Map<String, Boolean> features, Geometry geometry, double combinedScore) {
        this.id = id;
        this.scores = scores;
        this.geometry = geometry;
        this.combinedScore = combinedScore;
        this.features = features;
        this.length = getGeometryLengthMeters(this.geometry);
    }

    /**
     * Calculates the distance between the feature and another shape in terms of the relation
     * between their geometries.
     *
     * @param g1 the geometry of the shape from which the distance is computed.
     * @return a value indicating the minimum distance between this feature and the shape.
     */
    public double getDistance(Geometry g1) {
        Geometry g2 = this.geometry;
        if (!(g1 instanceof LineString) || !(g2 instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var segments = toLineSegments((LineString) g1);
        var feature = toLineSegments((LineString) g2).get(0);

        var relations = segments.stream()
                .map(segment -> getRelation(segment, feature))
                .collect(Collectors.toList());

        if (relations.stream().anyMatch(r -> r == Relation.OVERLAP)) {return 0;}

        if (relations.stream().anyMatch(r -> r == Relation.INTERSECT)) {return 0;}

        return segments.stream()
                .map(s -> s.midPoint().distance(feature.midPoint()))
                .min(Double::compareTo)
                .get();
    }

    public boolean intersectsBuffer(Geometry geometry, double bufferSize) {
        Geometry featureGeometry = this.geometry;
        if (!(geometry instanceof LineString) || !(featureGeometry instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var intersection = featureGeometry.buffer(bufferSize).intersection(geometry);

        var length = getGeometryLengthMeters(intersection);

        var bufferWidthMeters = getBufferWidthMeters(featureGeometry, bufferSize);
        var intersectionSizeMeters = getGeometryLengthMeters(intersection);

        return intersectionSizeMeters > bufferWidthMeters;
    }

    public double proportion(Geometry geometry, double bufferSize) {
        Geometry featureGeometry = this.geometry;
        if (!(geometry instanceof LineString) || !(featureGeometry instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var intersection = featureGeometry.buffer(bufferSize).intersection(geometry);

        var intersectionLength = getGeometryLengthMeters(intersection);
        // to get rid of the "extra" length given by the width of the buffer
        var proportion = Math.min(intersectionLength, this.length);
        var geometryLength = getGeometryLengthMeters(geometry);

        return geometryLength > 0 ? proportion / geometryLength : 0;
    }

    /**
     * Computes the kind of relation that exists among two line segments.
     *
     * @param ls1 the first line segment.
     * @param ls2 the second line segment-
     * @return a value that describes the relation.
     */
    private Relation getRelation(LineSegment ls1, LineSegment ls2) {
        RobustLineIntersector rli = new RobustLineIntersector();
        rli.computeIntersection(
                ls1.getCoordinate(0),
                ls1.getCoordinate(1),
                ls2.getCoordinate(0),
                ls2.getCoordinate(1)
        );

        if (rli.getIntersectionNum() == COLLINEAR_INTERSECTION) {
            return Relation.OVERLAP;
        }

        var dStart = ls1.distance(ls2.getCoordinate(0));
        var dEnd = ls1.distance(ls2.getCoordinate(1));

        if (rli.getIntersectionNum() == POINT_INTERSECTION) {
            if (dStart == 0 || dEnd == 0) {return Relation.SEPARATED;}
            else {return Relation.INTERSECT;}
        }

        return Relation.SEPARATED;
    }

    /**
     * Breaks a line into its segments.
     *
     * @param ls the line.
     * @return a list of the segments that make up the line.
     */
    private List<LineSegment> toLineSegments(LineString ls) {
        List<LineSegment> segments = new ArrayList<>();
        var points = ls.getCoordinates();

        for (int i = 1; i < points.length; i++) {
            segments.add(new LineSegment(points[i - 1], points[i]));
        }

        return segments;
    }

    private double getGeometryLengthMeters(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        double d = 0;
        for (int i = 1; i < coordinates.length; ++i) {
            d += SphericalDistanceLibrary.distance(coordinates[i - 1], coordinates[i]);
        }
        return d;
    }

    private double getBufferWidthMeters(Geometry geometry, double bufferSize) {
        var interiorPoint = geometry.getInteriorPoint();
        var buffer = interiorPoint.buffer(bufferSize);
        var coordinates = new ArrayList<Coordinate>();
        coordinates.add(buffer.getCoordinate());
        coordinates.add(interiorPoint.getCoordinate());
        var ls = buffer.getFactory().createLineString(new CoordinateArrayListSequence(coordinates));
        return getGeometryLengthMeters(ls);
    }
}
