package org.opentripplanner.ext.greenrouting;

import static org.opentripplanner.util.ProgressTracker.track;
import static org.opentripplanner.util.logging.ThrottleLogger.throttle;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
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
public class GreenRouting<T extends StreetEdge & GreenFactor> implements GraphBuilderModule {

    private static final Logger LOG = LoggerFactory.getLogger(GreenRouting.class);
    /**
     * Wrap LOG with a Throttle logger errors, this will prevent thousands of log events, and just
     * log one message every 3 second.
     */
    private static final Logger GREEN_ROUTING_ERROR_LOG = throttle(LOG);
    private final GreenRoutingConfig config;
    Map<Long, List<GreenFeature>> featuresWithId;
    Map<Long, List<T>> edgesWithId;
    private ProgressTracker progressTracker;

    private int edgesMapped = 0;
    private int featuresMapped = 0;
    private int edgesWithFeatures = 0;
    private int edgesWithoutFeatures = 0;
    private int featuresWithoutEdges = 0;
    private int nFeatures = 0;
    private long nGreenEdges = 0;
    private int nStreetEdges = 0;
    private double coverage = 0;
    private Map<Integer, List<T>> nFeaturesForEdge = new HashMap<>();

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
        this.featuresWithId = new HashMap<>();
        this.edgesWithId = new HashMap<>();
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

        this.nStreetEdges = graph.getEdgesOfType(StreetEdge.class).size();
        this.nGreenEdges = graph.getEdges().stream().filter(GreenFactor.class::isInstance).count();
        this.coverage = nStreetEdges != 0 ? (int) (nGreenEdges / (float) nStreetEdges * 100) : -1;

        LOG.info("Green edges are " + nGreenEdges + " (" + coverage + "% of the total).");

