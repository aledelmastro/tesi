package org.opentripplanner.ext.greenrouting;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.graph_builder.services.DefaultStreetEdgeFactory;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.util.I18NString;

public class GreenStreetEdgeFactory extends DefaultStreetEdgeFactory {
    @Override
    public StreetEdge createEdge(IntersectionVertex startEndpoint, IntersectionVertex endEndpoint,
        LineString geometry, I18NString name, double length, StreetTraversalPermission permissions,
        boolean back) {
        StreetEdge pse;
        if (useElevationData) {
            pse = new GreenStreetWithElevationEdge(startEndpoint, endEndpoint, geometry, name, length,
                permissions, back);
        } else {
            pse = new GreenStreetEdge(startEndpoint, endEndpoint, geometry, name, length, permissions,
                back);
        }
        return pse;
    }
}
