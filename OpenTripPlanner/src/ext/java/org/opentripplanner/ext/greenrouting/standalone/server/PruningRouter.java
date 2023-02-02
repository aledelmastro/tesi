package org.opentripplanner.ext.greenrouting.standalone.server;

import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.RouterConfig;
import org.opentripplanner.standalone.server.Router;

public class PruningRouter extends Router {
    private Graph originalGraph;

    public PruningRouter(
            Graph graph,
            RouterConfig routerConfig
    ) {
        super(graph, routerConfig);
        originalGraph = graph;
    }

    @Override
    public Graph getGraph() {
        var graph = super.getGraph();

        /*graph.getEdgesOfType(StreetEdge.class).stream()
                .filter(GreenFactor.class::isInstance)
                .forEach(graph::removeEdge);*/

        return graph;
    }

    public void filter() {

    }
}
