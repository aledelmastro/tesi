package org.opentripplanner.ext.greenrouting;

import static org.opentripplanner.ext.greenrouting.utils.GreenUtils.getBufferWidthMeters;
import static org.opentripplanner.ext.greenrouting.utils.GreenUtils.getGeometryLengthMeters;

import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

public class GreenFeature {

    long id;
    double combinedScore;
    double length;
    Map<String, Double> scores;
    Map<String, Boolean> features;
    Geometry geometry;

    GreenFeature(
            long id,
            Map<String, Double> scores,
            Map<String, Boolean> features,
            Geometry geometry,
            double combinedScore
    ) {
        this.id = id;
        this.scores = scores;
        this.geometry = geometry;
        this.combinedScore = combinedScore;
        this.features = features;
        this.length = getGeometryLengthMeters(this.geometry);
    }

    public boolean intersectsBuffer(Geometry geometry, double bufferSize) {
        Geometry featureGeometry = this.geometry;
        if (!(geometry instanceof LineString) || !(featureGeometry instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var intersection = featureGeometry.buffer(bufferSize).intersection(geometry);

        var bufferWidthMeters = getBufferWidthMeters(featureGeometry, bufferSize);
        var intersectionSizeMeters = getGeometryLengthMeters(intersection);

        return intersectionSizeMeters > bufferWidthMeters;
    }

    public double intersectionSize (Geometry geometry, double bufferSize) {
        Geometry featureGeometry = this.geometry;
        if (!(geometry instanceof LineString) || !(featureGeometry instanceof LineString)) {
            throw new IllegalArgumentException("Geometry is not a line string.");
        }

        var intersection = featureGeometry.buffer(bufferSize).intersection(geometry);

        return getGeometryLengthMeters(intersection);
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
}
