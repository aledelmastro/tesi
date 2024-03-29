package org.opentripplanner.inspector;

import java.awt.Color;
import org.opentripplanner.ext.greenrouting.edgetype.GreenFactor;
import org.opentripplanner.inspector.EdgeVertexTileRenderer.EdgeVertexRenderer;
import org.opentripplanner.inspector.EdgeVertexTileRenderer.EdgeVisualAttributes;
import org.opentripplanner.inspector.EdgeVertexTileRenderer.VertexVisualAttributes;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;

public class NonGreenEdgeRenderer implements EdgeVertexRenderer {

    private ScalarColorPalette palette = new DefaultScalarColorPalette(1.0, 20, 10.0);

    public NonGreenEdgeRenderer() {
    }

    @Override
    public boolean renderEdge(Edge e, EdgeVisualAttributes attrs) {
        if (e instanceof StreetEdge && ! (e instanceof GreenFactor)) {
            attrs.color = Color.RED;
            attrs.label = ((StreetEdge) e).wayId + " - " + e.getClass().getSimpleName();
            return true;
        }

        return false;
    }

    @Override
    public boolean renderVertex(Vertex v, VertexVisualAttributes attrs) {
        return false;
    }

    @Override
    public String getName() {
        return "Non green";
    }
}