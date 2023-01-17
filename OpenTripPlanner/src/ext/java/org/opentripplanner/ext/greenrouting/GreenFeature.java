package org.opentripplanner.ext.greenrouting;

import static org.locationtech.jts.algorithm.LineIntersector.COLLINEAR_INTERSECTION;
import static org.locationtech.jts.algorithm.LineIntersector.POINT_INTERSECTION;

import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.geotools.xml.xsi.XSISimpleTypes.Int;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.ext.greenrouting.utils.Relation;

public class GreenFeature {

    long id;
    double score;
    Map<String, Double> variables;
    Geometry geometry;

    GreenFeature(long id, Map<String, Double> variables, Geometry geometry, double score) {
        this.id = id;
        this.variables = variables;
        this.geometry = geometry;
        this.score = score;
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

        return intersection.getLength() > bufferSize;
    }

    public double proportion(Geometry geometry, double bufferSize) {
        Geometry featureGeometry = this.geometry;
        if (!(geometry instanceof LineString) || !(featureGeometry instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var intersection = featureGeometry.buffer(bufferSize).intersection(geometry);

        // to get rid of the "extra" length given by the width of the buffer
        var prop = intersection.getLength();
        if (intersection.getLength() > featureGeometry.getLength())
            prop = featureGeometry.getLength();

        return geometry.getLength() > 0 ? prop / geometry.getLength() : 0;
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
}
