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

    private static final String URL_API =
            "https://api.mapbox.com/tilesets/v1/sources/ale-delmastro/green";
    private static final Logger LOG = LoggerFactory.getLogger(GreenRouting.class);
    /**
     * Wrap LOG with a Throttle logger errors, this will prevent thousands of log events, and just
     * log one message every 3 second.
     */
    private static final Logger GREEN_ROUTING_ERROR_LOG = throttle(LOG);
    private final GreenRoutingConfig config;
    private final int nodeMapped;
    private final int nFeaturesMulti = 0;
    Map<Long, List<GreenFeature>> featuresWithId;
    Map<Long, List<T>> edgesWithId;
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
        var nGreenEdges = graph.getEdges().stream().filter(GreenFactor.class::isInstance).count();
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

            weightedAverageMap();

            this.uploadTiles(graph);

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
    public Map<T, List<GreenFeature>> mapFeaturesToNearestEdge(
            Map<Long, List<GreenFeature>> featuresWithId,
            Map<Long, List<T>> streetEdgesWithId
    ) {
        Map<T, List<GreenFeature>> featuresForEdge = new HashMap<>();

        var sharedIds = featuresWithId.keySet()
                .stream()
                .filter(k -> streetEdgesWithId.containsKey(k))
                .collect(Collectors.toList());

        progressTracker = track("Map features to street edges", 5000, sharedIds.size());

        for (var id : sharedIds) {
            if (id == 133960538) {
                int v = 108;
            }
            var edgesWithId = streetEdgesWithId.get(id);
            for (var feature : featuresWithId.get(id)) {
                var nearestEdges = edgesWithId.stream()
                        .filter(streetEdge -> feature.intersectsBuffer(
                                streetEdge.getGeometry(), config.getBufferSize()))
                        .collect(Collectors.toList());

                for (T edge : nearestEdges) {
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
    private List<T> greenStreetEdgesForID(long id) {
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

        /*var present = this.featuresWithId.get(feature.id).stream()
                .anyMatch(fwi -> fwi.geometry.equalsTopo(feature.geometry));
        if (!present)*/
            this.featuresWithId.get(feature.id).add(feature);
        /*else
            LOG.info("Masterplannnnn");*/
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

        Map<Integer, List<T>> nFeatureForEdge = new HashMap<>();
        closestFeatures.forEach((greenStreetEdge, greenFeatures) -> {
            var key = greenFeatures.size();
            nFeatureForEdge.putIfAbsent(key, new ArrayList<>());
            nFeatureForEdge.get(key).add(greenStreetEdge);
        });

        progressTracker = track("Computing scores", 5000, closestFeatures.keySet().size());

        for (T edge : closestFeatures.keySet()) {
            if (edge.wayId == 133960538) {
                int v = 108;
            }
            var totalLength = closestFeatures.get(edge)
                    .stream()
                    .mapToDouble(segment -> segment.geometry.getLength())
                    .sum();

            if (edge.wayId == 178459983) {
                int afdseilhju=1;
            }

            edge.setGreenyness(
                    closestFeatures.get(edge)
                            .stream()
                            .mapToDouble(feature -> feature.combinedScore * feature.proportion(
                                    edge.getGeometry(), config.getBufferSize()))
                            .sum()
            );

            config.getProperties().forEach(variable -> {
                var value = closestFeatures.get(edge)
                        .stream()
                        .mapToDouble(feature -> feature.scores.get(variable)
                                * feature.proportion(edge.getGeometry(), config.getBufferSize()))
                        .sum();

                if (value > 1 || value < 0) {
                    int i = 1;
                }

                if (value > 3) {
                    int i = 1;
                }
                edge.putScore(variable, value);
            });

            config.getFeatures().forEach(variable -> {
                var value = closestFeatures.get(edge)
                        .stream()
                        .anyMatch(feature -> feature.features.getOrDefault(variable, false));

                if (value) {
                    int afdskhjl = 0;
                }
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

    public void uploadTiles(Graph graph) {
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
            /*var scores = gEdge.getScores().keySet();
            scores.forEach(p -> feature.addNumberProperty(p, gEdge.getScores().get(p)));

            var features = gEdge.getFeatures().keySet();
            features.forEach(p -> feature.addNumberProperty(p, gEdge.getFeatures().get(p) ? 1 : 0));*/
        }

        return feature;
    }
}
