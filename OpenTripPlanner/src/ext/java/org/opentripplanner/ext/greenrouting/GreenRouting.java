package org.opentripplanner.ext.greenrouting;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.module.ned.ElevationModule;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.sandbox.greenrouting.GreenRoutingConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opentripplanner.util.logging.ThrottleLogger.throttle;

public class GreenRouting implements GraphBuilderModule {
    private static final Logger LOG = LoggerFactory.getLogger(GreenRouting.class);
    private GreenRoutingConfig config;

    /**
     * Wrap LOG with a Throttle logger for elevation edge warnings, this will prevent thousands
     * of log events, and just log one message every 3 second.
     */
    private static final Logger GREEN_ROUTING_ERROR_LOG = throttle(LOG);

    public GreenRouting(GreenRoutingConfig config) {
        this.config = config;
    }

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra, DataImportIssueStore issueStore) {
        var edges = graph.getStreetEdges();
        int greenE = 0;
        int others = 0;
        Set<String> cl = new HashSet<>();
        for (StreetEdge e: edges) {
            if (e instanceof GreenStreetEdge) {
                var ge = (GreenStreetEdge) e;
                if (ge.wayId % 2 == 0)
                    ge.greenyness = 3;
                else
                    ge.greenyness = 1;
                greenE++;
            }
            else {
                cl.add(e.getClass().getName());
                others++;
            }
        }
        int i;
        i = 12;

    }

    @Override
    public void checkInputs() {
        var p = config.toPath().toAbsolutePath();
        var f  = new File(this.config.getFileName());
        if (Files.exists(config.toPath())) {
            LOG.info("Green factors file found!");
        }
        else {
            LOG.warn(
                "No green factors file found at {} or read access not allowed! ", "s"
            );
        }
    }
}
