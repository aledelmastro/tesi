package org.opentripplanner.ext.greenrouting;

import static org.locationtech.jts.algorithm.RobustLineIntersector.COLLINEAR_INTERSECTION;
import static org.locationtech.jts.algorithm.RobustLineIntersector.POINT_INTERSECTION;
import static org.opentripplanner.util.logging.ThrottleLogger.throttle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.util.URLs;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://wiki.openstreetmap.org/wiki/Precision_of_coordinates
 */
public class GreenRouting implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(GreenRouting.class);
    /**
     * Wrap LOG with a Throttle logger errors, this will prevent thousands of log events, and just
     * log one message every 3 second.
     */
    private static final Logger GREEN_ROUTING_ERROR_LOG = throttle(LOG);
    private final GreenRoutingConfig config;
    Map<Long, List<GreenSegment>> featuresWithId;
    Map<Long, List<StreetEdge>> streetEdgesWithId;

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
        this.featuresWithId = new HashMap<>();
        this.streetEdgesWithId = new HashMap<>();
    }

    @Override
    public void buildGraph(
            Graph graph,
            HashMap<Class<?>, Object> extra,
            DataImportIssueStore issueStore
    ) {
        File dataFile = new File(config.getFileName());

        if (dataFile.exists()) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put(GeoJSONDataStoreFactory.URL_PARAM.key, URLs.fileToUrl(dataFile));
                DataStore ds = DataStoreFinder.getDataStore(params);

                // TODO ds.getTypeNames()[0] valutare se aggiungere il nome del file in config
                var featureCollection = ds.getFeatureSource(ds.getTypeNames()[0]).getFeatures();

                //streetEdges = graph.getStreetEdges();
                graph.getStreetEdges().forEach(se -> {
                    streetEdgesWithId.computeIfAbsent(se.wayId, id -> new ArrayList<>());
                    streetEdgesWithId.get(se.wayId).add(se);
                });

                try (SimpleFeatureIterator it = featureCollection.features()) {
                    while (it.hasNext()) {
                        SimpleFeature f = it.next();
                        long id = Long.parseLong((String) f.getAttribute(config.getId()));
                        double score = (Double) f.getAttribute(config.getScores());

                        Geometry geometry = (LineString) f.getDefaultGeometryProperty().getValue();
                        //var g = (Geometry) f.getDefaultGeometryProperty().getValue();
                        var segment = new GreenSegment(id, score, geometry);
                        addSegment(segment);
                    }
                }

                if (config.fastMapping()) {fastMap();}

                if (config.averageMapping()) {averageMap();}
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void checkInputs() {
        // Nothing
    }

    public Map<StreetEdge, List<Double>> mapToClosestEdge(
            Map<Long, List<GreenSegment>> featuresWithId,
            Map<Long, List<StreetEdge>> streetEdgesWithId
    ) {
        Map<StreetEdge, List<Double>> scoresForEdge = new HashMap<>();

        for (var id : featuresWithId.keySet()) {
            var edgesForID = streetEdgesWithId.get(id);
            for (var feature : featuresWithId.get(id)) {
                var minDistance = edgesForID.stream()
                        .mapToDouble(edge -> getDistance(edge.getGeometry(), feature.geometry))
                        .min()
                        .getAsDouble();

                var closestEdges = edgesForID.stream()
                        .filter(streetEdge ->
                                getDistance(streetEdge.getGeometry(), feature.geometry)
                                        == minDistance)
                        .collect(Collectors.toList());

                for (StreetEdge edge: closestEdges) {
                    if (edge instanceof GreenStreetEdge) {
                        var gse = (GreenStreetEdge) edge;
                        scoresForEdge.computeIfAbsent(gse, key -> new ArrayList<>());
                        scoresForEdge.get(gse).add(feature.score);
                    }
                }
            }
        }

        return scoresForEdge;
    }

    // TODO move to utils
    public Relation getRelation(LineSegment ls1, LineSegment ls2) {
        RobustLineIntersector rli = new RobustLineIntersector();
        rli.computeIntersection(
                ls1.getCoordinate(0),
                ls1.getCoordinate(1),
                ls2.getCoordinate(0),
                ls2.getCoordinate(1)
        );

        if (rli.getIntersectionNum() == COLLINEAR_INTERSECTION) {return Relation.OVERLAP;}

        var dStart = ls1.distance(ls2.getCoordinate(0));
        var dEnd = ls1.distance(ls2.getCoordinate(1));

        if (rli.getIntersectionNum() == POINT_INTERSECTION) {
            if (dStart == 0 || dEnd == 0) {return Relation.SEPARATED;}
            else {return Relation.INTERSECT;}
        }

        return Relation.SEPARATED;
    }

    public double getDistance(Geometry g1, Geometry g2) {
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

    public List<StreetEdge> getStreetEdgesForID(long id) {
        return streetEdgesWithId.get(id);
    }

    private void addSegment(GreenSegment segment) {
        if (!this.featuresWithId.containsKey(segment.id)) {
            this.featuresWithId.put(segment.id, new ArrayList<>());
        }

        this.featuresWithId.get(segment.id).add(segment);
    }

    private void fastMap() {
        for (var id : featuresWithId.keySet()) {
            var edgesForID = getStreetEdgesForID(id);
            var score = featuresWithId.get(id).get(0).score;
            for (StreetEdge edge : edgesForID) {
                if (edge instanceof GreenStreetEdge) {((GreenStreetEdge) edge).greenyness = score;}
            }
        }
    }

    private void averageMap() {
        Map<StreetEdge, List<Double>> scoresForEdge =
                mapToClosestEdge(this.featuresWithId, this.streetEdgesWithId);

        for (StreetEdge edge : scoresForEdge.keySet()) {
            ((GreenStreetEdge) edge).greenyness = scoresForEdge.get(edge).stream()
                    .mapToDouble(score -> score)
                    .average()
                    .getAsDouble();
        }
    }

    // TODO move to utils
    private List<LineSegment> toLineSegments(LineString ls) {
        List<LineSegment> segments = new ArrayList<>();
        var points = ls.getCoordinates();

        for (int i = 1; i < points.length; i++) {
            segments.add(new LineSegment(points[i - 1], points[i]));
        }

        return segments;
    }

    // TODO move to utils
    private enum Relation {
        OVERLAP,
        INTERSECT,
        SEPARATED
    }
}
