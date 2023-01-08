package org.opentripplanner.ext.greenrouting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.Mockito;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig.GreenMappingMode;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.SimpleVertex;

public class GreenRoutingTest {

    @Test
    public void getDistanceTest_OneOneTheOther() {
        var ls = getLineStringsOneOnTheOther();
        var feature = greenFeature(ls[0]);

        assertEquals(0, feature.getDistance(ls[1]), 0);
    }

    @Test
    public void getDistanceTest_Overlap() {
        var ls = getOverlappedLineStrings();
        var feature = greenFeature(ls[0]);

        assertEquals(0, feature.getDistance(ls[1]), 0);
    }

    @Test
    public void getDistanceTest_InternalIntersection() {
        var ls = getIncidentLineStrings();
        var feature = greenFeature(ls[0]);

        assertEquals(0, feature.getDistance(ls[1]), 0);
    }

    @Test
    public void getDistanceTest_ExtremityIntersection() {
        var geometryFactory = new GeometryFactory();

        LineString ls1 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(5, 0)
                }
        );

        LineString ls2 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(1, -1)
                }
        );

        var feature = greenFeature(ls1);
        assertTrue(feature.getDistance(ls2) > 0);
    }

    @Test
    public void getDistanceTest_ArgsDifferentThanLineString() {
        var geometryFactory = new GeometryFactory();

        Point p = geometryFactory.createPoint();
        var feature = greenFeature(geometryFactory.createLineString());

        assertThrows(IllegalArgumentException.class, () -> feature.getDistance(p));
    }

    @Test
    public void mapToClosestEdgeTest() {
        var gr = getDefaultGreenRouting();
        var score = 10;
        var score2 = 5;
        var id = 1L;

        var ls1 = lineString(0, 0, 5, 0);
        var incidentWithLs1 = lineString(1, 1, 1, -1);
        var doesntTouchLs1 = lineString(1, 1, 5, 2);

        GreenStreetEdge e1 = greenStreetEdge(ls1, id);
        GreenFeature gs = new GreenFeature(id, score, incidentWithLs1);
        GreenFeature gs2 = new GreenFeature(id, score, doesntTouchLs1);

        var ls2 = lineString(5, 0, 8, 0);
        var closerToS2 = lineString(4, -1, 8, -1);

        GreenStreetEdge e2 = greenStreetEdge(ls2, id);
        GreenFeature gs3 = new GreenFeature(id, score2, closerToS2);

        var featuresWithId = new HashMap<Long, List<GreenFeature>>();
        featuresWithId.put(id, List.of(gs, gs2, gs3));

        var streetEdgesWithId = new HashMap<Long, List<GreenStreetEdge>>();
        streetEdgesWithId.put(id, List.of(e1, e2));

        var mapping = gr.mapFeaturesToNearestEdge(
                featuresWithId,
                streetEdgesWithId
        );

        assertEquals(2, mapping.get(e1).size());
        assertEquals(1, mapping.get(e2).size());
    }

    @Test
    public void averageMapTest() {
        var gr = new GreenRouting(
                new GreenRoutingConfig("test/green-test/green.json", "id", "score",
                        GreenMappingMode.AVERAGE
                ));
        var id = 1L;

        Graph g = new Graph();

        GreenStreetEdge s = new GreenStreetEdge(
                new SimpleVertex(g, "v1", 0, 0),
                new SimpleVertex(g, "v2", 5, 0),
                lineString(0, 0, 5, 0),
                null,
                null,
                true
        );

        s.wayId = id;
        gr.buildGraph(g, null, null);

        assertEquals(s.greenyness, 8, 0);
    }

    private GreenRouting getDefaultGreenRouting() {
        GreenRoutingConfig grc = Mockito.mock(GreenRoutingConfig.class);
        return new GreenRouting(grc);
    }

    private LineString[] getIncidentLineStrings() {
        var geometryFactory = new GeometryFactory();

        LineString ls1 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(5, 0)
                }
        );

        LineString ls2 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(1, 1),
                        new Coordinate(1, -1)
                }
        );

        return new LineString[]{ls1, ls2};
    }

    private LineString[] getLineStringsOneOnTheOther() {
        var geometryFactory = new GeometryFactory();

        LineString ls1 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(5, 0)
                }
        );

        LineString ls2 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(1, 0),
                        new Coordinate(4, 0)
                }
        );

        return new LineString[]{ls1, ls2};
    }

    private LineString[] getOverlappedLineStrings() {
        var geometryFactory = new GeometryFactory();

        LineString ls1 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(5, 0)
                }
        );

        LineString ls2 = geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(1, 0),
                        new Coordinate(4, 0)
                }
        );

        return new LineString[]{ls1, ls2};
    }

    private LineString lineString(double x1, double y1, double x2, double y2) {
        var gf = new GeometryFactory();

        return gf.createLineString(new Coordinate[]{
                new Coordinate(x1, y1),
                new Coordinate(x2, y2)
        });
    }

    private GreenFeature greenFeature(Geometry g) {
        return new GreenFeature(0, 0, g);
    }

    private GreenStreetEdge greenStreetEdge(LineString geometry, long id) {
        var v1 = new IntersectionVertex(null, null, geometry.getCoordinates()[0].getX(),
                geometry.getCoordinates()[0].getY()
        );
        var v2 = new IntersectionVertex(null, null, geometry.getCoordinates()[1].getX(),
                geometry.getCoordinates()[1].getY()
        );
        var edge = new GreenStreetEdge(v1, v2, geometry, null, null, false);
        edge.wayId = id;
        return edge;
    }
}
