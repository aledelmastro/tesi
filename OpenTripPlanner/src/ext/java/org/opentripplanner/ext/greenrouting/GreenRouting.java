package org.opentripplanner.ext.greenrouting;

import static org.opentripplanner.util.ProgressTracker.track;
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
import org.opentripplanner.util.ProgressTracker;
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
    private final int nodeMapped;
    Map<Long, List<GreenFeature>> featuresWithId;
    Map<Long, List<GreenStreetEdge>> edgesWithId;
    private ProgressTracker progressTracker;

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
        this.featuresWithId = new HashMap<>();
        this.edgesWithId = new HashMap<>();
        this.nodeMapped = 0;
    }

    @Override
    public void buildGraph(
            Graph graph, HashMap<Class<?>, Object> extra, DataImportIssueStore issueStore
    ) {
        File dataFile = new File(config.getFileName());

        if (!dataFile.exists()) {
            LOG.error(config.getFileName() + " does not exist.");
            return;
        }

        var nStreetEdges = graph.getEdgesOfType(StreetEdge.class).size();
        var nGreenEdges = graph.getEdgesOfType(GreenStreetEdge.class).size();
        int coverage = nStreetEdges != 0 ? (int) (nGreenEdges / (float) nStreetEdges * 100) : 0;

        LOG.info("Green edges are " + nGreenEdges + " (" + coverage + "% of the total).");

        var featureCollection = getFeatureCollection(dataFile);
        if (featureCollection.isPresent()) {
            this.edgesWithId = getStreetEdges(graph);

            LOG.info("Collecting features...");
            var nFeatures = 0;
            try (SimpleFeatureIterator it = featureCollection.get().features()) {
                while (it.hasNext()) {
                    addFeature(parseFeature(it.next()));
                    nFeatures++;
                }
            }
            LOG.info("Total features: " + nFeatures);

            if (config.fastMapping()) {fastMap();}

            if (config.weightedAverageMapping()) {weightedAverageMap();}

            // TODO  aggiungere info sui nodi mappati

        }
    }

    @Override
    public void checkInputs() {
        // Nothing
    }

    /**
     * Maps each feature to its closest street edge.
     *
     * @param featuresWithId    a map in which the key is the wayId and the value is a list of the
     *                          features with that id.
     * @param streetEdgesWithId a map in which the key is the wayId and the value is a list of the
     *                          street edges with that id.
     * @return a map in which the key is a street edge and the value is a list of features whose
     * nearest edge is the key.
     */
    public Map<GreenStreetEdge, List<GreenFeature>> mapFeaturesToNearestEdge(
            Map<Long, List<GreenFeature>> featuresWithId,
            Map<Long, List<GreenStreetEdge>> streetEdgesWithId
    ) {
        Map<GreenStreetEdge, List<GreenFeature>> featuresForEdge = new HashMap<>();

        var sharedIds = featuresWithId.keySet()
                .stream()
                .filter(k -> streetEdgesWithId.containsKey(k))
                .collect(Collectors.toList());

        progressTracker = track("Map features to street edges", 5000, sharedIds.size());

        for (var id : sharedIds) {
            var edgesWithId = streetEdgesWithId.get(id);
            for (var feature : featuresWithId.get(id)) {
                var minDistance = edgesWithId.stream()
                        .mapToDouble(edge -> feature.getDistance(edge.getGeometry()))
                        .min()
                        .orElse(-1);

                var nearestEdges = edgesWithId.stream()
                        .filter(streetEdge -> feature.getDistance(streetEdge.getGeometry())
                                == minDistance)
                        .collect(Collectors.toList());

                for (GreenStreetEdge edge : nearestEdges) {
                    featuresForEdge.computeIfAbsent(edge, key -> new ArrayList<>());
                    featuresForEdge.get(edge).add(feature);
                }

            }
            progressTracker.step(m -> LOG.info(m));
        }

        LOG.info(progressTracker.completeMessage());
        /*LOG.info("Couldn't find a valid edge for " + (
                featuresWithId.keySet().size() - sharedIds.size()
        ) + " features.");
        LOG.info("Couldn't find a feature for " + (
                featuresWithId.keySet().size() - sharedIds.size()
        ) + " features.");*/

        return featuresForEdge;
    }

    /**
     * Returns all the instances of {@link GreenStreetEdge} with the specified id.
     *
     * @param id the id for the edges.
     * @return a list of all the edges with the specified id.
     */
    private List<GreenStreetEdge> greenStreetEdgesForID(long id) {
        return Objects.requireNonNullElse(edgesWithId.get(id), Collections.emptyList());
    }

    /**
     * Adds a feature to the internal map that stores the features grouped by their id.
     *
     * @param feature the feature to be added.
     */
    private void addFeature(GreenFeature feature) {
        if (!this.featuresWithId.containsKey(feature.id)) {
            this.featuresWithId.put(feature.id, new ArrayList<>());
        }

        this.featuresWithId.get(feature.id).add(feature);
    }

    /**
     * For each id, sets the score of the first corresponding feature to the first corresponding
     * edge. This approach can be used when data provide a single feature (thus a single score) for
     * each id.
     * <p>
     * It's the fastest kind of mapping because it simply iterates over the ids one time.
     */
    private void fastMap() {
        progressTracker =
                track("Map features to street edges", 5000, featuresWithId.keySet().size());

        for (var id : featuresWithId.keySet()) {
            var score = featuresWithId.get(id).get(0).score;
            var edgesForID = greenStreetEdgesForID(id);

            for (GreenStreetEdge edge : edgesForID) {edge.setGreenyness(score);}

            progressTracker.step(m -> LOG.info(m));
        }

        LOG.info(progressTracker.completeMessage());
    }

    /**
     * Groups the features by their nearest edge and performs a weighted average of their scores
     * based on their length. The calculated value is then set as a "green score" for the edge.
     * <p>
     * This option allows for multiple features (thus multiple scores) for a single id.
     */
    private void weightedAverageMap() {
        Map<GreenStreetEdge, List<GreenFeature>> closestFeatures =
                mapFeaturesToNearestEdge(this.featuresWithId, this.edgesWithId);

        for (GreenStreetEdge edge : closestFeatures.keySet()) {
            var totalLength = closestFeatures.get(edge)
                    .stream()
                    .mapToDouble(segment -> segment.geometry.getLength())
                    .sum();

            edge.setGreenyness(
                    closestFeatures.get(edge)
                            .stream()
                            .mapToDouble(feature -> feature.score * feature.geometry.getLength())
                            .sum() / totalLength
            );

            // TODO divisione per 0
            config.getVariables().forEach(variable -> {
                edge.putVariable(variable, closestFeatures.get(edge)
                        .stream()
                        .mapToDouble(feature -> feature.variables.get(variable) * feature.geometry.getLength())
                        .sum() / totalLength);
            });

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
        var id = ((Integer) feature.getAttribute(config.getId())).longValue();
        var expression = config.getExpression();

        var variables = new HashMap<String, Double>();
        config.getVariables().forEach(variable -> {
            var value = ((Number) feature.getAttribute(variable)).doubleValue();
            variables.put(variable, value);
            expression.setVariable(variable, value);
        });
        var score = expression.evaluate();

        Geometry geometry = (LineString) feature.getDefaultGeometryProperty().getValue();
        return new GreenFeature(id, variables, geometry, score);
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

        graph.getEdgesOfType(GreenStreetEdge.class).forEach(edge -> {
            edgesWithId.computeIfAbsent(edge.wayId, id -> new ArrayList<>());
            edgesWithId.get(edge.wayId).add(edge);
        });

        return edgesWithId;
    }

    /**
     * Loads a feature collection from a designated geojson file.
     *
     * @param dataFile the file containing the features.
     * @return an empty optional if a suitable loader could not be found or an access error
     * occurred. Otherwise, the optional contains a feature collection.
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
