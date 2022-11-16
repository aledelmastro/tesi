package org.opentripplanner.ext.greenrouting;

import java.util.HashMap;

import org.opentripplanner.graph_builder.DataImportIssueStore;
import org.opentripplanner.graph_builder.services.GraphBuilderModule;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;

public class GreenRouting implements GraphBuilderModule {

    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra, DataImportIssueStore issueStore) {
        var edges = graph.getStreetEdges();
        for (StreetEdge e: edges)
            e.tmp = 50;
    }

    @Override
    public void checkInputs() {
        // niente
    }
}
