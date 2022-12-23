package org.opentripplanner.ext.greenrouting;

import java.util.Map;

import org.locationtech.jts.geom.LineString;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.I18NString;

public class GreenStreetEdge extends StreetEdge implements GreenFactor {
    public Double greenyness = 0.0;

    public GreenStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, I18NString name, double length, StreetTraversalPermission permission, boolean back) {
        super(v1, v2, geometry, name, length, permission, back);
    }

    public GreenStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, I18NString name, StreetTraversalPermission permission, boolean back) {
        super(v1, v2, geometry, name, permission, back);
    }

    @Override
    public void setParams(Map<String, Double> params) {

    }

    //TODO valutare override di splitDestructively
}
