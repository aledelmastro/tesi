package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.I18NString;

public class GreenStreetEdge extends StreetEdge {
    private Double greenyness = 0.0;

    private Map<String, Double> variables = new HashMap<>();

    public GreenStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, I18NString name, double length, StreetTraversalPermission permission, boolean back) {
        super(v1, v2, geometry, name, length, permission, back);
    }

    public GreenStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, I18NString name, StreetTraversalPermission permission, boolean back) {
        super(v1, v2, geometry, name, permission, back);
    }

    public Double getGreenyness() {
        return greenyness;
    }

    public void setGreenyness(double greenyness) {
        this.greenyness = greenyness;
    }

    public Map<String, Double> getVariables() {
        return this.variables;
    }

    public void setVariables(Map<String, Double> variables) {
        this.variables = variables;
    }

    public void putVariable(String variable, Double value) {
        this.variables.put(variable,value);
    }

    //TODO valutare override di splitDestructively
}
