package org.opentripplanner.ext.greenrouting;

import static org.opentripplanner.util.logging.ThrottleLogger.throttle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.geojson.GeoJSONDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.util.URLs;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenRouting implements GraphBuilderModule {
    private static final Logger LOG = LoggerFactory.getLogger(GreenRouting.class);
    private GreenRoutingConfig config;
    Map<Long, List<GreenSegment>> segments;
    List<GreenSegment> greenSegments;
    Collection<StreetEdge> edges;

    /**
     * Wrap LOG with a Throttle logger errors, this will prevent thousands
     * of log events, and just log one message every 3 second.
     */
    private static final Logger GREEN_ROUTING_ERROR_LOG = throttle(LOG);

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
        this.segments = new HashMap<>();
        this.greenSegments = new ArrayList<>();
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra, DataImportIssueStore issueStore) {
        File dataFile = new File(config.getFileName());

        if (dataFile.exists()) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put(GeoJSONDataStoreFactory.URL_PARAM.key, URLs.fileToUrl(dataFile));
                DataStore ds = DataStoreFinder.getDataStore(params);

                // TODO ds.getTypeNames()[0] valutare se aggiungere il nome del file in config
                var featureCollection = ds.getFeatureSource(ds.getTypeNames()[0]).getFeatures();

                edges = graph.getStreetEdges();

                try (SimpleFeatureIterator it = featureCollection.features()) {
                    while (it.hasNext()) {
                        SimpleFeature f = it.next();
                        long id = Long.parseLong((String) f.getAttribute(config.getId()));
                        double score = (Double) f.getAttribute(config.getScores());

                        Geometry geometry = (LineString) f.getDefaultGeometryProperty().getValue();
                        //var g = (Geometry) f.getDefaultGeometryProperty().getValue();
                        var segment = new GreenSegment(id,score, geometry);
                        greenSegments.add(segment);
                        addSegment(segment);
                    }
                }

                if (config.fastMapping())
                    fastMap();

                /*for (GreenSegment gs: greenSegments) {
                    List<Edge> edgesForID = getEdgesForId(edges, gs.id);

                    for (Edge e: edgesForID)
                        gs.overlapWith(e);
                }

                for (long id: segments.keySet()) {

                }*/


        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }}

    private List<Edge> getEdgesForId(Collection<StreetEdge> edges, long id) {
        return edges.stream()
                .filter(e -> e.wayId == id)
                .collect(Collectors.toList());
    }

    /*private Comparator<Edge> getOverlap(GreenSegment gs) {
        return (o1, o2) -> {
            var i1 = o1.getGeometry().intersection(gs.geometry).getLength();
            var i2 = o2.getGeometry().intersection(gs.geometry).getLength();
            return Double.compare(i1, i2);
        };
    }*/

    private void addSegment(GreenSegment segment) {
        if (!this.segments.containsKey(segment.id))
            this.segments.put(segment.id, new ArrayList<>());

        this.segments.get(segment.id).add(segment);
    }

    private void fastMap() {
        for (GreenSegment segment: this.greenSegments) {
            var edgesForID = getEdgesForId(this.edges, segment.id);
            for (Edge edge: edgesForID)
                if (edge instanceof GreenStreetEdge)
                    ((GreenStreetEdge)edge).greenyness = segment.score;
        }

    }

    @Override
    public void checkInputs() {
        // Nothing
    }
}
