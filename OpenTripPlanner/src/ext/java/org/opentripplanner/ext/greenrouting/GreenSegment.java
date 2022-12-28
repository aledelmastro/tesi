package org.opentripplanner.ext.greenrouting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.routing.graph.Edge;

public class GreenSegment {

    long id;
    double score;
    Geometry geometry;
    Map<Edge, Double> overlaps;

    GreenSegment(long id, double score, Geometry geometry) {
        this.id = id;
        this.score = score;
        this.geometry = geometry;
        this.overlaps = new HashMap<>();
    }

    void overlapWith(Edge edge) {
        overlaps.put(
                edge,
                this.geometry.intersection(edge.getGeometry()).getLength()
        );
    }

    Collection<Edge> getOverlappedEdges() {
        return this.overlaps.keySet();
    }
}
