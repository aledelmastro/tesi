package org.opentripplanner.ext.greenrouting;

import static org.opentripplanner.util.logging.ThrottleLogger.throttle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.ext.greenrouting.edgetype.GreenStreetEdge;
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
    Map<Long, List<GreenFeature>> featuresWithId;
    Map<Long, List<GreenStreetEdge>> edgesWithId;

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
        this.featuresWithId = new HashMap<>();
        this.edgesWithId = new HashMap<>();
    }

    @Override
    public void buildGraph(
            Graph graph,
            HashMap<Class<?>, Object> extra,
            DataImportIssueStore issueStore
    ) {
        File dataFile = new File(config.getFileName());

        if (!dataFile.exists()) {return;}

        var featureCollection = getFeatureCollection(dataFile);
        if(featureCollection.isPresent()) {
            this.edgesWithId = getStreetEdges(graph);

            try (SimpleFeatureIterator it = featureCollection.get().features()) {
                while (it.hasNext()) {addFeature(parseFeature(it.next()));}
            }

            if (config.fastMapping()) {fastMap();}

            if (config.weightedAverageMapping()) {weightedAverageMap();}
        }
    }

    @Override
    public void checkInputs() {
        // Nothing
    }

    public Map<StreetEdge, List<GreenFeature>> mapFeaturesToClosestEdge(
            Map<Long, List<GreenFeature>> featuresWithId,
            Map<Long, List<GreenStreetEdge>> streetEdgesWithId
    ) {
        Map<StreetEdge, List<GreenFeature>> scoresForEdge = new HashMap<>();

        for (var id : featuresWithId.keySet()) {
            var edgesWithId = streetEdgesWithId.get(id);
            for (var feature : featuresWithId.get(id)) {
                var minDistance = edgesWithId.stream()
                        .mapToDouble(edge -> feature.getDistance(edge.getGeometry()))
                        .min()
                        .orElse(-1);

                var closestEdges = edgesWithId.stream()
                        .filter(streetEdge -> feature.getDistance(streetEdge.getGeometry())
                                == minDistance)
                        .collect(Collectors.toList());

                for (GreenStreetEdge edge : closestEdges) {
                    scoresForEdge.computeIfAbsent(edge, key -> new ArrayList<>());
                    scoresForEdge.get(edge).add(feature);
                }
            }
        }

        return scoresForEdge;
    }

    public List<GreenStreetEdge> greenStreetEdgesForID(long id) {
        return Objects.requireNonNullElse(edgesWithId.get(id), Collections.emptyList());
    }

    private void addFeature(GreenFeature feature) {
        if (!this.featuresWithId.containsKey(feature.id)) {
            this.featuresWithId.put(feature.id, new ArrayList<>());
        }

        this.featuresWithId.get(feature.id).add(feature);
    }

    private void fastMap() {
        for (var id : featuresWithId.keySet()) {
            var score = featuresWithId.get(id).get(0).score;
            var edgesForID = greenStreetEdgesForID(id);

            for (GreenStreetEdge edge : edgesForID) {edge.greenyness = score;}
        }
    }

    private void weightedAverageMap() {
        Map<StreetEdge, List<GreenFeature>> closestFeatures =
                mapFeaturesToClosestEdge(this.featuresWithId, this.edgesWithId);

        for (StreetEdge edge : closestFeatures.keySet()) {
            var totalLength = closestFeatures.get(edge).stream()
                    .mapToDouble(segment -> segment.geometry.getLength())
                    .sum();

            closestFeatures.get(edge).stream()
                    .mapToDouble(segment -> segment.score * segment.geometry.getLength())
                    .average()
                    .ifPresent(v -> ((GreenStreetEdge) edge).greenyness = v / totalLength);
        }
    }

    /**
     * Extracts the values from a SimpleFeature into a GreenFeature performing all the required type
     * casts.
     *
     * @param feature a generic feature as described by {@link SimpleFeature}.
     * @return an instance of GreenFeature populated with the values extracted from the argument.
     */
    private GreenFeature parseFeature(SimpleFeature feature) {
        long id = Long.parseLong((String) feature.getAttribute(config.getId()));
        double score = (Double) feature.getAttribute(config.getScores());

        Geometry geometry = (LineString) feature.getDefaultGeometryProperty().getValue();
        return new GreenFeature(id, score, geometry);
    }

    /**
     * Finds all the instances of GreenStreetEdge in the graph and groups them based on their
     * wayId.
     *
     * @param graph the graph built from OSM data.
     * @return a key-value map where the key is the wayId and the value is a list of all the
     * corresponding GreenStreetEdge.
     * @see GreenStreetEdge
     */
    private Map<Long, List<GreenStreetEdge>> getStreetEdges(Graph graph) {
        Map<Long, List<GreenStreetEdge>> edgesWithId = new HashMap<>();

        graph.getStreetEdges().forEach(se -> {
            if (se instanceof GreenStreetEdge) {
                edgesWithId.computeIfAbsent(se.wayId, id -> new ArrayList<>());
                edgesWithId.get(se.wayId).add((GreenStreetEdge) se);
            }
        });

        return edgesWithId;
    }

    /**
     * Loads a feature collection from a designated geojson file.
     * @param dataFile the file containing the features.
     * @return an empty optional if a suitable loader could not be found or
     * an access error occurred. Otherwise, the optional contains a feature
     * collection.
     */
    private Optional<SimpleFeatureCollection> getFeatureCollection(File dataFile) {
        Map<String, Object> params = new HashMap<>();
        params.put(GeoJSONDataStoreFactory.URL_PARAM.key, URLs.fileToUrl(dataFile));
        DataStore ds = null;
        Optional<SimpleFeatureCollection> featureCollection = Optional.empty();
        try {
            ds = DataStoreFinder.getDataStore(params);
            featureCollection =
                    Optional.ofNullable(ds.getFeatureSource(ds.getTypeNames()[0]).getFeatures());
        }
        catch (IOException e) {
            // TODO eccezione file!!!
            e.printStackTrace();
        }

        return featureCollection;
    }
}
