package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.LineString;
import org.opentripplanner.common.model.P2;
import org.opentripplanner.routing.edgetype.AreaEdge;
import org.opentripplanner.routing.edgetype.AreaEdgeList;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.SplitterVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.util.I18NString;

public class GreenAreaEdge extends AreaEdge implements GreenFactor {

    private Double greenyness = 0.0;
    private Map<String, Double> variables = new HashMap<>();
    private Map<String, Boolean> features = new HashMap<>();


    public GreenAreaEdge(
            IntersectionVertex startEndpoint,
            IntersectionVertex endEndpoint,
            LineString geometry,
            I18NString name,
            double length,
            StreetTraversalPermission permissions,
            boolean back,
            AreaEdgeList area
    ) {
        super(startEndpoint, endEndpoint, geometry, name, length, permissions, back, area);
    }

    @Override
    public double getGreenyness() {
        return this.greenyness;
    }

    @Override
    public void setGreenyness(double greenyness) {
        this.greenyness = greenyness;
    }

    @Override
    public Map<String, Double> getScores() {
        return this.variables;
    }

    @Override
    public void setScores(Map<String, Double> scores) {
        this.variables = scores;
    }

    @Override
    public void putScore(String label, Double value) {
        this.variables.put(label, value);
    }

    @Override
    public Map<String, Boolean> getFeatures() {
        return this.features;
    }

    @Override
    public void setFeatures(Map<String, Boolean> features) {
        this.features = features;
    }

    @Override
    public void putFeature(String label, boolean value) {
        this.features.put(label, value);
    }

    public P2<StreetEdge> splitDestructively(SplitterVertex v) {
        var splitEdges = super.splitDestructively(v);
        var greenSplitEdges = new StreetEdge[]{
                fromStreetEdge(splitEdges.first), fromStreetEdge(splitEdges.second)
        };

        return new P2<>(greenSplitEdges);
    }

    private static GreenStreetWithElevationEdge fromStreetEdge(StreetEdge e) {
        GreenStreetWithElevationEdge greenEdge = new GreenStreetWithElevationEdge((StreetVertex) e.getFromVertex(),
                (StreetVertex) e.getToVertex(), e.getGeometry(), e.getName(), e.getPermission(),
                e.isBack()
        );

        e.getToVertex().removeIncoming(e);
        e.getFromVertex().removeOutgoing(e);

        greenEdge.setBicycleSafetyFactor(e.getBicycleSafetyFactor());
        greenEdge.setHasBogusName(e.hasBogusName());
        greenEdge.setStairs(e.isStairs());
        greenEdge.setWheelchairAccessible(e.isWheelchairAccessible());
        greenEdge.setBack(e.isBack());
        if (e.wayId == 532533560) {
            int i = 97;
        }
        greenEdge.wayId = e.wayId;

        return greenEdge;
    }
}