        var featureCollection = getFeatureCollection(dataFile);
        if (featureCollection.isPresent()) {
            this.edgesWithId = getStreetEdges(graph);

            LOG.info("Collecting features...");

            try (SimpleFeatureIterator it = featureCollection.get().features()) {
                while (it.hasNext()) {
                    addFeature(parseFeature(it.next()));
                    this.nFeatures++;
                }
            }
            LOG.info("Total features: " + this.nFeatures);

            this.featuresWithoutEdges = this.nFeatures;
            this.featuresMapped = 0;

            weightedAverageMap();

            this.writeFiles(graph);
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
    public Map<T, List<GreenFeature>> mapFeaturesToNearestEdge(
            Map<Long, List<GreenFeature>> featuresWithId,
            Map<Long, List<T>> streetEdgesWithId
    ) {
        Map<T, List<GreenFeature>> featuresForEdge = new HashMap<>();

        var sharedIds = featuresWithId.keySet()
                .stream()
                .filter(k -> streetEdgesWithId.containsKey(k))
                .peek(k -> {
                    this.edgesWithFeatures += streetEdgesWithId.getOrDefault(k, List.of()).size();
                    this.featuresWithoutEdges -= this.featuresWithId.getOrDefault(k, List.of()).size();
                })
                .collect(Collectors.toList());

        progressTracker = track("Map features to street edges", 5000, sharedIds.size());

        for (var id : sharedIds) {
            var edgesWithId = streetEdgesWithId.get(id);
            for (var feature : featuresWithId.get(id)) {
                var nearestEdges = edgesWithId.stream()
                        .filter(streetEdge -> feature.intersectsBuffer(
                                streetEdge.getGeometry(), config.getBufferSize()))
                        .collect(Collectors.toList());

                if (!nearestEdges.isEmpty()) {
                    this.featuresMapped++;
                }

                for (T edge : nearestEdges) {
                    featuresForEdge.computeIfAbsent(edge, key -> new ArrayList<>());
                    featuresForEdge.get(edge).add(feature);
                }

            }

            progressTracker.step(m -> LOG.info(m));
        }

        LOG.info(progressTracker.completeMessage());

        return featuresForEdge;
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
     * Groups the features by their nearest edge and performs a weighted average of their scores
     * based on their length. The calculated value is then set as a "green score" for the edge.
     * <p>
     * This option allows for multiple features (thus multiple scores) for a single id.
     */
    private void weightedAverageMap() {
        Map<T, List<GreenFeature>> closestFeatures =
                mapFeaturesToNearestEdge(this.featuresWithId, this.edgesWithId);

        closestFeatures.forEach((greenStreetEdge, greenFeatures) -> {
            var key = greenFeatures.size();
            nFeaturesForEdge.putIfAbsent(key, new ArrayList<>());
            nFeaturesForEdge.get(key).add(greenStreetEdge);
        });

        progressTracker = track("Computing scores", 5000, closestFeatures.keySet().size());

        for (T edge : closestFeatures.keySet()) {
            // For debug reasons
            this.edgesMapped++;

            var intersectionSizes = closestFeatures.get(edge)
                    .stream()
                    .map(feature -> feature.intersectionSize(edge.getGeometry(),
                            config.getBufferSize()))
                    .collect(Collectors.toList());

            var features = closestFeatures.get(edge);

            var totalIntersectionSize = 0d;
            for (double is : intersectionSizes) {totalIntersectionSize += is;}

            var combinedScore = 0d;
            for (int i = 0; i < features.size(); i++) {
                combinedScore += features.get(i).combinedScore * intersectionSizes.get(i);
            }
            edge.setGreenyness(combinedScore / totalIntersectionSize);

            var properties = new ArrayList<>(config.getProperties());
            for (var prop: properties) {
                var value = 0d;
                for (int i=0; i < features.size(); i++) {
                    value += features.get(i).scores.get(prop) * intersectionSizes.get(i);
                }
                value /= totalIntersectionSize;
                edge.putScore(prop, value);
            }

            config.getFeatures().forEach(variable -> {
                var value = closestFeatures.get(edge)
                        .stream()
                        .anyMatch(feature -> feature.features.getOrDefault(variable, false));

                edge.putFeature(variable, value);
            });

            progressTracker.step(m -> LOG.info(m));
        }

        LOG.info(progressTracker.completeMessage());
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
        config.getProperties().forEach(prop -> {
            var value = ((Number) feature.getAttribute(prop)).doubleValue();
            variables.put(prop, value);
            expression.setVariable(prop, value);
        });

        var features = new HashMap<String, Boolean>();
        config.getFeatures().forEach(feat -> {
            var value = ((Number) feature.getAttribute(feat)).doubleValue() == 1;
            features.put(feat, value);
        });

        var combinedScore = expression.evaluate();

        Geometry geometry = (LineString) feature.getDefaultGeometryProperty().getValue();
        return new GreenFeature(id, variables, features, geometry, combinedScore);
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
    private Map<Long, List<T>> getStreetEdges(Graph graph) {
        Map<Long, List<T>> edgesWithId = new HashMap<>();

        graph.getEdgesOfType(StreetEdge.class)
                .stream()
                .filter(GreenFactor.class::isInstance)
                .map(e -> (T) e)
                .forEach(edge -> {
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

    public void writeFiles(Graph graph) {
        LOG.info("Generating LDGeojson...");
        var features = graph.getEdgesOfType(StreetEdge.class)
                .stream()
                .filter(GreenFactor.class::isInstance)
                .map(e -> toFeature(e).toJson())
                .collect(Collectors.toList());

        var nonGreen = graph.getEdgesOfType(StreetEdge.class)
                .stream()
                .filter(e -> !(e instanceof GreenStreetEdge))
                .map(e -> toFeature(e).toJson())
                .collect(Collectors.toList());

        LOG.info("Write LDGeojson on file.");

        try (PrintWriter pw = new PrintWriter(config.getOutputFileName())) {
            features.forEach(f -> pw.println(f));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter("nonGreen.json")) {
            nonGreen.forEach(ng -> pw.println(ng));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter(config.getLogFileName())) {
            pw.println("Buffer size: " + config.getBufferSize());
            pw.println("Edges with at least one corresponding feature: " + this.edgesWithFeatures);
            pw.println("Edges without at least one corresponding feature: " + (this.nStreetEdges-this.edgesWithFeatures));
            pw.println("Features without corresponding edges: " + this.featuresWithoutEdges);
            pw.println("Edges successfully mapped: " + this.edgesMapped);
            pw.println("Features successfully mapped: " + this.featuresMapped);
            pw.println("Total number of features: " + this.nFeatures);
            pw.println("Total number of edges: " + this.nStreetEdges);
            pw.println("Total number of green edges: " + this.nGreenEdges);
            pw.println("Total number of green edges: " + this.nGreenEdges + " (" + coverage + "% of total).");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        /*try {
            URL url = new URL(URL_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "multipart/form-data");
            conn.
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private Feature toFeature(StreetEdge edge) {
        var points = Arrays.stream(edge.getGeometry().getCoordinates())
                .map(p -> Point.fromLngLat(p.x, p.y))
                .collect(Collectors.toList());

        var feature = Feature.fromGeometry(com.mapbox.geojson.LineString.fromLngLats(points));
        feature.addNumberProperty("osm_id", edge.wayId);

        if (edge instanceof GreenFactor) {
            var gEdge = (GreenFactor) edge;
            feature.addNumberProperty("score", gEdge.getGreenyness());
            var scores = gEdge.getScores().keySet();
            scores.forEach(p -> feature.addNumberProperty(p, gEdge.getScores().get(p)));

            var features = gEdge.getFeatures().keySet();
            features.forEach(p -> feature.addNumberProperty(p, gEdge.getFeatures().get(p) ? 1 : 0));
        }

        return feature;
    }
}
